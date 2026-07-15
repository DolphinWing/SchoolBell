package dolphin.android.apps.schoolbell.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import com.google.firebase.analytics.FirebaseAnalytics
import dolphin.android.apps.schoolbell.data.ScheduleDatabase
import dolphin.android.apps.schoolbell.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber

class AlarmReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "AlarmReceiver"
        const val ACTION_RING = "dolphin.android.apps.schoolbell.ACTION_RING"
        private const val INTENT_QUICK_BOOT = "android.intent.action.QUICKBOOT_POWERON"

        private const val FA_EVENT_ALARM_LATENCY = "alarm_trigger_latency"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Timber.tag(TAG).d("onReceive action=$action")

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settingsRepository = SettingsRepository(context)
                val masterEnabled = settingsRepository.masterSwitchFlow.first()

                when (action) {
                    ACTION_RING -> handleRingAction(context, intent, masterEnabled)
                    Intent.ACTION_BOOT_COMPLETED, INTENT_QUICK_BOOT -> {
                        handleBootAction(context, masterEnabled)
                    }
                }
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Error handling alarm broadcast")
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun handleRingAction(context: Context, intent: Intent, masterEnabled: Boolean) {
        val scheduleId = intent.getIntExtra("SCHEDULE_ID", -1)
        val label = intent.getStringExtra("SCHEDULE_LABEL") ?: "School Bell"

        logAlarmLatency(context, intent)

        Timber.tag(TAG).i("Ring trigger: scheduleId=$scheduleId, label='$label', masterEnabled=$masterEnabled")

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
    }

    private suspend fun handleBootAction(context: Context, masterEnabled: Boolean) {
        Timber.tag(TAG).i("System boot detected. Rescheduling all active alarms.")
        val db = ScheduleDatabase.getDatabase(context)
        val schedules = db.scheduleDao().getAllSchedules()
        AlarmScheduler.rescheduleAll(context, schedules, masterEnabled)
    }

    private fun logAlarmLatency(context: Context, intent: Intent) {
        val expectedTime = intent.getLongExtra("EXPECTED_TRIGGER_TIME", -1L)
        if (expectedTime == -1L) return

        val isExact = intent.getBooleanExtra("IS_EXACT_ALARM", false)
        val actualTime = System.currentTimeMillis()
        val latency = actualTime - expectedTime

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val isIgnoringBattery =
            powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: false

        try {
            val firebaseAnalytics = FirebaseAnalytics.getInstance(context)
            val bundle = android.os.Bundle().apply {
                putLong("latency_ms", latency)
                putString("is_battery_optimized", (!isIgnoringBattery).toString())
                putString("is_exact_alarm", isExact.toString())
            }
            firebaseAnalytics.logEvent(FA_EVENT_ALARM_LATENCY, bundle)
            Timber.tag(TAG).d("Logged telemetry $FA_EVENT_ALARM_LATENCY: latency=$latency ms")
            Timber.tag(TAG).d("  isBatteryOptimized=${!isIgnoringBattery}, isExact=$isExact")
        } catch (e: java.lang.Exception) {
            Timber.tag(TAG).e(e, "Failed to log telemetry to Firebase Analytics")
        }
    }
}
