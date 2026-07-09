package dolphin.android.apps.schoolbell.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import dolphin.android.apps.schoolbell.data.ScheduleDatabase
import dolphin.android.apps.schoolbell.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "AlarmReceiver"
        const val ACTION_RING = "dolphin.android.apps.schoolbell.ACTION_RING"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d(TAG, "onReceive action=$action")

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settingsRepository = SettingsRepository(context)
                val masterEnabled = settingsRepository.masterSwitchFlow.first()

                if (action == ACTION_RING) {
                    val scheduleId = intent.getIntExtra("SCHEDULE_ID", -1)
                    val label = intent.getStringExtra("SCHEDULE_LABEL") ?: "School Bell"

                    Log.d(TAG, "Ring trigger: scheduleId=$scheduleId, label='$label', masterEnabled=$masterEnabled")

                    if (masterEnabled && scheduleId != -1) {
                        // Start playing the bell sound
                        val serviceIntent = Intent(context, BellRingService::class.java).apply {
                            putExtra("SCHEDULE_ID", scheduleId)
                            putExtra("SCHEDULE_LABEL", label)
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            context.startForegroundService(serviceIntent)
                        } else {
                            context.startService(serviceIntent)
                        }

                        // Reschedule the next occurrence for this particular schedule item
                        val db = ScheduleDatabase.getDatabase(context)
                        val schedule = db.scheduleDao().getScheduleById(scheduleId)
                        if (schedule != null) {
                            AlarmScheduler.scheduleAlarm(context, schedule)
                        }
                    }
                } else if (action == Intent.ACTION_BOOT_COMPLETED || action == "android.intent.action.QUICKBOOT_POWERON") {
                    Log.d(TAG, "System boot detected. Rescheduling all active alarms.")
                    val db = ScheduleDatabase.getDatabase(context)
                    val schedules = db.scheduleDao().getAllSchedules()
                    AlarmScheduler.rescheduleAll(context, schedules, masterEnabled)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling alarm broadcast", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
