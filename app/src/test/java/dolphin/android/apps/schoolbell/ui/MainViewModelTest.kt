package dolphin.android.apps.schoolbell.ui

import android.app.Application
import android.app.backup.BackupManager
import androidx.compose.material3.SnackbarDuration
import dolphin.android.apps.schoolbell.R
import dolphin.android.apps.schoolbell.data.Schedule
import dolphin.android.apps.schoolbell.data.ScheduleDao
import dolphin.android.apps.schoolbell.data.SettingsRepository
import dolphin.android.apps.schoolbell.service.AlarmScheduler
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val application = mockk<Application>(relaxed = true)
    private val scheduleDao = mockk<ScheduleDao>(relaxed = true)
    private val settingsRepository = mockk<SettingsRepository>(relaxed = true)
    private val backupManager = mockk<BackupManager>(relaxed = true)
    private val systemFeatureChecker = mockk<SystemFeatureChecker>(relaxed = true)

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockkObject(AlarmScheduler)

        // Stub to avoid NoSuchElementException and suspend default null crashes in init {}
        coEvery { scheduleDao.getAllSchedules() } returns emptyList()
        every { settingsRepository.ignoreBatteryWarningFlow } returns flowOf(true)

        // Default mocks
        every { scheduleDao.getAllSchedulesFlow() } returns flowOf(emptyList())
        every { settingsRepository.masterSwitchFlow } returns flowOf(true)
        every { settingsRepository.useCustomBellFlow } returns flowOf(true)
        every { settingsRepository.volumeFlow } returns flowOf(0.5f)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `toggleMasterSwitch - updates repository, reschedules, and notifies backup`() = runTest {
        val viewModel = MainViewModel(application, scheduleDao, settingsRepository, backupManager, systemFeatureChecker)

        viewModel.toggleMasterSwitch(false)
        advanceUntilIdle()

        coVerify { settingsRepository.setMasterSwitch(false) }
        verify { AlarmScheduler.rescheduleAll(any(), any(), false) }
        verify { backupManager.dataChanged() }
    }

    @Test
    fun `addSchedule - inserts to dao, schedules alarm, and sends ShowSnackbar event`() = runTest {
        val viewModel = MainViewModel(application, scheduleDao, settingsRepository, backupManager, systemFeatureChecker)

        coEvery { scheduleDao.insert(any()) } returns 1L

        val events = mutableListOf<UiEvent>()
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiEventFlow.collect { events.add(it) }
        }

        viewModel.addSchedule(8, 30, "Math Class", "1,2,3")
        advanceUntilIdle()

        coVerify {
            scheduleDao.insert(match {
                it.hour == 8 && it.minute == 30 && it.label == "Math Class"
            })
        }
        verify { AlarmScheduler.scheduleAlarm(any(), any()) }
        verify { backupManager.dataChanged() }

        assertEquals(1, events.size)
        val event = events.first() as UiEvent.ShowSnackbar
        assertEquals(R.string.main_schedule_added, event.messageRes)
        assertEquals(listOf("Math Class"), event.formatArgs)
        assertEquals(SnackbarDuration.Short, event.duration)

        collectJob.cancel()
    }

    @Test
    fun `addSchedule - with empty label formats label to HH mm`() = runTest {
        val viewModel = MainViewModel(application, scheduleDao, settingsRepository, backupManager, systemFeatureChecker)

        coEvery { scheduleDao.insert(any()) } returns 1L

        val events = mutableListOf<UiEvent>()
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiEventFlow.collect { events.add(it) }
        }

        viewModel.addSchedule(8, 5, "", "1,2,3")
        advanceUntilIdle()

        assertEquals(1, events.size)
        val event = events.first() as UiEvent.ShowSnackbar
        assertEquals(R.string.main_schedule_added, event.messageRes)
        assertEquals(listOf("08:05"), event.formatArgs)

        collectJob.cancel()
    }

    @Test
    fun `deleteSchedule - cancels alarm, deletes from dao, and sends Long ShowSnackbar event`() = runTest {
        val viewModel = MainViewModel(application, scheduleDao, settingsRepository, backupManager, systemFeatureChecker)
        val schedule =
            Schedule(id = 3, hour = 10, minute = 15, label = "Class Dismissed", isActive = true, daysOfWeek = "1")

        val events = mutableListOf<UiEvent>()
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiEventFlow.collect { events.add(it) }
        }

        viewModel.deleteSchedule(schedule)
        advanceUntilIdle()

        coVerify { scheduleDao.delete(schedule) }
        verify { AlarmScheduler.cancelAlarm(any(), schedule) }
        verify { backupManager.dataChanged() }

        assertEquals(1, events.size)
        val event = events.first() as UiEvent.ShowSnackbar
        assertEquals(R.string.main_schedule_deleted, event.messageRes)
        assertEquals(listOf("Class Dismissed"), event.formatArgs)
        assertEquals(R.string.main_undo, event.actionRes)
        assertEquals(SnackbarDuration.Long, event.duration)

        // Test Undo action restores the schedule
        event.onAction?.invoke()
        advanceUntilIdle()

        coVerify { scheduleDao.insert(schedule) }

        collectJob.cancel()
    }

    @Test
    fun `restoreSchedule - inserts to dao, schedules alarm, and sends ShowSnackbar event`() = runTest {
        val viewModel = MainViewModel(application, scheduleDao, settingsRepository, backupManager, systemFeatureChecker)
        val schedule = Schedule(id = 3, hour = 12, minute = 0, label = "", isActive = true, daysOfWeek = "1")

        val events = mutableListOf<UiEvent>()
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiEventFlow.collect { events.add(it) }
        }

        viewModel.restoreSchedule(schedule)
        advanceUntilIdle()

        coVerify { scheduleDao.insert(schedule) }
        verify { AlarmScheduler.scheduleAlarm(any(), schedule) }
        verify { backupManager.dataChanged() }

        assertEquals(1, events.size)
        val event = events.first() as UiEvent.ShowSnackbar
        assertEquals(R.string.main_schedule_restored, event.messageRes)
        assertEquals(listOf("12:00"), event.formatArgs)
        assertEquals(SnackbarDuration.Short, event.duration)

        collectJob.cancel()
    }

    @Test
    fun `batteryDialogState - open and close methods toggle state`() {
        val viewModel = MainViewModel(application, scheduleDao, settingsRepository, backupManager, systemFeatureChecker)

        assertFalse(viewModel.showBatteryDialog.value)

        viewModel.openBatteryDialog()
        assertTrue(viewModel.showBatteryDialog.value)

        viewModel.closeBatteryDialog()
        assertFalse(viewModel.showBatteryDialog.value)
    }

    @Test
    fun `setVolume - updates repository and notifies backup`() = runTest {
        val viewModel = MainViewModel(application, scheduleDao, settingsRepository, backupManager, systemFeatureChecker)

        viewModel.setVolume(0.8f)
        advanceUntilIdle()

        coVerify { settingsRepository.setVolume(0.8f) }
        verify { backupManager.dataChanged() }
    }
}
