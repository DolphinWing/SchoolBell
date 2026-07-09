package dolphin.android.apps.SchoolBell.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
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

    var hour by remember(schedule) { mutableIntStateOf(schedule?.hour ?: 8) }
    var minute by remember(schedule) { mutableIntStateOf(schedule?.minute ?: 0) }
    var label by remember(schedule) { mutableStateOf(schedule?.label ?: "") }
    val days = schedule?.daysOfWeek ?: "1,2,3,4,5"

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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NumberPicker(value = hour, onValueChange = { hour = it }, range = 0..23, label = "Hour")
                Text(":", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(horizontal = 8.dp))
                NumberPicker(value = minute, onValueChange = { minute = it }, range = 0..59, label = "Min")
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (schedule == null) {
                        viewModel.addSchedule(hour, minute, label, days)
                    } else {
                        viewModel.updateSchedule(schedule.copy(hour = hour, minute = minute, label = label))
                    }
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (scheduleId == null) "SAVE" else "UPDATE")
            }
        }
    }
}
