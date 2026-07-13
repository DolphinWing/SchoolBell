package dolphin.android.apps.schoolbell.ui

import androidx.annotation.StringRes
import androidx.compose.material3.SnackbarDuration

sealed interface UiEvent {
    data class ShowSnackbar(
        @param:StringRes val messageRes: Int,
        val formatArgs: List<Any> = emptyList(),
        @param:StringRes val actionRes: Int? = null,
        val duration: SnackbarDuration = SnackbarDuration.Short,
        val onAction: (() -> Unit)? = null
    ) : UiEvent
}
