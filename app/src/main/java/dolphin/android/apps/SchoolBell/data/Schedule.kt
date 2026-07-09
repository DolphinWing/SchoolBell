package dolphin.android.apps.SchoolBell.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedules")
data class Schedule(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val hour: Int,         // 0-23
    val minute: Int,       // 0-59
    val label: String,     // e.g., "Class Start", "Lunch Break"
    val isActive: Boolean,  // enabled or disabled
    val daysOfWeek: String, // comma-separated days, e.g., "1,2,3,4,5" (1 = Monday, 7 = Sunday)
    val createdAt: Long = System.currentTimeMillis()
) {
    // Helper to get days of week as a set of integers
    fun getDaysSet(): Set<Int> {
        if (daysOfWeek.isBlank()) return emptySet()
        return daysOfWeek.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .toSet()
    }

    // Formatted time helper
    fun formattedTime(): String {
        return String.format("%02d:%02d", hour, minute)
    }
}
