package dolphin.android.apps.SchoolBell.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import dolphin.android.apps.SchoolBell.data.Schedule

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onAdd: () -> Unit,
    onEdit: (Schedule) -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val schedules by viewModel.schedules.collectAsState()
    val masterEnabled by viewModel.masterSwitchEnabled.collectAsState()
    val permissionsState by viewModel.permissionsState.collectAsState()
    val context = LocalContext.current

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        viewModel.checkPermissions()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("School Bell", fontWeight = FontWeight.Bold) },
                actions = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (masterEnabled) "ON" else "OFF",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (masterEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                        Switch(
                            checked = masterEnabled,
                            onCheckedChange = { viewModel.toggleMasterSwitch(it) },
                            modifier = Modifier.padding(horizontal = 8.dp),
                            thumbContent = {
                                Icon(
                                    imageVector = if (masterEnabled) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                )
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAdd,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Schedule")
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
            }

            if (schedules.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "No schedules added yet.\nTap + to create one.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(schedules, key = { it.id }) { schedule ->
                        ScheduleCard(
                            schedule = schedule,
                            onToggle = { viewModel.toggleSchedule(schedule, it) },
                            onDelete = { viewModel.deleteSchedule(schedule) },
                            onClick = { onEdit(schedule) }
                        )
                    }
                }
            }
        }
    }

    // Check permissions when returning to the screen (e.g., from settings)
    DisposableEffect(Unit) {
        viewModel.checkPermissions()
        onDispose { }
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
                    "權限要求",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            if (!permissionsState.hasNotificationPermission) {
                Text(
                    "請開啟通知權限以確保鈴聲能正常顯示通知。",
                    style = MaterialTheme.typography.bodyMedium
                )
                TextButton(onClick = onRequestNotification) {
                    Text("開啟通知權限")
                }
            }
            
            if (!permissionsState.canScheduleExactAlarms) {
                Text(
                    "請允許精確鬧鐘權限，以確保鈴聲能在正確的時間響起。",
                    style = MaterialTheme.typography.bodyMedium
                )
                TextButton(onClick = onRequestExactAlarm) {
                    Text("開啟精確鬧鐘權限")
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

@Composable
fun NumberPicker(value: Int, onValueChange: (Int) -> Unit, range: IntRange, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall)
        OutlinedTextField(
            value = value.toString(),
            onValueChange = { 
                val newVal = it.toIntOrNull()
                if (newVal != null && newVal in range) onValueChange(newVal)
            },
            modifier = Modifier.width(64.dp),
            singleLine = true
        )
    }
}

private fun formatDays(days: String): String {
    val dayMap = mapOf(
        "1" to "Mon", "2" to "Tue", "3" to "Wed", "4" to "Thu", 
        "5" to "Fri", "6" to "Sat", "7" to "Sun"
    )
    return days.split(",").mapNotNull { dayMap[it.trim()] }.joinToString(", ")
}
