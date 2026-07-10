package dolphin.android.apps.schoolbell.ui

import android.Manifest
import android.app.AlarmManager
import android.app.Application
import android.app.backup.BackupManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dolphin.android.apps.schoolbell.data.Schedule
import dolphin.android.apps.schoolbell.data.ScheduleDao
import dolphin.android.apps.schoolbell.data.ScheduleDatabase
import dolphin.android.apps.schoolbell.data.SettingsRepository
import dolphin.android.apps.schoolbell.service.AlarmScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    application: Application,
    private val scheduleDao: ScheduleDao = ScheduleDatabase.getDatabase(application).scheduleDao(),
    private val settingsRepository: SettingsRepository = SettingsRepository(application),
    private val backupManager: BackupManager = BackupManager(application)
) : AndroidViewModel(application) {

    private val _permissionsState = MutableStateFlow(PermissionsState())
    val permissionsState: StateFlow<PermissionsState> = _permissionsState.asStateFlow()

    data class PermissionsState(
        val hasNotificationPermission: Boolean = true,
        val canScheduleExactAlarms: Boolean = true
    )

    val schedules: StateFlow<List<Schedule>> = scheduleDao.getAllSchedulesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val masterSwitchEnabled: StateFlow<Boolean> = settingsRepository.masterSwitchFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val useCustomBell: StateFlow<Boolean> = settingsRepository.useCustomBellFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    init {
        checkPermissions()
        viewModelScope.launch {
            // Ensure all alarms are synced with the current state on app start
            AlarmScheduler.rescheduleAll(getApplication(), scheduleDao.getAllSchedules(), masterSwitchEnabled.value)
        }
    }

    fun toggleUseCustomBell(useCustom: Boolean) {
        viewModelScope.launch {
            settingsRepository.setUseCustomBell(useCustom)
            backupManager.dataChanged()
        }
    }

    fun checkPermissions() {
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
}
