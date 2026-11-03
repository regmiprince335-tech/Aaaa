package com.example.domain.command

import com.example.data.model.ActionFeedback
import com.example.data.model.ActionStatus
import com.example.data.model.DeviceStatus
import com.example.domain.device.AppLauncherRepository
import com.example.domain.device.DeviceControlRepository

sealed class CommandResult {
    data class Handled(
        val feedback: ActionFeedback,
        val speechResponse: String,
        val commandName: String
    ) : CommandResult()

    data class RequiresAi(val query: String) : CommandResult()
}

class CommandProcessor(
    private val deviceControl: DeviceControlRepository,
    private val appLauncher: AppLauncherRepository
) {

    fun processInput(rawInput: String, currentStatus: DeviceStatus? = null): CommandResult {
        val input = rawInput.trim()
        val lower = input.lowercase()

        // 0. Hands-Free Conversational Protocol
        if (matchesAny(lower, "enable hands free", "hands free on", "hands-free on", "turn on hands free", "start hands free", "hands free mode", "hands free mode on")) {
            return CommandResult.Handled(
                feedback = ActionFeedback(
                    status = ActionStatus.SUCCESS,
                    title = "Hands-Free Protocol Active",
                    message = "Hands-free duplex conversation engaged."
                ),
                speechResponse = "Hands-free mode engaged, sir. I am listening continuously.",
                commandName = "Hands-Free ON"
            )
        }
        if (matchesAny(lower, "disable hands free", "hands free off", "hands-free off", "turn off hands free", "stand down", "goodbye jarvis", "exit hands free", "stop listening", "hands free mode off")) {
            return CommandResult.Handled(
                feedback = ActionFeedback(
                    status = ActionStatus.SUCCESS,
                    title = "Hands-Free Deactivated",
                    message = "Hands-free conversation powered down."
                ),
                speechResponse = "Standing down, sir. Resuming manual standby.",
                commandName = "Hands-Free OFF"
            )
        }

        // 1. Flashlight
        if (matchesAny(lower, "turn on flashlight", "flashlight on", "torch on", "enable flashlight", "turn on torch")) {
            val fb = deviceControl.setFlashlight(true)
            return CommandResult.Handled(
                feedback = fb,
                speechResponse = if (fb.status == ActionStatus.SUCCESS) "Flashlight illuminated, sir." else fb.message,
                commandName = "Flashlight ON"
            )
        }
        if (matchesAny(lower, "turn off flashlight", "flashlight off", "torch off", "disable flashlight", "turn off torch")) {
            val fb = deviceControl.setFlashlight(false)
            return CommandResult.Handled(
                feedback = fb,
                speechResponse = if (fb.status == ActionStatus.SUCCESS) "Flashlight disengaged." else fb.message,
                commandName = "Flashlight OFF"
            )
        }

        // 2. Volume
        if (matchesAny(lower, "increase volume", "volume up", "raise volume", "louder", "boost volume")) {
            val fb = deviceControl.adjustVolume(1)
            return CommandResult.Handled(
                feedback = fb,
                speechResponse = "Volume raised, sir.",
                commandName = "Volume Up"
            )
        }
        if (matchesAny(lower, "decrease volume", "volume down", "lower volume", "quieter", "reduce volume")) {
            val fb = deviceControl.adjustVolume(-1)
            return CommandResult.Handled(
                feedback = fb,
                speechResponse = "Volume lowered.",
                commandName = "Volume Down"
            )
        }
        if (matchesAny(lower, "mute volume", "mute audio", "mute sound", "silence audio", "mute")) {
            val fb = deviceControl.muteVolume()
            return CommandResult.Handled(
                feedback = fb,
                speechResponse = "Audio streams muted.",
                commandName = "Mute"
            )
        }

        // 3. Brightness
        val brightnessRegex = Regex("(?:set|change)?\\s*brightness(?:\\s+to)?\\s*(\\d+)(?:\\s*percent|%)?")
        val brightnessMatch = brightnessRegex.find(lower)
        if (brightnessMatch != null) {
            val percent = brightnessMatch.groupValues[1].toIntOrNull() ?: 50
            val fb = deviceControl.setBrightness(percent)
            return CommandResult.Handled(
                feedback = fb,
                speechResponse = if (fb.status == ActionStatus.SUCCESS) "Luminance calibrated to $percent percent." else fb.message,
                commandName = "Set Brightness $percent%"
            )
        }

        // 4. App Launching ("open [app]", "launch [app]")
        if (lower.startsWith("open ") || lower.startsWith("launch ")) {
            val appQuery = lower.removePrefix("open ").removePrefix("launch ").trim()
            if (appQuery.isNotBlank()) {
                if (appQuery == "settings" || appQuery == "system settings") {
                    val fb = deviceControl.openSystemSettings()
                    return CommandResult.Handled(
                        feedback = fb,
                        speechResponse = "Opening system settings console.",
                        commandName = "Open Settings"
                    )
                }
                if (appQuery == "wifi" || appQuery == "wi-fi" || appQuery == "wi-fi settings") {
                    val fb = deviceControl.handleWifiAction()
                    return CommandResult.Handled(
                        feedback = fb,
                        speechResponse = "Deploying Wi-Fi control panel.",
                        commandName = "Open Wi-Fi"
                    )
                }
                if (appQuery == "bluetooth" || appQuery == "bluetooth settings") {
                    val fb = deviceControl.handleBluetoothAction()
                    return CommandResult.Handled(
                        feedback = fb,
                        speechResponse = "Opening Bluetooth controls.",
                        commandName = "Open Bluetooth"
                    )
                }

                val fb = appLauncher.launchAppByName(appQuery)
                return CommandResult.Handled(
                    feedback = fb,
                    speechResponse = fb.message,
                    commandName = "Launch $appQuery"
                )
            }
        }

        // 5. System Status telemetry commands
        if (matchesAny(lower, "show battery", "battery status", "what is my battery", "battery level", "how much battery")) {
            val level = currentStatus?.batteryLevel ?: -1
            val isCharging = currentStatus?.isCharging == true
            val statusText = if (level >= 0) {
                "Battery is currently at $level percent${if (isCharging) ", charging" else ""}, sir."
            } else {
                "Battery telemetry is currently recalibrating."
            }
            return CommandResult.Handled(
                feedback = ActionFeedback(
                    status = ActionStatus.SUCCESS,
                    title = "Battery Telemetry",
                    message = statusText
                ),
                speechResponse = statusText,
                commandName = "Battery Query"
            )
        }

        if (matchesAny(lower, "show memory", "memory status", "ram status", "how much ram", "ram usage")) {
            val used = currentStatus?.usedRamGb ?: 0f
            val total = currentStatus?.totalRamGb ?: 0f
            val pct = currentStatus?.ramUsagePercent ?: 0
            val statusText = "System RAM utilization is at $pct percent: $used GB used out of $total GB total."
            return CommandResult.Handled(
                feedback = ActionFeedback(
                    status = ActionStatus.SUCCESS,
                    title = "Memory Utilization",
                    message = statusText
                ),
                speechResponse = statusText,
                commandName = "RAM Query"
            )
        }

        if (matchesAny(lower, "show storage", "storage status", "internal storage", "disk space")) {
            val used = currentStatus?.usedStorageGb ?: 0f
            val total = currentStatus?.totalStorageGb ?: 0f
            val pct = currentStatus?.storageUsagePercent ?: 0
            val statusText = "Internal storage is at $pct percent capacity: $used GB occupied of $total GB total."
            return CommandResult.Handled(
                feedback = ActionFeedback(
                    status = ActionStatus.SUCCESS,
                    title = "Storage Status",
                    message = statusText
                ),
                speechResponse = statusText,
                commandName = "Storage Query"
            )
        }

        // 6. Wi-Fi / Bluetooth quick triggers
        if (matchesAny(lower, "wifi settings", "open wifi", "toggle wifi", "turn on wifi", "turn off wifi")) {
            val fb = deviceControl.handleWifiAction()
            return CommandResult.Handled(
                feedback = fb,
                speechResponse = fb.message,
                commandName = "Wi-Fi Action"
            )
        }

        if (matchesAny(lower, "bluetooth settings", "open bluetooth", "toggle bluetooth", "turn on bluetooth", "turn off bluetooth")) {
            val fb = deviceControl.handleBluetoothAction()
            return CommandResult.Handled(
                feedback = fb,
                speechResponse = fb.message,
                commandName = "Bluetooth Action"
            )
        }

        // Not a deterministic device command -> Route to AI conversation
        return CommandResult.RequiresAi(input)
    }

    private fun matchesAny(input: String, vararg targets: String): Boolean {
        return targets.any { input == it || input.startsWith("$it ") || input.endsWith(" $it") }
    }
}
