package dolphin.android.apps.schoolbell

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldDefaults
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dolphin.android.apps.schoolbell.service.BellRingService
import dolphin.android.apps.schoolbell.ui.EditScheduleKey
import dolphin.android.apps.schoolbell.ui.EditScheduleScreen
import dolphin.android.apps.schoolbell.ui.MainKey
import dolphin.android.apps.schoolbell.ui.MainScreen
import dolphin.android.apps.schoolbell.ui.theme.SchoolBellTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (BuildConfig.DEBUG) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        enableEdgeToEdge()
        setContent {
            SchoolBellTheme {
                AppNavigation(onTestBell = { debugTestBell() })
            }
        }
    }

    private fun debugTestBell() {
        val label = getString(R.string.main_test_ringing)
        val intent = Intent(this, BellRingService::class.java).apply {
            putExtra("SCHEDULE_LABEL", label)
        }
        ContextCompat.startForegroundService(this, intent)
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun AppNavigation(onTestBell: () -> Unit = {}) {
    val backStack = rememberNavBackStack(MainKey as NavKey)
    val saveableStateDecorator = rememberSaveableStateHolderNavEntryDecorator<NavKey>()
    val viewModelDecorator = rememberViewModelStoreNavEntryDecorator<NavKey>()

    val sceneStrategy = ListDetailSceneStrategy<NavKey>(
        shouldHandleSinglePaneLayout = false,
        backNavigationBehavior = BackNavigationBehavior.PopUntilScaffoldValueChange,
        directive = PaneScaffoldDirective.Default,
        adaptStrategies = ListDetailPaneScaffoldDefaults.adaptStrategies(),
        paneExpansionDragHandle = null,
        paneExpansionState = null
    )

    NavDisplay(
        backStack = backStack,
        entryDecorators = listOf(saveableStateDecorator, viewModelDecorator),
        sceneStrategies = listOf(sceneStrategy)
    ) { key ->
        when (key) {
            is MainKey -> NavEntry(
                key = key,
                metadata = ListDetailSceneStrategy.listPane()
            ) {
                MainScreen(
                    onAdd = { backStack.add(EditScheduleKey()) },
                    onEdit = { schedule -> backStack.add(EditScheduleKey(schedule.id)) },
                    onTestBell = onTestBell
                )
            }

            is EditScheduleKey -> NavEntry(
                key = key,
                metadata = ListDetailSceneStrategy.detailPane()
            ) {
                EditScheduleScreen(
                    scheduleId = key.scheduleId,
                    onDismiss = { if (backStack.size > 1) backStack.removeAt(backStack.size - 1) }
                )
            }

            else -> NavEntry(key) { }
        }
    }
}
