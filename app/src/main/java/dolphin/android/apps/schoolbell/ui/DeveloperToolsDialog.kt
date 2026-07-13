package dolphin.android.apps.schoolbell.ui

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import dolphin.android.apps.schoolbell.service.BellRingService
import dolphin.android.apps.schoolbell.ui.theme.SchoolBellTheme

@Composable
fun DeveloperToolsDialog(
    onDismiss: () -> Unit,
    onAddMockSchedules: () -> Unit,
    onClearAllSchedules: () -> Unit,
    getDiagnostics: () -> List<String>
) {
    var diagnosticsList by remember { mutableStateOf(emptyList<String>()) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        diagnosticsList = getDiagnostics()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnClickOutside = false,
            dismissOnBackPress = true
        ),
        title = {
            Text(text = "Developer Tools", fontWeight = FontWeight.Bold)
        },
        text = {
            DeveloperToolsDialogContent(
                diagnosticsList = diagnosticsList,
                onAddMockSchedules = {
                    onAddMockSchedules()
                    diagnosticsList = getDiagnostics()
                },
                onClearAllSchedules = {
                    onClearAllSchedules()
                    diagnosticsList = getDiagnostics()
                },
                onTestRing = {
                    val label = "Developer Test Ringing"
                    val intent = Intent(context, BellRingService::class.java).apply {
                        putExtra("SCHEDULE_LABEL", label)
                    }
                    ContextCompat.startForegroundService(context, intent)
                },
                onForceSilent = {
                    val intent = Intent(context, BellRingService::class.java).apply {
                        action = BellRingService.ACTION_STOP
                    }
                    ContextCompat.startForegroundService(context, intent)
                },
                onRefreshDiagnostics = {
                    diagnosticsList = getDiagnostics()
                }
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun DeveloperToolsDialogContent(
    diagnosticsList: List<String>,
    onAddMockSchedules: () -> Unit,
    onClearAllSchedules: () -> Unit,
    onTestRing: () -> Unit,
    onForceSilent: () -> Unit,
    onRefreshDiagnostics: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section 1: Actions
        Text(
            text = "Actions",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )

        DeveloperActionPanel(
            onAddMock = onAddMockSchedules,
            onClearAll = onClearAllSchedules,
            onTestRing = onTestRing,
            onForceSilent = onForceSilent
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))

        // Section 2: Diagnostics
        DiagnosticsPanel(
            diagnosticsList = diagnosticsList,
            onRefresh = onRefreshDiagnostics
        )
    }
}

@Composable
private fun DeveloperActionPanel(
    onAddMock: () -> Unit,
    onClearAll: () -> Unit,
    onTestRing: () -> Unit,
    onForceSilent: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalButton(
                onClick = onAddMock,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Mocks", style = MaterialTheme.typography.labelMedium)
            }

            OutlinedButton(
                onClick = onClearAll,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Clear All", style = MaterialTheme.typography.labelMedium)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalButton(
                onClick = onTestRing,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Test Ring", style = MaterialTheme.typography.labelMedium)
            }

            OutlinedButton(
                onClick = onForceSilent,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.MusicOff,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Force Silent", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun DiagnosticsPanel(
    diagnosticsList: List<String>,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Active Alarms Diagnostics",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            IconButton(onClick = onRefresh) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh"
                )
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
            )
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (diagnosticsList.isEmpty()) {
                    item {
                        Text(
                            text = "> No active alarms found.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    items(diagnosticsList) { diagnostic ->
                        val isError = diagnostic.contains("ERROR")
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isError) "[WARN] " else "[OK] ",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = diagnostic,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DeveloperToolsDialogContentPreview() {
    SchoolBellTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            DeveloperToolsDialogContent(
                diagnosticsList = listOf(
                    "ID: 1 | 08:00 | Next: 2026-07-13 08:00:00",
                    "ID: 2 | 12:00 | Next: 2026-07-13 12:00:00",
                    "ID: 3 | 15:30 | Next: ERROR"
                ),
                onAddMockSchedules = {},
                onClearAllSchedules = {},
                onTestRing = {},
                onForceSilent = {},
                onRefreshDiagnostics = {}
            )
        }
    }
}
