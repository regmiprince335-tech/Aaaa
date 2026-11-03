package com.example.domain.device

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.example.data.model.ActionFeedback
import com.example.data.model.ActionStatus
import com.example.data.model.AppInfo

class AppLauncherRepository(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager

    fun getInstalledApps(): List<AppInfo> {
        val launchableIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = try {
            packageManager.queryIntentActivities(launchableIntent, 0)
        } catch (e: Exception) {
            emptyList()
        }

        return resolveInfos.mapNotNull { info ->
            val packageName = info.activityInfo.packageName
            val label = info.loadLabel(packageManager).toString()
            val isSystem = (info.activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            if (label.isNotBlank()) AppInfo(packageName, label, isSystem) else null
        }.distinctBy { it.packageName }
            .sortedBy { it.appName.lowercase() }
    }

    fun launchAppByName(requestedName: String): ActionFeedback {
        val target = requestedName.trim().lowercase()
        val installedApps = getInstalledApps()

        // 1. Direct or partial match on label
        var match = installedApps.firstOrNull { it.appName.lowercase() == target }
        if (match == null) {
            match = installedApps.firstOrNull { it.appName.lowercase().contains(target) || target.contains(it.appName.lowercase()) }
        }

        // 2. Common aliases (e.g. "browser" -> chrome, "music" -> spotify/youtube music)
        if (match == null) {
            match = when {
                target.contains("chrome") || target.contains("browser") -> {
                    installedApps.firstOrNull { it.packageName.contains("chrome") || it.appName.contains("Chrome", ignoreCase = true) || it.appName.contains("Browser", ignoreCase = true) }
                }
                target.contains("youtube") -> {
                    installedApps.firstOrNull { it.packageName.contains("youtube") || it.appName.contains("YouTube", ignoreCase = true) }
                }
                target.contains("camera") -> {
                    installedApps.firstOrNull { it.packageName.contains("camera") || it.appName.contains("Camera", ignoreCase = true) }
                }
                target.contains("calculator") -> {
                    installedApps.firstOrNull { it.packageName.contains("calculator") || it.appName.contains("Calc", ignoreCase = true) }
                }
                target.contains("map") -> {
                    installedApps.firstOrNull { it.packageName.contains("maps") || it.appName.contains("Maps", ignoreCase = true) }
                }
                target.contains("settings") -> {
                    installedApps.firstOrNull { it.packageName.contains("settings") || it.appName.contains("Settings", ignoreCase = true) }
                }
                else -> null
            }
        }

        if (match == null) {
            return ActionFeedback(
                status = ActionStatus.UNAVAILABLE,
                title = "Application Not Found",
                message = "Sorry, I couldn't find an installed application matching '$requestedName'."
            )
        }

        return try {
            val launchIntent = packageManager.getLaunchIntentForPackage(match.packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                ActionFeedback(
                    status = ActionStatus.SUCCESS,
                    title = "Launching Application",
                    message = "Initiating ${match.appName} protocol."
                )
            } else {
                ActionFeedback(
                    status = ActionStatus.UNAVAILABLE,
                    title = "Launch Restricted",
                    message = "Unable to generate launch intent for ${match.appName}."
                )
            }
        } catch (e: Exception) {
            ActionFeedback(
                status = ActionStatus.UNAVAILABLE,
                title = "Launch Error",
                message = "Failed to launch ${match.appName}: ${e.localizedMessage ?: "Security policy"}"
            )
        }
    }
}
