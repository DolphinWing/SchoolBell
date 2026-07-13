package dolphin.android.apps.schoolbell.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import dolphin.android.apps.schoolbell.BuildConfig
import dolphin.android.apps.schoolbell.R
import dolphin.android.apps.schoolbell.data.Schedule
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

    var developerClickCount by remember { mutableIntStateOf(0) }
    var lastClickTime by remember { mutableLongStateOf(0L) }
    var showDevDialog by remember { mutableStateOf(false) }

    if (showDevDialog) {
        DeveloperToolsDialog(
            onDismiss = { showDevDialog = false },
            onAddMockSchedules = { viewModel.insertMockSchedules() },
            onClearAllSchedules = { viewModel.clearAllSchedules() },
            getDiagnostics = { viewModel.getDiagnosticsInfo() }
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
        onVersionClick = {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime < 2000) {
                developerClickCount++
            } else {
                developerClickCount = 1
            }
            lastClickTime = currentTime

            if (developerClickCount >= 10) {
                developerClickCount = 0
                showDevDialog = true
            }
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
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkPermissions(checkBattery = false)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
    onVersionClick: () -> Unit,
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
            FloatingActionButton(
                onClick = onAdd,
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.main_add_schedule))
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
                onToggleCustomBell = onToggleCustomBell,
                onTestBell = onTestBell
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
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onVersionClick()
                    }
                    .padding(vertical = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
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
            permissionsState = MainViewModel.PermissionsState(
                hasNotificationPermission = true,
                canScheduleExactAlarms = true
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onToggleMaster = {},
            onToggleCustomBell = {},
            onAdd = {},
            onEdit = {},
            onToggleSchedule = { _, _ -> },
            onDeleteSchedule = {},
            onTestBell = {},
            onVersionClick = {},
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
            permissionsState = MainViewModel.PermissionsState(
                hasNotificationPermission = true,
                canScheduleExactAlarms = true
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onToggleMaster = {},
            onToggleCustomBell = {},
            onAdd = {},
            onEdit = {},
            onToggleSchedule = { _, _ -> },
            onDeleteSchedule = {},
            onTestBell = {},
            onVersionClick = {},
            onRequestNotification = {},
            onRequestExactAlarm = {}
        )
    }
}
