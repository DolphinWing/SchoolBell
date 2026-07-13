package dolphin.android.apps.schoolbell.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dolphin.android.apps.schoolbell.R

@Composable
fun PermissionWarningCard(
    permissionsState: MainViewModel.PermissionsState,
    onRequestNotification: () -> Unit,
    onRequestExactAlarm: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    stringResource(R.string.perm_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (!permissionsState.hasNotificationPermission) {
                Text(
                    stringResource(R.string.perm_notification_desc),
                    style = MaterialTheme.typography.bodyMedium
                )
                TextButton(onClick = onRequestNotification) {
                    Text(stringResource(R.string.perm_notification_btn))
                }
            }

            if (!permissionsState.canScheduleExactAlarms) {
                Text(
                    stringResource(R.string.perm_exact_alarm_desc),
                    style = MaterialTheme.typography.bodyMedium
                )
                TextButton(onClick = onRequestExactAlarm) {
                    Text(stringResource(R.string.perm_exact_alarm_btn))
                }
            }
        }
    }
}
