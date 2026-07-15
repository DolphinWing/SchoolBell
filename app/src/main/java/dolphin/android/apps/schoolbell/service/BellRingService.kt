package dolphin.android.apps.schoolbell.service

import android.annotation.SuppressLint
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
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import dolphin.android.apps.schoolbell.MainActivity
import dolphin.android.apps.schoolbell.R
import dolphin.android.apps.schoolbell.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber

class BellRingService : Service() {

    companion object {
        private const val TAG = "BellRingService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "school_bell_ringing_channel"
        const val ACTION_STOP = "dolphin.android.apps.schoolbell.ACTION_STOP"
        const val ACTION_ALARM_STOPPED = "dolphin.android.apps.schoolbell.ACTION_ALARM_STOPPED"
    }

    private var mediaPlayer: MediaPlayer? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private val handler = Handler(Looper.getMainLooper())
    private val autoStopRunnable = Runnable {
        Timber.tag(TAG).i("Auto-stop timeout reached. Silencing.")
        stopSelf()
    }

    override fun onCreate() {
        super.onCreate()
        Timber.tag(TAG).d("Service onCreate")
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.tag(TAG).d("Service onStartCommand action=${intent?.action}")

        if (intent?.action == ACTION_STOP) {
            Timber.tag(TAG).i("Stop action received. Stopping service.")
            stopSelf()
            return START_NOT_STICKY
        }

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
            val vol = settings.volumeFlow.first()
            playSound(useCustom, vol)
        }

        return START_NOT_STICKY
    }

    private fun playSound(useCustom: Boolean, volume: Float) {
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
                isLooping = !useCustom // Loop only system alarm, custom bell plays once
                setWakeMode(this@BellRingService, PowerManager.PARTIAL_WAKE_LOCK)
                
                if (useCustom) {
                    setVolume(volume, volume)
                }
                
                prepare()
                start()

                if (useCustom) {
                    setOnCompletionListener {
                        Timber.tag(TAG).d("Playback completed. Stopping service.")
                        stopSelf()
                    }
                }
            }

            val duration = try {
                mediaPlayer?.duration ?: -1
            } catch (e: Exception) {
                -1
            }

            val watchdogDelay = if (useCustom && duration > 0) {
                duration.toLong() + 2000L // Track duration + 2s buffer
            } else {
                15000L // 15s standard fallback
            }

            // Schedule auto-stop watchdog on Main thread
            handler.post {
                handler.removeCallbacks(autoStopRunnable)
                handler.postDelayed(autoStopRunnable, watchdogDelay)
            }

            Timber.tag(TAG).i("MediaPlayer playing successfully (custom=$useCustom, watchdog=${watchdogDelay}ms)")
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error playing sound")
            // If error, stop service quickly
            handler.post {
                handler.removeCallbacks(autoStopRunnable)
                handler.postDelayed(autoStopRunnable, 2000L)
            }
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
            Timber.tag(TAG).e(e, "Error stopping/releasing MediaPlayer")
        } finally {
            mediaPlayer = null
        }
    }

    @SuppressLint("FullScreenIntentPolicy")
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

        val contentIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("FROM_ALARM", true)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            0,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_bell)
            .setContentTitle(getString(R.string.notif_title))
            .setContentText(label)
            .setSubText(getString(R.string.notif_desc))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(false)
            .setFullScreenIntent(contentPendingIntent, true) // crucial for heads-up and lock screen
            .setContentIntent(contentPendingIntent)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                getString(R.string.notif_stop),
                stopPendingIntent
            )
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notif_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.notif_channel_desc)
                setBypassDnd(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.tag(TAG).d("Service onDestroy")
        handler.removeCallbacks(autoStopRunnable)
        stopSound()
        try {
            sendBroadcast(Intent(ACTION_ALARM_STOPPED))
            Timber.tag(TAG).d("Sent ACTION_ALARM_STOPPED broadcast")
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error sending ACTION_ALARM_STOPPED broadcast")
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
