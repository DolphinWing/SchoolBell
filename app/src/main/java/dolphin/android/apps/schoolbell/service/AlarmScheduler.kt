package dolphin.android.apps.schoolbell.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import dolphin.android.apps.schoolbell.data.Schedule
import timber.log.Timber
import java.util.Calendar

object AlarmScheduler {
    private const val TAG = "AlarmScheduler"

    fun calendarDayToIso(calendarDay: Int): Int {
        return when (calendarDay) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }
    }

    fun calculateNextTriggerTime(schedule: Schedule, nowMillis: Long = System.currentTimeMillis()): Long? {
        val daysSet = schedule.getDaysSet()
        if (daysSet.isEmpty()) return null

        for (offset in 0..7) {
            val targetCal = Calendar.getInstance().apply {
                timeInMillis = nowMillis
                add(Calendar.DAY_OF_YEAR, offset)
                set(Calendar.HOUR_OF_DAY, schedule.hour)
                set(Calendar.MINUTE, schedule.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val targetIsoDay = calendarDayToIso(targetCal.get(Calendar.DAY_OF_WEEK))
            if (daysSet.contains(targetIsoDay)) {
                if (offset > 0 || targetCal.timeInMillis > nowMillis) {
                    return targetCal.timeInMillis
                }
            }
        }
        return null
    }

    fun scheduleAlarm(context: Context, schedule: Schedule) {
        if (!schedule.isActive) {
            cancelAlarm(context, schedule)
            return
        }

        val triggerTime = calculateNextTriggerTime(schedule)
        if (triggerTime == null) {
            Timber.tag(TAG).w("Could not calculate next trigger time for schedule id=${schedule.id} (${schedule.label})")
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "dolphin.android.apps.schoolbell.ACTION_RING"
            putExtra("SCHEDULE_ID", schedule.id)
            putExtra("SCHEDULE_LABEL", schedule.label)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            schedule.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                    Timber.tag(TAG).d("Scheduled EXACT alarm for id=${schedule.id} (${schedule.label}) at $triggerTime")
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                    Timber.tag(TAG).d("Scheduled INEXACT (fallback) alarm for id=${schedule.id} (${schedule.label}) at $triggerTime")
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
                Timber.tag(TAG).d("Scheduled EXACT alarm (pre-S) for id=${schedule.id} (${schedule.label}) at $triggerTime")
            }
        } catch (e: SecurityException) {
            Timber.tag(TAG).e(e, "SecurityException scheduling exact alarm for id=${schedule.id}")
            // Fallback to inexact
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }

    fun cancelAlarm(context: Context, schedule: Schedule) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "dolphin.android.apps.schoolbell.ACTION_RING"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            schedule.id,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Timber.tag(TAG).d("Cancelled alarm for id=${schedule.id} (${schedule.label})")
        }
    }

    fun rescheduleAll(context: Context, schedules: List<Schedule>, masterEnabled: Boolean) {
        for (schedule in schedules) {
            if (masterEnabled && schedule.isActive) {
                scheduleAlarm(context, schedule)
            } else {
                cancelAlarm(context, schedule)
            }
        }
    }
}
