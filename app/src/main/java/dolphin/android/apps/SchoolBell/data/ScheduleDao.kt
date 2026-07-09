package dolphin.android.apps.SchoolBell.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(schedule: Schedule): Long

    @Update
    suspend fun update(schedule: Schedule)

    @Delete
    suspend fun delete(schedule: Schedule)

    @Query("SELECT * FROM schedules ORDER BY hour ASC, minute ASC, id ASC")
    fun getAllSchedulesFlow(): Flow<List<Schedule>>

    @Query("SELECT * FROM schedules ORDER BY hour ASC, minute ASC, id ASC")
    suspend fun getAllSchedules(): List<Schedule>

    @Query("SELECT * FROM schedules WHERE isActive = 1")
    suspend fun getActiveSchedules(): List<Schedule>

    @Query("SELECT * FROM schedules WHERE id = :id LIMIT 1")
    suspend fun getScheduleById(id: Int): Schedule?
}
