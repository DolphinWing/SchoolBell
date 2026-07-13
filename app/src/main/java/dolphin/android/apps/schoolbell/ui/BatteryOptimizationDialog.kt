package dolphin.android.apps.schoolbell.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dolphin.android.apps.schoolbell.R

@Composable
fun BatteryOptimizationDialog(
    onDismiss: (neverShowAgain: Boolean) -> Unit,
    onGoToSettings: (neverShowAgain: Boolean) -> Unit
) {
    var neverShowAgain by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { onDismiss(neverShowAgain) },
        title = {
            Text(text = stringResource(R.string.battery_dialog_title))
        },
        text = {
            Column {
                Text(text = stringResource(R.string.battery_dialog_desc))
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { neverShowAgain = !neverShowAgain }
                ) {
                    Checkbox(
                        checked = neverShowAgain,
                        onCheckedChange = { neverShowAgain = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(R.string.battery_dialog_never_show))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onGoToSettings(neverShowAgain) }) {
                Text(text = stringResource(R.string.battery_dialog_settings_btn))
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss(neverShowAgain) }) {
                Text(text = stringResource(R.string.battery_dialog_cancel_btn))
            }
        }
    )
}
