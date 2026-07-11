package dolphin.android.apps.schoolbell.data

/**
 * Validator utility to enforce domain rules and constraints for Schedule configurations.
 */
object ScheduleValidator {
    /**
     * Checks if a target schedule overlaps/conflicts with any existing active schedules.
     * A conflict occurs if the hour and minute are identical, and there is an intersection
     * in their repeating days.
     *
     * @param hour The hour of the target schedule (0-23)
     * @param minute The minute of the target schedule (0-59)
     * @param daysString Comma-separated days of week, e.g., "1,2,3,4,5"
     * @param existingSchedules Current list of schedules in the database
     * @param excludeId Optional schedule ID to exclude from comparison (typically the schedule currently being edited)
     * @return True if a conflict exists, false otherwise.
     */
    fun hasConflict(
        hour: Int,
        minute: Int,
        daysString: String,
        existingSchedules: List<Schedule>,
        excludeId: Int? = null
    ): Boolean {
        val targetDays = daysString.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .toSet()
        if (targetDays.isEmpty()) return false

        return existingSchedules.any { schedule ->
            if (excludeId != null && schedule.id == excludeId) return@any false

            schedule.hour == hour &&
                    schedule.minute == minute &&
                    schedule.getDaysSet().intersect(targetDays).isNotEmpty()
        }
    }
}
