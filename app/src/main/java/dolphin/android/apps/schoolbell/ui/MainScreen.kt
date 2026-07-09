package dolphin.android.apps.schoolbell.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import dolphin.android.apps.schoolbell.BuildConfig
import dolphin.android.apps.schoolbell.data.Schedule
import dolphin.android.apps.schoolbell.service.BellRingService
import dolphin.android.apps.schoolbell.ui.theme.SchoolBellTheme

@Composable
fun MainScreen(
    onAdd: () -> Unit,
    onEdit: (Schedule) -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val schedules by viewModel.schedules.collectAsState()
    val masterEnabled by viewModel.masterSwitchEnabled.collectAsState()
    val useCustomBell by viewModel.useCustomBell.collectAsState()
    val permissionsState by viewModel.permissionsState.collectAsState()
    val context = LocalContext.current

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        viewModel.checkPermissions()
    }

    MainContent(
        schedules = schedules,
        masterEnabled = masterEnabled,
        useCustomBell = useCustomBell,
        permissionsState = permissionsState,
        onToggleMaster = { viewModel.toggleMasterSwitch(it) },
        onToggleCustomBell = { viewModel.toggleUseCustomBell(it) },
        onAdd = onAdd,
        onEdit = onEdit,
        onToggleSchedule = { schedule, enabled -> viewModel.toggleSchedule(schedule, enabled) },
        onDeleteSchedule = { viewModel.deleteSchedule(it) },
        onTestBell = {
            val intent = Intent(context, BellRingService::class.java).apply {
                putExtra("SCHEDULE_LABEL", "Ringing Test")
            }
            ContextCompat.startForegroundService(context, intent)
        },
        onRequestNotification = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        },
        onRequestExactAlarm = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            }
        }
    )

    // Check permissions when returning to the screen (e.g., from settings)
    DisposableEffect(Unit) {
        viewModel.checkPermissions()
        onDispose { }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(
    schedules: List<Schedule>,
    masterEnabled: Boolean,
    useCustomBell: Boolean,
    permissionsState: MainViewModel.PermissionsState,
    onToggleMaster: (Boolean) -> Unit,
    onToggleCustomBell: (Boolean) -> Unit,
    onAdd: () -> Unit,
    onEdit: (Schedule) -> Unit,
    onToggleSchedule: (Schedule, Boolean) -> Unit,
    onDeleteSchedule: (Schedule) -> Unit,
    onTestBell: () -> Unit,
    onRequestNotification: () -> Unit,
    onRequestExactAlarm: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("School Bell", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (BuildConfig.DEBUG) {
                    SmallFloatingActionButton(
                        onClick = onTestBell,
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    ) {
                        Icon(Icons.Default.BugReport, contentDescription = "Test Ringing")
                    }
                }

                FloatingActionButton(
                    onClick = onAdd,
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Schedule")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Permission Warnings
            if (!permissionsState.hasNotificationPermission || !permissionsState.canScheduleExactAlarms) {
                PermissionWarningCard(
                    permissionsState = permissionsState,
                    onRequestNotification = onRequestNotification,
                    onRequestExactAlarm = onRequestExactAlarm
                )
            }

            // Global Settings
            GlobalSettingsCard(
                masterEnabled = masterEnabled,
                useCustomBell = useCustomBell,
                onToggleMaster = onToggleMaster,
                onToggleCustomBell = onToggleCustomBell
            )

            Box(modifier = Modifier.weight(1f)) {
                if (schedules.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "No schedules added yet.\nTap + to start.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(schedules, key = { it.id }) { schedule ->
                            ScheduleCard(
                                schedule = schedule,
                                onToggle = { onToggleSchedule(schedule, it) },
                                onDelete = { onDeleteSchedule(schedule) },
                                onClick = { onEdit(schedule) }
                            )
                        }
                    }
                }
            }

            Text(
                text = "Version ${BuildConfig.VERSION_NAME}${if (BuildConfig.DEBUG) "-debug" else ""}",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun GlobalSettingsCard(
    masterEnabled: Boolean,
    useCustomBell: Boolean,
    onToggleMaster: (Boolean) -> Unit,
    onToggleCustomBell: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Row 1: Master Switch
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
                    Text("Master Switch", style = MaterialTheme.typography.titleMedium)
                    Text(
                        if (masterEnabled) "Enabled" else "Disabled",
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

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f)
            )

            // Row 2: Ringtone Mode
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
                    Text("Ringtone Mode", style = MaterialTheme.typography.titleMedium)
                    Text(
                        if (useCustomBell) "Campus Bell" else "System Alarm",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
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
    }
}

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
                    "Permissions Required",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (!permissionsState.hasNotificationPermission) {
                Text(
                    "Please enable notification permissions to ensure the bell displays correctly.",
                    style = MaterialTheme.typography.bodyMedium
                )
                TextButton(onClick = onRequestNotification) {
                    Text("Enable Notifications")
                }
            }

            if (!permissionsState.canScheduleExactAlarms) {
                Text(
                    "Please allow exact alarm permissions to ensure the bell rings on time.",
                    style = MaterialTheme.typography.bodyMedium
                )
                TextButton(onClick = onRequestExactAlarm) {
                    Text("Enable Exact Alarms")
                }
            }
        }
    }
}

