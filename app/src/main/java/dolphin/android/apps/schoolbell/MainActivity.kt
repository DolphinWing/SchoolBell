package dolphin.android.apps.schoolbell

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import dolphin.android.apps.schoolbell.ui.MainViewModel
import dolphin.android.apps.schoolbell.ui.theme.SchoolBellTheme

class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    private var isFromAlarm = false

    private val alarmStoppedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (isFromAlarm) {
                timber.log.Timber.tag(TAG).i("Alarm stopped without user interaction. Finishing activity.")
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parseIntent(intent)

        val filter = IntentFilter(BellRingService.ACTION_ALARM_STOPPED)
        ContextCompat.registerReceiver(this, alarmStoppedReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)

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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        parseIntent(intent)
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        if (isFromAlarm) {
            isFromAlarm = false
            timber.log.Timber.tag(TAG).d("User interacted with the app. Auto-close disabled.")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(alarmStoppedReceiver)
        } catch (e: Exception) {
            timber.log.Timber.tag(TAG).e(e, "Error unregistering alarmStoppedReceiver")
        }
    }

    private fun parseIntent(intent: Intent?) {
        isFromAlarm = intent?.getBooleanExtra("FROM_ALARM", false) ?: false
        timber.log.Timber.tag(TAG).d("parseIntent: isFromAlarm=$isFromAlarm")
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

    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as androidx.lifecycle.ViewModelStoreOwner
    val sharedViewModel: MainViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        viewModelStoreOwner = activity,
        factory = MainViewModel.Factory
    )

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
                    onTestBell = onTestBell,
                    viewModel = sharedViewModel
                )
            }

            is EditScheduleKey -> NavEntry(
                key = key,
                metadata = ListDetailSceneStrategy.detailPane()
            ) {
                EditScheduleScreen(
                    scheduleId = key.scheduleId,
                    onDismiss = { if (backStack.size > 1) backStack.removeAt(backStack.size - 1) },
                    viewModel = sharedViewModel
                )
            }

            else -> NavEntry(key) { }
        }
    }
}
