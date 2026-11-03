package com.example.service

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class JarvisNotificationItem(
    val id: String,
    val packageName: String,
    val appName: String,
    val title: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

class JarvisNotificationListenerService : NotificationListenerService() {

    companion object {
        private val _notifications = MutableStateFlow<List<JarvisNotificationItem>>(emptyList())
        val notifications: StateFlow<List<JarvisNotificationItem>> = _notifications.asStateFlow()

        private val _isConnected = MutableStateFlow(false)
        val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

        var isReadingEnabled: Boolean = false

        fun isPermissionGranted(context: Context): Boolean {
            val enabledListeners = Settings.Secure.getString(
                context.contentResolver,
                "enabled_notification_listeners"
            ) ?: return false
            val myComponent = ComponentName(context, JarvisNotificationListenerService::class.java).flattenToString()
            return enabledListeners.contains(myComponent)
        }

        fun clearNotifications() {
            _notifications.value = emptyList()
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        _isConnected.value = true
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        _isConnected.value = false
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null || !isReadingEnabled) return

        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return

        // Skip ongoing/persistent or media controls
        val isOngoing = (notification.flags and Notification.FLAG_ONGOING_EVENT) != 0
        if (isOngoing) return

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
            ?: ""

        if (title.isBlank() && text.isBlank()) return

        val pm = packageManager
        val appName = try {
            val appInfo = pm.getApplicationInfo(sbn.packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            sbn.packageName
        }

        val item = JarvisNotificationItem(
            id = "${sbn.id}_${sbn.postTime}",
            packageName = sbn.packageName,
            appName = appName,
            title = title,
            text = text,
            timestamp = sbn.postTime
        )

        val updatedList = listOf(item) + _notifications.value.take(29)
        _notifications.value = updatedList
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // notification dismissed
    }
}