@Composable
fun ScheduleCard(
    schedule: Schedule,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (schedule.isActive)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = schedule.formattedTime(),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = if (schedule.isActive)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = schedule.label,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = formatDays(schedule.daysOfWeek),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }

            Switch(
                checked = schedule.isActive,
                onCheckedChange = onToggle
            )
        }
    }
}

private fun formatDays(days: String): String {
    val dayMap = mapOf(
        "1" to "Mon", "2" to "Tue", "3" to "Wed", "4" to "Thu",
        "5" to "Fri", "6" to "Sat", "7" to "Sun"
    )
    return days.split(",").mapNotNull { dayMap[it.trim()] }.joinToString(", ")
}

@Preview(showBackground = true, name = "Light Mode")
@Composable
fun MainScreenPreview() {
    SchoolBellTheme {
        MainContent(
            schedules = listOf(
                Schedule(id = 1, hour = 8, minute = 0, label = "Period 1", isActive = true, daysOfWeek = "1,2,3,4,5"),
                Schedule(id = 2, hour = 12, minute = 0, label = "Lunch", isActive = false, daysOfWeek = "1,2,3,4,5")
            ),
            masterEnabled = true,
            useCustomBell = true,
            permissionsState = MainViewModel.PermissionsState(true, true),
            onToggleMaster = {},
            onToggleCustomBell = {},
            onAdd = {},
            onEdit = {},
            onToggleSchedule = { _, _ -> },
            onDeleteSchedule = {},
            onTestBell = {},
            onRequestNotification = {},
            onRequestExactAlarm = {}
        )
    }
}

@Preview(showBackground = true, name = "Dark Mode")
@Composable
fun MainScreenDarkPreview() {
    SchoolBellTheme(darkTheme = true) {
        MainContent(
            schedules = listOf(
                Schedule(id = 1, hour = 8, minute = 0, label = "Period 1", isActive = true, daysOfWeek = "1,2,3,4,5"),
                Schedule(id = 2, hour = 12, minute = 0, label = "Lunch", isActive = false, daysOfWeek = "1,2,3,4,5")
            ),
            masterEnabled = true,
            useCustomBell = true,
            permissionsState = MainViewModel.PermissionsState(true, true),
            onToggleMaster = {},
            onToggleCustomBell = {},
            onAdd = {},
            onEdit = {},
            onToggleSchedule = { _, _ -> },
            onDeleteSchedule = {},
            onTestBell = {},
            onRequestNotification = {},
            onRequestExactAlarm = {}
        )
    }
}
