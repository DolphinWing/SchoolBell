package dolphin.android.apps.schoolbell.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dolphin.android.apps.schoolbell.R
import dolphin.android.apps.schoolbell.ui.theme.SchoolBellTheme

@Composable
fun GlobalSettingsCard(
    masterEnabled: Boolean,
    useCustomBell: Boolean,
    onToggleMaster: (Boolean) -> Unit,
    onToggleCustomBell: (Boolean) -> Unit,
    onTestBell: () -> Unit,
    initialExpanded: Boolean = false
) {
    var isExpanded by remember { mutableStateOf(initialExpanded) }
    val shouldBeExpanded = isExpanded || !masterEnabled

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = if (masterEnabled)
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
            else
                MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .clickable { if (masterEnabled) isExpanded = !isExpanded }
                .padding(16.dp)
        ) {
            SettingsHeaderRow(
                masterEnabled = masterEnabled,
                useCustomBell = useCustomBell,
                shouldBeExpanded = shouldBeExpanded,
                isExpanded = isExpanded
            )

            if (shouldBeExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(
                    color = if (masterEnabled)
                        MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f)
                    else
                        MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.1f)
                )
                Spacer(modifier = Modifier.height(12.dp))

                MasterSwitchRow(
                    masterEnabled = masterEnabled,
                    onToggleMaster = onToggleMaster
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = if (masterEnabled)
                        MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f)
                    else
                        MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.1f)
                )

                RingtoneModeRow(
                    useCustomBell = useCustomBell,
                    onToggleCustomBell = onToggleCustomBell
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = if (masterEnabled)
                        MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f)
                    else
                        MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.1f)
                )

                TestAlarmRow(
                    masterEnabled = masterEnabled,
                    onTestBell = onTestBell
                )
            }
        }
    }
}

@Composable
private fun SettingsHeaderRow(
    masterEnabled: Boolean,
    useCustomBell: Boolean,
    shouldBeExpanded: Boolean,
    isExpanded: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (!shouldBeExpanded) {
                Text(
                    text = if (masterEnabled) {
                        if (useCustomBell) stringResource(R.string.settings_summary_campus) else stringResource(
                            R.string.settings_summary_system
                        )
                    } else {
                        stringResource(R.string.settings_summary_disabled)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (masterEnabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
                )
            }
        }
        if (masterEnabled) {
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MasterSwitchRow(
    masterEnabled: Boolean,
    onToggleMaster: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (masterEnabled) Icons.Default.Notifications else Icons.Default.NotificationsOff,
            contentDescription = null,
            tint = if (masterEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                stringResource(R.string.settings_master_switch),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                if (masterEnabled) stringResource(R.string.settings_enabled) else stringResource(R.string.settings_disabled),
                style = MaterialTheme.typography.bodySmall,
                color = if (masterEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }
        Switch(
            checked = masterEnabled,
            onCheckedChange = onToggleMaster,
            colors = SwitchDefaults.colors(
                uncheckedThumbColor = MaterialTheme.colorScheme.error,
                uncheckedTrackColor = MaterialTheme.colorScheme.errorContainer,
            )
        )
    }
}

@Composable
private fun RingtoneModeRow(
    useCustomBell: Boolean,
    onToggleCustomBell: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.MusicNote,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                stringResource(R.string.settings_ringtone_mode),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                if (useCustomBell) stringResource(R.string.settings_campus_bell) else stringResource(R.string.settings_system_alarm),
                style = MaterialTheme.typography.bodySmall,
                color = if (useCustomBell) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = useCustomBell,
            onCheckedChange = onToggleCustomBell,
            thumbContent = {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize)
                )
            }
        )
    }
}

@Composable
private fun TestAlarmRow(
    masterEnabled: Boolean,
    onTestBell: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTestBell() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = null,
            tint = if (masterEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                stringResource(R.string.settings_test_alarm),
                style = MaterialTheme.typography.titleMedium,
                color = if (masterEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                stringResource(R.string.settings_test_alarm_summary),
                style = MaterialTheme.typography.bodySmall,
                color = if (masterEnabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
            )
        }
    }
}

@Preview(showBackground = true, name = "Settings Card Collapsed")
@Composable
fun GlobalSettingsCardCollapsedPreview() {
    SchoolBellTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            GlobalSettingsCard(
                masterEnabled = true,
                useCustomBell = true,
                onToggleMaster = {},
                onToggleCustomBell = {},
                onTestBell = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Settings Card Expanded")
@Composable
fun GlobalSettingsCardExpandedPreview() {
    SchoolBellTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            GlobalSettingsCard(
                masterEnabled = true,
                useCustomBell = true,
                onToggleMaster = {},
                onToggleCustomBell = {},
                onTestBell = {},
                initialExpanded = true
            )
        }
    }
}

@Preview(showBackground = true, name = "Settings Card Disabled (Warning)")
@Composable
fun GlobalSettingsCardDisabledPreview() {
    SchoolBellTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            GlobalSettingsCard(
                masterEnabled = false,
                useCustomBell = true,
                onToggleMaster = {},
                onToggleCustomBell = {},
                onTestBell = {}
            )
        }
    }
}
