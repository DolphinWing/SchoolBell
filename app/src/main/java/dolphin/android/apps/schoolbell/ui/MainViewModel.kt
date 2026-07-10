package dolphin.android.apps.schoolbell.ui

import android.Manifest
import android.app.AlarmManager
import android.app.Application
import android.app.backup.BackupManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.VisibleForTesting
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import dolphin.android.apps.schoolbell.data.Schedule
import dolphin.android.apps.schoolbell.data.ScheduleDao
import dolphin.android.apps.schoolbell.data.ScheduleDatabase
import dolphin.android.apps.schoolbell.data.SettingsRepository
import dolphin.android.apps.schoolbell.service.AlarmScheduler
import dolphin.android.apps.schoolbell.BuildConfig
import android.os.PowerManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    application: Application,
    private val scheduleDao: ScheduleDao,
    private val settingsRepository: SettingsRepository,
    private val backupManager: BackupManager
) : AndroidViewModel(application) {

    private val _permissionsState = MutableStateFlow(PermissionsState())
    val permissionsState: StateFlow<PermissionsState> = _permissionsState.asStateFlow()

    data class PermissionsState(
        val hasNotificationPermission: Boolean = true,
        val canScheduleExactAlarms: Boolean = true
    )

    private val _showBatteryWarningSnackbar = MutableStateFlow(false)
    val showBatteryWarningSnackbar: StateFlow<Boolean> = _showBatteryWarningSnackbar.asStateFlow()

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
            delay(3000)
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
        val context = getApplication<Application>()
        val hasNotifications = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

        _permissionsState.value = PermissionsState(hasNotifications, canExact)

        if (checkBattery) {
            checkBatteryOptimizationWarning()
        }
    }

    fun checkBatteryOptimizationWarning() {
        viewModelScope.launch {
            val context = getApplication<Application>()
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            val isIgnoring = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                powerManager.isIgnoringBatteryOptimizations(context.packageName)
            } else {
                true
            }
            val ignoreWarning = settingsRepository.ignoreBatteryWarningFlow.first()
            _showBatteryWarningSnackbar.value = !isIgnoring && !ignoreWarning
        }
    }

    fun setIgnoreBatteryWarningPermanently(ignore: Boolean) {
        viewModelScope.launch {
            settingsRepository.setIgnoreBatteryWarning(ignore)
        }
    }

    fun dismissBatteryWarningSnackbar() {
        _showBatteryWarningSnackbar.value = false
    }

    fun insertMockSchedules() {
        if (!BuildConfig.DEBUG) return
        viewModelScope.launch {
            val mocks = listOf(
                Schedule(hour = 9, minute = 0, label = "Good morning, Work.", isActive = true, daysOfWeek = "1,2,3,4,5"),
                Schedule(hour = 11, minute = 50, label = "Time to lunch, dear.", isActive = true, daysOfWeek = "1,2,3,4,5"),
                Schedule(hour = 12, minute = 50, label = "Time to work, guys.", isActive = true, daysOfWeek = "1,2,3,4,5"),
                Schedule(hour = 15, minute = 30, label = "Afternoon break", isActive = true, daysOfWeek = "1,2,3,4,5"),
                Schedule(hour = 15, minute = 50, label = "Time to get back to work.", isActive = true, daysOfWeek = "1,2,3,4,5"),
                Schedule(hour = 18, minute = 0, label = "Time to leave, dude.", isActive = true, daysOfWeek = "1,2,3,4,5"),
                Schedule(hour = 20, minute = 0, label = "It's too late.", isActive = true, daysOfWeek = "1,2,3,4,5")
            )
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
        }
    }

    fun getSchedule(id: Int?): Schedule? {
        if (id == null) return null
        return schedules.value.find { it.id == id }
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
