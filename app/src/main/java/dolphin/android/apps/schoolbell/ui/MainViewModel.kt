package dolphin.android.apps.schoolbell.ui

import android.app.Application
import android.app.backup.BackupManager
import androidx.compose.material3.SnackbarDuration
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import dolphin.android.apps.schoolbell.BuildConfig
import dolphin.android.apps.schoolbell.data.Schedule
import dolphin.android.apps.schoolbell.data.ScheduleDao
import dolphin.android.apps.schoolbell.data.ScheduleDatabase
import dolphin.android.apps.schoolbell.data.SettingsRepository
import dolphin.android.apps.schoolbell.service.AlarmScheduler
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class MainViewModel(
    application: Application,
    private val scheduleDao: ScheduleDao,
    private val settingsRepository: SettingsRepository,
    private val backupManager: BackupManager,
    private val systemFeatureChecker: SystemFeatureChecker = SystemFeatureChecker(application)
) : AndroidViewModel(application) {

    private val _permissionsState = MutableStateFlow(PermissionsState())
    val permissionsState: StateFlow<PermissionsState> = _permissionsState.asStateFlow()

    data class PermissionsState(
        val hasNotificationPermission: Boolean = true,
        val canScheduleExactAlarms: Boolean = true
    )

    private val _uiEventChannel = Channel<UiEvent>(Channel.BUFFERED)
    val uiEventFlow = _uiEventChannel.receiveAsFlow()

    private val _showBatteryDialog = MutableStateFlow(false)
    val showBatteryDialog: StateFlow<Boolean> = _showBatteryDialog.asStateFlow()

    fun openBatteryDialog() {
        _showBatteryDialog.value = true
    }

    fun closeBatteryDialog() {
        _showBatteryDialog.value = false
    }

    private var hasShownBatteryWarningInSession = false

    val schedules: StateFlow<List<Schedule>> = scheduleDao.getAllSchedulesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val masterSwitchEnabled: StateFlow<Boolean> = settingsRepository.masterSwitchFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val useCustomBell: StateFlow<Boolean> = settingsRepository.useCustomBellFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    init {
        checkPermissions(checkBattery = false)
        viewModelScope.launch {
            // Ensure all alarms are synced with the current state on app start
            AlarmScheduler.rescheduleAll(getApplication(), scheduleDao.getAllSchedules(), masterSwitchEnabled.value)
        }
        viewModelScope.launch {
            // Delay check to avoid startup congestion
            delay(3000.milliseconds)
            checkBatteryOptimizationWarning()
        }
    }

    fun toggleUseCustomBell(useCustom: Boolean) {
        viewModelScope.launch {
            settingsRepository.setUseCustomBell(useCustom)
            backupManager.dataChanged()
        }
    }

    fun checkPermissions(checkBattery: Boolean = true) {
        val hasNotifications = systemFeatureChecker.hasNotificationPermission()
        val canExact = systemFeatureChecker.canScheduleExactAlarms()

        _permissionsState.value = PermissionsState(hasNotifications, canExact)

        if (checkBattery) {
            checkBatteryOptimizationWarning()
        }
    }

    fun checkBatteryOptimizationWarning() {
        viewModelScope.launch {
            val isIgnoring = systemFeatureChecker.isIgnoringBatteryOptimizations()
            val ignoreWarning = settingsRepository.ignoreBatteryWarningFlow.first()
            if (!isIgnoring && !ignoreWarning && !hasShownBatteryWarningInSession) {
                hasShownBatteryWarningInSession = true
                _uiEventChannel.send(
                    UiEvent.ShowSnackbar(
                        messageRes = dolphin.android.apps.schoolbell.R.string.battery_warning_snackbar_message,
                        actionRes = dolphin.android.apps.schoolbell.R.string.battery_warning_snackbar_action,
                        duration = SnackbarDuration.Long,
                        onAction = { openBatteryDialog() }
                    ))
            }
        }
    }

    fun setIgnoreBatteryWarningPermanently(ignore: Boolean) {
        viewModelScope.launch {
            settingsRepository.setIgnoreBatteryWarning(ignore)
        }
    }

    fun insertMockSchedules() {
        if (!BuildConfig.DEBUG) return
        viewModelScope.launch {
            val mocks = dolphin.android.apps.schoolbell.data.MockDataHelper.getMockSchedules()
            mocks.forEach { schedule ->
                val id = scheduleDao.insert(schedule)
                val inserted = schedule.copy(id = id.toInt())
                if (masterSwitchEnabled.value) {
                    AlarmScheduler.scheduleAlarm(getApplication(), inserted)
                }
            }
            backupManager.dataChanged()
        }
    }

    fun toggleMasterSwitch(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setMasterSwitch(enabled)
            // Reschedule all based on new master state
            AlarmScheduler.rescheduleAll(getApplication(), schedules.value, enabled)
            backupManager.dataChanged()
        }
    }

    fun toggleSchedule(schedule: Schedule, enabled: Boolean) {
        viewModelScope.launch {
            val updated = schedule.copy(isActive = enabled)
            scheduleDao.update(updated)

            if (masterSwitchEnabled.value) {
                if (enabled) {
                    AlarmScheduler.scheduleAlarm(getApplication(), updated)
                } else {
                    AlarmScheduler.cancelAlarm(getApplication(), updated)
                }
            }
            backupManager.dataChanged()
        }
    }

    fun addSchedule(hour: Int, minute: Int, label: String, days: String) {
        viewModelScope.launch {
            val newSchedule = Schedule(
                hour = hour,
                minute = minute,
                label = label,
                isActive = true,
                daysOfWeek = days
            )
            val id = scheduleDao.insert(newSchedule)
            val inserted = newSchedule.copy(id = id.toInt())

            if (masterSwitchEnabled.value) {
                AlarmScheduler.scheduleAlarm(getApplication(), inserted)
            }
            backupManager.dataChanged()

            val displayLabel = getDisplayLabel(label, hour, minute)
            _uiEventChannel.send(
                UiEvent.ShowSnackbar(
                    messageRes = dolphin.android.apps.schoolbell.R.string.main_schedule_added,
                    formatArgs = listOf(displayLabel),
                    duration = SnackbarDuration.Short
                )
            )
        }
    }

    fun updateSchedule(schedule: Schedule) {
        viewModelScope.launch {
            scheduleDao.update(schedule)
            if (masterSwitchEnabled.value && schedule.isActive) {
                AlarmScheduler.scheduleAlarm(getApplication(), schedule)
            } else {
                AlarmScheduler.cancelAlarm(getApplication(), schedule)
            }
            backupManager.dataChanged()
        }
    }

    fun deleteSchedule(schedule: Schedule) {
        viewModelScope.launch {
            AlarmScheduler.cancelAlarm(getApplication(), schedule)
            scheduleDao.delete(schedule)
            backupManager.dataChanged()

            val displayLabel = getDisplayLabel(schedule.label, schedule.hour, schedule.minute)
            _uiEventChannel.send(
                UiEvent.ShowSnackbar(
                messageRes = dolphin.android.apps.schoolbell.R.string.main_schedule_deleted,
                formatArgs = listOf(displayLabel),
                actionRes = dolphin.android.apps.schoolbell.R.string.main_undo,
                duration = SnackbarDuration.Long,
                onAction = { restoreSchedule(schedule) }
            ))
        }
    }

    private fun getDisplayLabel(label: String, hour: Int, minute: Int): String {
        return if (label.isBlank()) {
            String.format(java.util.Locale.getDefault(), "%02d:%02d", hour, minute)
        } else {
            label
        }
    }

    fun restoreSchedule(schedule: Schedule) {
        viewModelScope.launch {
            scheduleDao.insert(schedule)
            if (masterSwitchEnabled.value && schedule.isActive) {
                AlarmScheduler.scheduleAlarm(getApplication(), schedule)
            }
            backupManager.dataChanged()

            val displayLabel = getDisplayLabel(schedule.label, schedule.hour, schedule.minute)
            _uiEventChannel.send(
                UiEvent.ShowSnackbar(
                    messageRes = dolphin.android.apps.schoolbell.R.string.main_schedule_restored,
                    formatArgs = listOf(displayLabel),
                    duration = SnackbarDuration.Short
                )
            )
        }
    }

    fun getSchedule(id: Int?): Schedule? {
        if (id == null) return null
        return schedules.value.find { it.id == id }
    }

    fun clearAllSchedules() {
        viewModelScope.launch {
            schedules.value.forEach { schedule ->
                AlarmScheduler.cancelAlarm(getApplication(), schedule)
            }
            scheduleDao.deleteAll()
            backupManager.dataChanged()
        }
    }

    fun getDiagnosticsInfo(): List<String> {
        val activeSchedules = schedules.value.filter { it.isActive }
        if (activeSchedules.isEmpty()) return listOf("No active alarms in database.")

        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return activeSchedules.map { schedule ->
            val triggerTime = AlarmScheduler.calculateNextTriggerTime(schedule)
            if (triggerTime != null) {
                val dateStr = dateFormat.format(java.util.Date(triggerTime))
                "ID: ${schedule.id} | ${schedule.formattedTime()} | Next: $dateStr"
            } else {
                "ID: ${schedule.id} | ${schedule.formattedTime()} | Next: ERROR"
            }
        }
    }

    /**
     * Factory for creating [MainViewModel] with custom dependencies.
     */
    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                val database = ScheduleDatabase.getDatabase(application)

                return MainViewModel(
                    application,
                    database.scheduleDao(),
                    SettingsRepository(application),
                    BackupManager(application)
                ) as T
            }
        }
    }
}
