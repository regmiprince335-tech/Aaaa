package com.example.domain.device

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.example.data.model.ActionFeedback
import com.example.data.model.ActionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DeviceControlRepository(private val context: Context) {

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager

    private val _isFlashlightOn = MutableStateFlow(false)
    val isFlashlightOn: StateFlow<Boolean> = _isFlashlightOn.asStateFlow()

    private var cameraIdWithFlash: String? = null

    init {
        findCameraWithFlash()
    }

    private fun findCameraWithFlash() {
        try {
            val cameraIds = cameraManager?.cameraIdList ?: emptyArray()
            for (id in cameraIds) {
                val characteristics = cameraManager?.getCameraCharacteristics(id)
                val hasFlash = characteristics?.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
                if (hasFlash) {
                    cameraIdWithFlash = id
                    break
                }
            }
        } catch (e: Exception) {
            cameraIdWithFlash = null
        }
    }

    fun toggleFlashlight(): ActionFeedback {
        val targetState = !_isFlashlightOn.value
        return setFlashlight(targetState)
    }

    fun setFlashlight(enable: Boolean): ActionFeedback {
        val camId = cameraIdWithFlash
        if (cameraManager == null || camId == null) {
            return ActionFeedback(
                status = ActionStatus.UNAVAILABLE,
                title = "Flashlight Unavailable",
                message = "No hardware camera flash unit was detected on this device."
            )
        }

        return try {
            cameraManager.setTorchMode(camId, enable)
            _isFlashlightOn.value = enable
            ActionFeedback(
                status = ActionStatus.SUCCESS,
                title = if (enable) "Flashlight Engaged" else "Flashlight Disengaged",
                message = if (enable) "Illumination module activated." else "Illumination module powered down."
            )
        } catch (e: Exception) {
            ActionFeedback(
                status = ActionStatus.UNAVAILABLE,
                title = "Flashlight Fault",
                message = "Unable to toggle torch: ${e.localizedMessage ?: "Hardware busy"}"
            )
        }
    }

    fun adjustVolume(direction: Int): ActionFeedback {
        if (audioManager == null) {
            return ActionFeedback(
                status = ActionStatus.UNAVAILABLE,
                title = "Audio System Unavailable",
                message = "Audio manager service could not be acquired."
            )
        }

        return try {
            val dir = if (direction > 0) AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER
            audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                dir,
                AudioManager.FLAG_SHOW_UI or AudioManager.FLAG_PLAY_SOUND
            )
            val currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val percent = ((currentVol.toFloat() / maxVol) * 100).toInt()

            ActionFeedback(
                status = ActionStatus.SUCCESS,
                title = if (direction > 0) "Volume Increased" else "Volume Decreased",
                message = "Master audio output adjusted to $percent% ($currentVol/$maxVol)."
            )
        } catch (e: Exception) {
            ActionFeedback(
                status = ActionStatus.UNAVAILABLE,
                title = "Audio Adjustment Failed",
                message = e.localizedMessage ?: "System prevented volume change."
            )
        }
    }

    fun muteVolume(): ActionFeedback {
        if (audioManager == null) {
            return ActionFeedback(
                status = ActionStatus.UNAVAILABLE,
                title = "Audio System Unavailable",
                message = "Audio manager service could not be acquired."
            )
        }

        return try {
            audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_MUTE,
                AudioManager.FLAG_SHOW_UI
            )
            ActionFeedback(
                status = ActionStatus.SUCCESS,
                title = "Audio Muted",
                message = "All media audio streams have been silenced."
            )
        } catch (e: Exception) {
            ActionFeedback(
                status = ActionStatus.UNAVAILABLE,
                title = "Mute Operation Failed",
                message = e.localizedMessage ?: "System prevented volume mute."
            )
        }
    }

    fun setBrightness(percentage: Int): ActionFeedback {
        val clamped = percentage.coerceIn(0, 100)
        val brightnessValue = (clamped * 255) / 100

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(context)) {
                openWriteSettingsScreen()
                return ActionFeedback(
                    status = ActionStatus.PERMISSION_REQUIRED,
                    title = "System Permission Required",
                    message = "Android requires 'Modify System Settings' permission to adjust display brightness. Opening settings panel..."
                )
            }
        }

        return try {
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                brightnessValue
            )
            ActionFeedback(
                status = ActionStatus.SUCCESS,
                title = "Display Luminance Set",
                message = "Brightness level calibrated to $clamped%."
            )
        } catch (e: Exception) {
            ActionFeedback(
                status = ActionStatus.UNAVAILABLE,
                title = "Brightness Adjustment Fault",
                message = e.localizedMessage ?: "Security policy prevented brightness change."
            )
        }
    }

    fun openWriteSettingsScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    fun handleWifiAction(): ActionFeedback {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Open official connectivity panel on modern Android
                val panelIntent = Intent(Settings.Panel.ACTION_WIFI).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(panelIntent)
                ActionFeedback(
                    status = ActionStatus.SYSTEM_ACTION_REQUIRED,
                    title = "Wi-Fi Panel Deployed",
                    message = "Opening system Wi-Fi connection panel per Android security policy."
                )
            } else {
                val wifiIntent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(wifiIntent)
                ActionFeedback(
                    status = ActionStatus.SYSTEM_ACTION_REQUIRED,
                    title = "Opening Wi-Fi Settings",
                    message = "Deploying system Wi-Fi settings console."
                )
            }
        } catch (e: Exception) {
            val fallbackIntent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(fallbackIntent)
            ActionFeedback(
                status = ActionStatus.SYSTEM_ACTION_REQUIRED,
                title = "Wi-Fi Settings Opened",
                message = "System Wi-Fi configuration screen launched."
            )
        }
    }

    fun handleBluetoothAction(): ActionFeedback {
        val adapter = bluetoothManager?.adapter
        if (adapter == null) {
            return ActionFeedback(
                status = ActionStatus.UNAVAILABLE,
                title = "Bluetooth Unavailable",
                message = "No Bluetooth radio transceiver is present on this hardware."
            )
        }

        return try {
            val btIntent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(btIntent)
            ActionFeedback(
                status = ActionStatus.SYSTEM_ACTION_REQUIRED,
                title = "Bluetooth Console Opened",
                message = "Opening system Bluetooth controls per Android policy."
            )
        } catch (e: Exception) {
            ActionFeedback(
                status = ActionStatus.UNAVAILABLE,
                title = "Bluetooth Launch Failed",
                message = e.localizedMessage ?: "Unable to launch Bluetooth settings."
            )
        }
    }

    fun openSystemSettings(): ActionFeedback {
        return try {
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            ActionFeedback(
                status = ActionStatus.SUCCESS,
                title = "System Settings",
                message = "Android System Control console initiated."
            )
        } catch (e: Exception) {
            ActionFeedback(
                status = ActionStatus.UNAVAILABLE,
                title = "Settings Launch Error",
                message = e.localizedMessage ?: "Unable to launch settings."
            )
        }
    }
}
