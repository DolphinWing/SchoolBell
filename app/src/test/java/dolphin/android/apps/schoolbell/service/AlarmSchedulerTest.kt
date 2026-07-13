package dolphin.android.apps.schoolbell.service

import dolphin.android.apps.schoolbell.data.Schedule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.util.Calendar

class AlarmSchedulerTest {

    // Helper to get fixed "now" for testing: 2024-07-10 09:00:00 (Wednesday)
    private fun getFixedNow(): Long {
        return Calendar.getInstance().apply {
            set(2024, Calendar.JULY, 10, 9, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    @Test
    fun `calculateNextTriggerTime - today later - returns today`() {
        val now = getFixedNow()
        val schedule = Schedule(
            hour = 10,
            minute = 0,
            label = "Later Today",
            isActive = true,
            daysOfWeek = "3" // Wednesday
        )

        val trigger = AlarmScheduler.calculateNextTriggerTime(schedule, now)
        assertNotNull(trigger)

        val resultCal = Calendar.getInstance().apply { timeInMillis = trigger!! }
        assertEquals(2024, resultCal.get(Calendar.YEAR))
        assertEquals(Calendar.JULY, resultCal.get(Calendar.MONTH))
        assertEquals(10, resultCal.get(Calendar.DAY_OF_MONTH))
        assertEquals(10, resultCal.get(Calendar.HOUR_OF_DAY))
    }

    @Test
    fun `calculateNextTriggerTime - today past - returns next week if same day only`() {
        val now = getFixedNow()
        val schedule = Schedule(
            hour = 8,
            minute = 0,
            label = "Already Past Today",
            isActive = true,
            daysOfWeek = "3" // Wednesday
        )

        val trigger = AlarmScheduler.calculateNextTriggerTime(schedule, now)
        assertNotNull(trigger)

        val resultCal = Calendar.getInstance().apply { timeInMillis = trigger!! }
        // Should be next Wednesday (July 17)
        assertEquals(17, resultCal.get(Calendar.DAY_OF_MONTH))
        assertEquals(8, resultCal.get(Calendar.HOUR_OF_DAY))
    }

    @Test
    fun `calculateNextTriggerTime - Mon to Fri - triggers on Thursday`() {
        val now = getFixedNow() // Wednesday 09:00
        val schedule = Schedule(
            hour = 8,
            minute = 0,
            label = "Weekday Morning",
            isActive = true,
            daysOfWeek = "1,2,3,4,5" // Mon-Fri
        )

        val trigger = AlarmScheduler.calculateNextTriggerTime(schedule, now)
        assertNotNull(trigger)

        val resultCal = Calendar.getInstance().apply { timeInMillis = trigger!! }
        // Wednesday 08:00 is past, so next is Thursday July 11
        assertEquals(11, resultCal.get(Calendar.DAY_OF_MONTH))
        assertEquals(8, resultCal.get(Calendar.HOUR_OF_DAY))
    }

    @Test
    fun `calculateNextTriggerTime - weekend only during weekday - triggers on Sat`() {
        val now = getFixedNow() // Wednesday July 10
        val schedule = Schedule(
            hour = 8,
            minute = 0,
            label = "Weekend",
            isActive = true,
            daysOfWeek = "6,7" // Sat, Sun
        )

        val trigger = AlarmScheduler.calculateNextTriggerTime(schedule, now)
        assertNotNull(trigger)

        val resultCal = Calendar.getInstance().apply { timeInMillis = trigger!! }
        // Next should be Sat July 13
        assertEquals(13, resultCal.get(Calendar.DAY_OF_MONTH))
        assertEquals(8, resultCal.get(Calendar.HOUR_OF_DAY))
    }
}
