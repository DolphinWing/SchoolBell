package dolphin.android.apps.schoolbell.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class ScheduleDaoTest {

    private lateinit var scheduleDao: ScheduleDao
    private lateinit var db: ScheduleDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // Using an in-memory database because the information stored here disappears when the
        // process is killed.
        db = Room.inMemoryDatabaseBuilder(context, ScheduleDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        scheduleDao = db.scheduleDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetSchedule() = runBlocking {
        val schedule = Schedule(
            hour = 8,
            minute = 30,
            label = "Class Start",
            isActive = true,
            daysOfWeek = "1,2,3,4,5"
        )
        scheduleDao.insert(schedule)
        
        val allSchedules = scheduleDao.getAllSchedulesFlow().first()
        assertEquals(allSchedules[0].label, "Class Start")
        assertEquals(allSchedules[0].hour, 8)
    }

    @Test
    @Throws(Exception::class)
    fun getAllSchedules_isSortedByTime() = runBlocking {
        val s1 = Schedule(hour = 12, minute = 0, label = "Lunch", isActive = true, daysOfWeek = "1")
        val s2 = Schedule(hour = 8, minute = 0, label = "Morning", isActive = true, daysOfWeek = "1")
        val s3 = Schedule(hour = 8, minute = 45, label = "Period 1", isActive = true, daysOfWeek = "1")

        scheduleDao.insert(s1)
        scheduleDao.insert(s2)
        scheduleDao.insert(s3)

        val allSchedules = scheduleDao.getAllSchedulesFlow().first()
        
        assertEquals(3, allSchedules.size)
        // Should be: 08:00, 08:45, 12:00
        assertEquals("Morning", allSchedules[0].label)
        assertEquals("Period 1", allSchedules[1].label)
        assertEquals("Lunch", allSchedules[2].label)
    }

    @Test
    @Throws(Exception::class)
    fun deleteSchedule_removesFromDb() = runBlocking {
        val schedule = Schedule(hour = 9, minute = 0, label = "To Delete", isActive = true, daysOfWeek = "1")
        val id = scheduleDao.insert(schedule)
        val inserted = schedule.copy(id = id.toInt())

        scheduleDao.delete(inserted)
        
        val allSchedules = scheduleDao.getAllSchedulesFlow().first()
        assertTrue(allSchedules.isEmpty())
    }

    @Test
    @Throws(Exception::class)
    fun updateSchedule_changesData() = runBlocking {
        val schedule = Schedule(hour = 9, minute = 0, label = "Old Label", isActive = true, daysOfWeek = "1")
        val id = scheduleDao.insert(schedule)
        
        val updated = schedule.copy(id = id.toInt(), label = "New Label")
        scheduleDao.update(updated)
        
        val result = scheduleDao.getScheduleById(id.toInt())
        assertEquals("New Label", result?.label)
    }
}
