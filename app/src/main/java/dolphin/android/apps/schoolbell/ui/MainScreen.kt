package dolphin.android.apps.schoolbell.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import dolphin.android.apps.schoolbell.BuildConfig
import dolphin.android.apps.schoolbell.R
import dolphin.android.apps.schoolbell.data.Schedule
import dolphin.android.apps.schoolbell.service.BellRingService
import dolphin.android.apps.schoolbell.ui.theme.SchoolBellTheme

@Composable
fun MainScreen(
    onAdd: () -> Unit,
    onEdit: (Schedule) -> Unit,
    viewModel: MainViewModel = viewModel(factory = MainViewModel.Factory),
    onTestBell: () -> Unit = {}
) {
    val schedules by viewModel.schedules.collectAsState()
    val masterEnabled by viewModel.masterSwitchEnabled.collectAsState()
    val useCustomBell by viewModel.useCustomBell.collectAsState()
    val permissionsState by viewModel.permissionsState.collectAsState()
    val showBatteryWarningSnackbar by viewModel.showBatteryWarningSnackbar.collectAsState()
    val context = LocalContext.current

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        viewModel.checkPermissions()
    }

    val snackbarHostState = remember { SnackbarHostState() }
    var showBatteryDialog by remember { mutableStateOf(false) }

    val snackbarMsg = stringResource(R.string.battery_warning_snackbar_message)
    val snackbarAction = stringResource(R.string.battery_warning_snackbar_action)

    LaunchedEffect(showBatteryWarningSnackbar) {
        if (showBatteryWarningSnackbar) {
            val result = snackbarHostState.showSnackbar(
                message = snackbarMsg,
                actionLabel = snackbarAction,
                duration = SnackbarDuration.Long
            )
            if (result == SnackbarResult.ActionPerformed) {
                showBatteryDialog = true
            }
            viewModel.dismissBatteryWarningSnackbar()
        }
    }

    if (showBatteryDialog) {
        BatteryOptimizationDialog(
            onDismiss = { neverShowAgain ->
                if (neverShowAgain) {
                    viewModel.setIgnoreBatteryWarningPermanently(true)
                }
                showBatteryDialog = false
            },
            onGoToSettings = { neverShowAgain ->
                if (neverShowAgain) {
                    viewModel.setIgnoreBatteryWarningPermanently(true)
                }
                showBatteryDialog = false
                try {
                    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                    context.startActivity(intent)
                } catch (_: Exception) {
                    val intent = Intent(Settings.ACTION_SETTINGS)
                    context.startActivity(intent)
                }
            }
        )
    }

    MainContent(
        schedules = schedules,
        masterEnabled = masterEnabled,
        useCustomBell = useCustomBell,
        permissionsState = permissionsState,
        snackbarHostState = snackbarHostState,
        onToggleMaster = { viewModel.toggleMasterSwitch(it) },
        onToggleCustomBell = { viewModel.toggleUseCustomBell(it) },
        onAdd = onAdd,
        onEdit = onEdit,
        onToggleSchedule = { schedule, enabled -> viewModel.toggleSchedule(schedule, enabled) },
        onDeleteSchedule = { viewModel.deleteSchedule(it) },
        onTestBell = onTestBell,
        onAddMockSchedules = { viewModel.insertMockSchedules() },
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainContent(
    schedules: List<Schedule>,
    masterEnabled: Boolean,
    useCustomBell: Boolean,
    permissionsState: MainViewModel.PermissionsState,
    snackbarHostState: SnackbarHostState,
    onToggleMaster: (Boolean) -> Unit,
    onToggleCustomBell: (Boolean) -> Unit,
    onAdd: () -> Unit,
    onEdit: (Schedule) -> Unit,
    onToggleSchedule: (Schedule, Boolean) -> Unit,
    onDeleteSchedule: (Schedule) -> Unit,
    onTestBell: () -> Unit,
    onAddMockSchedules: () -> Unit,
    onRequestNotification: () -> Unit,
    onRequestExactAlarm: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.main_title), fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (BuildConfig.DEBUG) {
                    Surface(
                        modifier = Modifier
                            .size(48.dp)
                            .combinedClickable(
                                onClick = onTestBell,
                                onLongClick = onAddMockSchedules
                            ),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        tonalElevation = 6.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.BugReport,
                                contentDescription = stringResource(R.string.main_test_ringing)
                            )
                        }
                    }
                }

                FloatingActionButton(
                    onClick = onAdd,
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.main_add_schedule))
                }
            }
        }
    )
    { innerPadding ->
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
                            stringResource(R.string.main_empty_list),
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
                text = stringResource(
                    R.string.app_version,
                    BuildConfig.VERSION_NAME
                ) + if (BuildConfig.DEBUG) "-debug" else "",
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
    onToggleCustomBell: (Boolean) -> Unit,
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
            // Header Row
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

            if (shouldBeExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(
                    color = if (masterEnabled)
                        MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f)
                    else
                        MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.1f)
                )
                Spacer(modifier = Modifier.height(12.dp))

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

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = if (masterEnabled)
                        MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f)
                    else
                        MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.1f)
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
            Column(
                modifier = Modifier
                    .weight(1f)
                    .alpha(if (schedule.isActive) 1f else 0.45f)
            ) {
                Text(
                    text = schedule.formattedTime(),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = formatDays(schedule.daysOfWeek),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = schedule.label,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.main_delete),
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

@Composable
private fun formatDays(days: String): String {
    val dayMap = mapOf(
        "1" to stringResource(R.string.day_mon),
        "2" to stringResource(R.string.day_tue),
        "3" to stringResource(R.string.day_wed),
        "4" to stringResource(R.string.day_thu),
        "5" to stringResource(R.string.day_fri),
        "6" to stringResource(R.string.day_sat),
        "7" to stringResource(R.string.day_sun)
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
            snackbarHostState = remember { SnackbarHostState() },
            onToggleMaster = {},
            onToggleCustomBell = {},
            onAdd = {},
            onEdit = {},
            onToggleSchedule = { _, _ -> },
            onDeleteSchedule = {},
            onTestBell = {},
            onAddMockSchedules = {},
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
            snackbarHostState = remember { SnackbarHostState() },
            onToggleMaster = {},
            onToggleCustomBell = {},
            onAdd = {},
            onEdit = {},
            onToggleSchedule = { _, _ -> },
            onDeleteSchedule = {},
            onTestBell = {},
            onAddMockSchedules = {},
            onRequestNotification = {},
            onRequestExactAlarm = {}
        )
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
                onToggleCustomBell = {}
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
                onToggleCustomBell = {}
            )
        }
    }
}
