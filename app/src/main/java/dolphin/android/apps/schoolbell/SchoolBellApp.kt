package dolphin.android.apps.schoolbell

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

/**
 * Custom Application class for SchoolBell.
 * Handles library initializations like Timber.
 */
class SchoolBellApp : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            // In Debug, we want all logs
            Timber.plant(Timber.DebugTree())
        } else {
            // In Release, we only keep important logs and forward them to Crashlytics
            Timber.plant(CrashlyticsTree())
        }
    }

    /**
     * A custom Timber Tree for Release builds.
     * It filters out non-critical logs (VERBOSE/DEBUG) and forwards INFO+ logs to Crashlytics.
     */
    private class CrashlyticsTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            // Only log INFO, WARN, and ERROR in release builds
            if (priority == Log.VERBOSE || priority == Log.DEBUG) {
                return
            }

            // Map priority to char for clean breadcrumbs format
            val priorityChar = when (priority) {
                Log.INFO -> "I"
                Log.WARN -> "W"
                Log.ERROR -> "E"
                Log.ASSERT -> "A"
                else -> "U"
            }

            // Log to Crashlytics internal circular buffer (up to 64KB)
            val logMessage = "$priorityChar/${tag ?: "SchoolBell"}: $message"
            try {
                FirebaseCrashlytics.getInstance().log(logMessage)

                // Log to Logcat too for release troubleshooting via USB
                Log.println(priority, tag, message)

                if (t != null) {
                    if (priority == Log.ERROR || priority == Log.ASSERT) {
                        FirebaseCrashlytics.getInstance().recordException(t)
                    }
                }
            } catch (e: Exception) {
                // Fallback in case Firebase isn't initialized or crashes
                @SuppressLint("LogNotTimber")
                Log.e("CrashlyticsTree", "Failed to log to Crashlytics", e)
            }
        }
    }
}
