package dolphin.android.apps.schoolbell.ui

import android.app.Application
import android.app.backup.BackupManager
import dolphin.android.apps.schoolbell.data.ScheduleDao
import dolphin.android.apps.schoolbell.data.SettingsRepository
import dolphin.android.apps.schoolbell.service.AlarmScheduler
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
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
    fun `addSchedule - inserts to dao, schedules alarm, and notifies backup`() = runTest {
        val viewModel = MainViewModel(application, scheduleDao, settingsRepository, backupManager, systemFeatureChecker)
        
        // Mock insert to return an ID
        coEvery { scheduleDao.insert(any()) } returns 1L
        
        viewModel.addSchedule(8, 0, "Test Bell", "1,2,3")
        advanceUntilIdle()

        coVerify { scheduleDao.insert(match { 
            it.hour == 8 && it.minute == 0 && it.label == "Test Bell" 
        }) }
        verify { AlarmScheduler.scheduleAlarm(any(), any()) }
        verify { backupManager.dataChanged() }
    }
}
