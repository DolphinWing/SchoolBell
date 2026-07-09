package dolphin.android.apps.schoolbell.ui

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object MainKey : NavKey

@Serializable
data class EditScheduleKey(val scheduleId: Int? = null) : NavKey
