package com.example.data.model

enum class JarvisState(val label: String) {
    IDLE("ONLINE"),
    LISTENING("LISTENING..."),
    THINKING("THINKING..."),
    SPEAKING("SPEAKING..."),
    ERROR("SYSTEM ALERT")
}

enum class MessageSender {
    USER,
    JARVIS,
    SYSTEM
}

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val sender: MessageSender,
    val timestamp: Long = System.currentTimeMillis(),
    val isStreaming: Boolean = false,
    val isError: Boolean = false,
    val executedAction: String? = null
)

data class DeviceStatus(
    val batteryLevel: Int = -1,
    val isCharging: Boolean = false,
    val batteryTemperature: Float? = null,
    val usedRamGb: Float = 0f,
    val totalRamGb: Float = 0f,
    val freeRamGb: Float = 0f,
    val ramUsagePercent: Int = 0,
    val usedStorageGb: Float = 0f,
    val totalStorageGb: Float = 0f,
    val storageUsagePercent: Int = 0,
    val networkType: String = "Unknown",
    val isNetworkConnected: Boolean = false,
    val networkDetails: String = "",
    val cpuStatus: String = "Active"
)

enum class ActionStatus {
    SUCCESS,
    UNAVAILABLE,
    PERMISSION_REQUIRED,
    SYSTEM_ACTION_REQUIRED
}

data class ActionFeedback(
    val status: ActionStatus,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class AppInfo(
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean = false
)

data class UserSettings(
    val wakeWordEnabled: Boolean = false,
    val ttsEnabled: Boolean = true,
    val performanceMode: Boolean = false,
    val neonIntensity: Float = 1.0f,
    val notificationReadingEnabled: Boolean = false,
    val customApiKey: String = "",
    val handsFreeMode: Boolean = false
)
