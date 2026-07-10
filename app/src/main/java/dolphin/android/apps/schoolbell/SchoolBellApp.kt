package dolphin.android.apps.schoolbell

import android.app.Application
import android.util.Log
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
            // In Release, we only keep important logs
            Timber.plant(ReleaseTree())
        }
    }

    /**
     * A custom Timber Tree for Release builds.
     * It filters out non-critical logs and can be expanded to send logs to Crashlytics.
     */
    private class ReleaseTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            // Only log INFO, WARN, and ERROR in release builds
            if (priority == Log.VERBOSE || priority == Log.DEBUG) {
                return
            }

            // In a real app, you might send errors to Crashlytics here:
            // if (priority == Log.ERROR && t != null) {
            //     FirebaseCrashlytics.getInstance().recordException(t)
            // }
            
            // For now, we still log to Logcat for "important" release insights
            Log.println(priority, tag, message)
        }
    }
}
