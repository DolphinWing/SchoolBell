package dolphin.android.apps.schoolbell.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import dolphin.android.apps.schoolbell.ui.theme.SchoolBellTheme
import java.util.Locale

@Composable
fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Select Ring Time",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(bottom = 20.dp)
                )
                content()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    TextButton(onClick = onConfirm) { Text("Confirm") }
                }
            }
        }
    }
}

@Composable
fun EditScheduleScreen(
    scheduleId: Int?,
    onDismiss: () -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val schedules by viewModel.schedules.collectAsState()
    val schedule = remember(scheduleId, schedules) {
        viewModel.getSchedule(scheduleId)
    }

    EditScheduleContent(
        scheduleId = scheduleId,
        initialLabel = schedule?.label ?: "",
        initialHour = schedule?.hour ?: 8,
        initialMinute = schedule?.minute ?: 0,
        daysOfWeek = schedule?.daysOfWeek ?: "1,2,3,4,5",
        onSave = { hour, minute, label, days ->
            if (schedule == null) {
                viewModel.addSchedule(hour, minute, label, days)
            } else {
                viewModel.updateSchedule(schedule.copy(hour = hour, minute = minute, label = label, daysOfWeek = days))
            }
            onDismiss()
        },
        onDismiss = onDismiss
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScheduleContent(
    scheduleId: Int?,
    initialLabel: String,
    initialHour: Int,
    initialMinute: Int,
    daysOfWeek: String,
    onSave: (hour: Int, minute: Int, label: String, days: String) -> Unit,
    onDismiss: () -> Unit
) {
    var label by remember(initialLabel) { mutableStateOf(initialLabel) }

    // Manage days of week selection state
    val initialDaysSet = remember(daysOfWeek) {
        daysOfWeek.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
    }
    var selectedDays by remember(initialDaysSet) { mutableStateOf(initialDaysSet) }

    val timePickerState = key(initialHour, initialMinute) {
        rememberTimePickerState(
            initialHour = initialHour,
            initialMinute = initialMinute,
            is24Hour = true
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (scheduleId == null) "Add Schedule" else "Edit Schedule") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text("Label") },
                modifier = Modifier.fillMaxWidth()
            )

            var showTimePicker by remember { mutableStateOf(false) }

            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showTimePicker = true }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = String.format(
                            Locale.getDefault(),
                            "%02d:%02d",
                            timePickerState.hour,
                            timePickerState.minute
                        ),
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    Text("Set Time", style = MaterialTheme.typography.labelLarge)
                }
            }

            if (showTimePicker) {
                TimePickerDialog(
                    onDismiss = { showTimePicker = false },
                    onConfirm = { showTimePicker = false }
                ) {
                    TimePicker(state = timePickerState)
                }
            }

            Text("Repeat", style = MaterialTheme.typography.titleSmall)
            DaysOfWeekSelector(
                selectedDays = selectedDays,
                onToggleDay = { day ->
                    selectedDays = if (selectedDays.contains(day)) {
                        selectedDays - day
                    } else {
                        selectedDays + day
                    }
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val daysString = selectedDays.toList().sorted().joinToString(",")
                    onSave(timePickerState.hour, timePickerState.minute, label, daysString)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (scheduleId == null) "Save" else "Update")
            }
        }
    }
}

@Composable
fun DaysOfWeekSelector(
    selectedDays: Set<Int>,
    onToggleDay: (Int) -> Unit
) {
    val days = listOf(7, 1, 2, 3, 4, 5, 6)
    val dayLabels = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        days.zip(dayLabels).forEach { (day, label) ->
            val isSelected = selectedDays.contains(day)
            Surface(
                modifier = Modifier
                    .size(40.dp)
                    .clickable { onToggleDay(day) },
                shape = androidx.compose.foundation.shape.CircleShape,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(label, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Composable
fun EditScheduleScreenPreview() {
    SchoolBellTheme {
        EditScheduleContent(
            scheduleId = null,
            initialLabel = "Example Bell",
            initialHour = 12,
            initialMinute = 30,
            daysOfWeek = "1,2,3,4,5",
            onSave = { _, _, _, _ -> },
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true, name = "Dark Mode")
@Composable
fun EditScheduleScreenDarkPreview() {
    SchoolBellTheme(darkTheme = true) {
        EditScheduleContent(
            scheduleId = null,
            initialLabel = "Example Bell",
            initialHour = 12,
            initialMinute = 30,
            daysOfWeek = "1,2,3,4,5",
            onSave = { _, _, _, _ -> },
            onDismiss = {}
        )
    }
}
