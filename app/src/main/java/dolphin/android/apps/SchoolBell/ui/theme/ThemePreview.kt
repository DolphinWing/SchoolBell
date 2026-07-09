package dolphin.android.apps.SchoolBell.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dolphin.android.apps.SchoolBell.R

/**
 * Preview function to visualize the vibrant Material 3 color scheme
 * in both Light and Dark modes, along with the App Icon.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeContent(title: String) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {}) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("App Icon Preview", style = MaterialTheme.typography.titleLarge)
            AppIconPreview()

            HorizontalDivider()

            Text("Buttons", style = MaterialTheme.typography.titleLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {}) { Text("Primary") }
                FilledTonalButton(onClick = {}) { Text("Tonal") }
            }
            OutlinedButton(onClick = {}) { Text("Outlined") }

            HorizontalDivider()

            Text("Cards", style = MaterialTheme.typography.titleLarge)
            ElevatedCard(
                modifier = Modifier.fillMaxWidth().height(100.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text("Elevated Card")
                }
            }

            HorizontalDivider()

            Text("Controls", style = MaterialTheme.typography.titleLarge)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Switch Control")
                Spacer(Modifier.width(16.dp))
                Switch(checked = true, onCheckedChange = {})
            }
        }
    }
}

/**
 * A Composable that mimics the look of the Adaptive App Icon
 */
@Composable
fun AppIconPreview() {
    Box(
        modifier = Modifier
            .size(108.dp)
            .clip(CircleShape)
            .background(Color(0xFF0061A4)), // Electric Blue Background
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_bell_foreground),
            contentDescription = "App Icon Foreground",
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Composable
fun ThemeColorPreviewLight() {
    SchoolBellTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            ThemeContent("Light Theme")
        }
    }
}

@Preview(showBackground = true, name = "Dark Mode")
@Composable
fun ThemeColorPreviewDark() {
    SchoolBellTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            ThemeContent("Dark Theme")
        }
    }
}
