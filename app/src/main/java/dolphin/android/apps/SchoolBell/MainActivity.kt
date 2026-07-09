package dolphin.android.apps.SchoolBell

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldDefaults
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import dolphin.android.apps.SchoolBell.ui.EditScheduleKey
import dolphin.android.apps.SchoolBell.ui.EditScheduleScreen
import dolphin.android.apps.SchoolBell.ui.MainKey
import dolphin.android.apps.SchoolBell.ui.MainScreen
import dolphin.android.apps.SchoolBell.ui.theme.SchoolBellTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SchoolBellTheme {
                AppNavigation()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun AppNavigation() {
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
        sceneStrategy = sceneStrategy
    ) { key ->
        when (key) {
            is MainKey -> NavEntry(
                key = key,
                metadata = ListDetailSceneStrategy.listPane()
            ) {
                MainScreen(
                    onAdd = { backStack.add(EditScheduleKey()) },
                    onEdit = { schedule -> backStack.add(EditScheduleKey(schedule.id)) }
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
