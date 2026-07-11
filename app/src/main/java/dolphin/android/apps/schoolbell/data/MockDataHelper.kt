package dolphin.android.apps.schoolbell.data

/**
 * Utility to isolate static mock schedules data from ViewModels.
 */
object MockDataHelper {
    /**
     * Returns a default set of mock schedules for development and testing.
     */
    fun getMockSchedules(): List<Schedule> {
        return listOf(
            Schedule(hour = 9, minute = 0, label = "Good morning, Work.", isActive = true, daysOfWeek = "1,2,3,4,5"),
            Schedule(hour = 11, minute = 50, label = "Time to lunch, dear.", isActive = true, daysOfWeek = "1,2,3,4,5"),
            Schedule(hour = 12, minute = 50, label = "Time to work, guys.", isActive = true, daysOfWeek = "1,2,3,4,5"),
            Schedule(hour = 15, minute = 30, label = "Afternoon break", isActive = true, daysOfWeek = "1,2,3,4,5"),
            Schedule(hour = 15, minute = 50, label = "Time to get back to work.", isActive = true, daysOfWeek = "1,2,3,4,5"),
            Schedule(hour = 18, minute = 0, label = "Time to leave, dude.", isActive = true, daysOfWeek = "1,2,3,4,5"),
            Schedule(hour = 20, minute = 0, label = "It's too late.", isActive = true, daysOfWeek = "1,2,3,4,5")
        )
    }
}
