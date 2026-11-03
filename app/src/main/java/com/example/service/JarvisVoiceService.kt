package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.MainActivity
import com.example.domain.voice.DefaultWakeWordEngine
import com.example.domain.voice.WakeWordEngine

class JarvisVoiceService : Service() {

    companion object {
        const val CHANNEL_ID = "jarvis_voice_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "com.example.jarvis.START_VOICE_SERVICE"
        const val ACTION_STOP = "com.example.jarvis.STOP_VOICE_SERVICE"
        const val ACTION_WAKE_DETECTED = "com.example.jarvis.WAKE_WORD_DETECTED"

        var isServiceActive = false
            private set

        fun start(context: Context) {
            val intent = Intent(context, JarvisVoiceService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, JarvisVoiceService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    private var wakeWordEngine: WakeWordEngine? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopForegroundService()
                return START_NOT_STICKY
            }
            ACTION_START -> {
                startForegroundServiceSafely()
            }
            else -> {
                startForegroundServiceSafely()
            }
        }
        return START_STICKY
    }

    private fun startForegroundServiceSafely() {
        try {
            val notification = buildNotification("Listening for 'Hey JARVIS'...")
            val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                } else {
                    0
                }
            } else {
                0
            }

            ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, type)
            isServiceActive = true

            if (wakeWordEngine == null) {
                wakeWordEngine = DefaultWakeWordEngine(this).apply {
                    start {
                        onWakeDetected()
                    }
                }
            }
        } catch (e: Exception) {
            // Permission or security exception
            stopSelf()
        }
    }

    private fun onWakeDetected() {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("EXTRA_START_LISTENING", true)
        }
        startActivity(launchIntent)

        val broadcast = Intent(ACTION_WAKE_DETECTED).apply {
            setPackage(packageName)
        }
        sendBroadcast(broadcast)
    }

    private fun stopForegroundService() {
        isServiceActive = false
        wakeWordEngine?.stop()
        wakeWordEngine?.destroy()
        wakeWordEngine = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "JARVIS Wake Word Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Foreground service maintaining JARVIS voice keyword detection"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(statusText: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = Intent(this, JarvisVoiceService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("JARVIS AI Assistant Active")
            .setContentText(statusText)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Deactivate", stopPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        stopForegroundService()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
