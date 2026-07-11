package dolphin.android.apps.schoolbell.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScheduleValidatorTest {

    private val existingSchedules = listOf(
        Schedule(id = 1, hour = 8, minute = 0, label = "Morning Bell", isActive = true, daysOfWeek = "1,2,3,4,5"),
        Schedule(id = 2, hour = 12, minute = 0, label = "Lunch Bell", isActive = true, daysOfWeek = "1,2,3,4,5"),
        Schedule(id = 3, hour = 18, minute = 0, label = "Evening Bell", isActive = true, daysOfWeek = "6,7")
    )

    @Test
    fun `hasConflict - identical time and overlapping day - returns true`() {
        // Monday (1) overlaps with A (1..5)
        val conflict = ScheduleValidator.hasConflict(
            hour = 8,
            minute = 0,
            daysString = "1",
            existingSchedules = existingSchedules
        )
        assertTrue(conflict)
    }

    @Test
    fun `hasConflict - identical time but different days - returns false`() {
        // Weekend (6,7) does not overlap with A (1..5)
        val conflict = ScheduleValidator.hasConflict(
            hour = 8,
            minute = 0,
            daysString = "6,7",
            existingSchedules = existingSchedules
        )
        assertFalse(conflict)
    }

    @Test
    fun `hasConflict - different time but same days - returns false`() {
        val conflict = ScheduleValidator.hasConflict(
            hour = 9,
            minute = 0,
            daysString = "1,2,3,4,5",
            existingSchedules = existingSchedules
        )
        assertFalse(conflict)
    }

    @Test
    fun `hasConflict - editing same schedule - returns false`() {
        // Edit schedule 1, changing nothing. It shouldn't conflict with itself.
        val conflict = ScheduleValidator.hasConflict(
            hour = 8,
            minute = 0,
            daysString = "1,2,3,4,5",
            existingSchedules = existingSchedules,
            excludeId = 1
        )
        assertFalse(conflict)
    }

    @Test
    fun `hasConflict - empty target days - returns false`() {
        val conflict = ScheduleValidator.hasConflict(
            hour = 8,
            minute = 0,
            daysString = "",
            existingSchedules = existingSchedules
        )
        assertFalse(conflict)
    }
}
