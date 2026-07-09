package dolphin.android.apps.schoolbell.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import dolphin.android.apps.schoolbell.MainActivity
import dolphin.android.apps.schoolbell.R
import dolphin.android.apps.schoolbell.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BellRingService : Service() {

    companion object {
        private const val TAG = "BellRingService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "school_bell_ringing_channel"
        const val ACTION_STOP = "dolphin.android.apps.schoolbell.ACTION_STOP"
    }

    private var mediaPlayer: MediaPlayer? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private val handler = Handler(Looper.getMainLooper())
    private val autoStopRunnable = Runnable {
        Log.d(TAG, "Auto-stop timeout reached. Silencing.")
        stopSelf()
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate")
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service onStartCommand action=${intent?.action}")

        if (intent?.action == ACTION_STOP) {
            Log.d(TAG, "Stop action received. Stopping service.")
            stopSelf()
            return START_NOT_STICKY
        }

        val scheduleId = intent?.getIntExtra("SCHEDULE_ID", -1) ?: -1
        val label = intent?.getStringExtra("SCHEDULE_LABEL") ?: "School Bell"

        // Build notification
        val notification = buildNotification(label)

        // Start Foreground
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        // Play Sound based on settings
        serviceScope.launch {
            val settings = SettingsRepository(this@BellRingService)
            val useCustom = settings.useCustomBellFlow.first()
            playSound(useCustom)
        }

        // Auto-stop after 15 seconds to prevent continuous ringing
        handler.removeCallbacks(autoStopRunnable)
        handler.postDelayed(autoStopRunnable, 15000)

        return START_NOT_STICKY
    }

    private fun playSound(useCustom: Boolean) {
        if (mediaPlayer != null) {
            return // Already playing
        }

        try {
            val audioUri = if (useCustom) {
                "android.resource://${packageName}/${R.raw.off_class}".toUri()
            } else {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            }

            mediaPlayer = MediaPlayer().apply {
                if (useCustom) {
                    setDataSource(this@BellRingService, audioUri)
                } else {
                    setDataSource(this@BellRingService, audioUri!!)
                }
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                isLooping = true
                setWakeMode(this@BellRingService, PowerManager.PARTIAL_WAKE_LOCK)
                prepare()
                start()
            }
            Log.d(TAG, "MediaPlayer playing successfully (custom=$useCustom)")
        } catch (e: Exception) {
            Log.e(TAG, "Error playing sound", e)
        }
    }

    private fun stopSound() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping/releasing MediaPlayer", e)
        } finally {
            mediaPlayer = null
        }
    }

    private fun buildNotification(label: String): Notification {
        val stopIntent = Intent(this, BellRingService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentIntent = Intent(this, MainActivity::class.java)
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            0,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("School Bell Active!")
            .setContentText(label)
            .setSubText("Class schedule alert")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(false)
            .setFullScreenIntent(contentPendingIntent, true) // crucial for heads-up and lock screen
            .setContentIntent(contentPendingIntent)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "STOP",
                stopPendingIntent
            )
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "School Bell Ringing",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel used for playing school bell ring alarms"
                enableVibration(true)
                setBypassDnd(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service onDestroy")
        handler.removeCallbacks(autoStopRunnable)
        stopSound()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
