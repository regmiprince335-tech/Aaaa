package com.example.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "HUD", Icons.Default.Home)
    object Chat : Screen("chat", "Chat", Icons.Default.Chat)
    object Device : Screen("device", "Systems", Icons.Default.PowerSettingsNew)
    object Notifications : Screen("notifications", "Comms", Icons.Default.Notifications)
    object Settings : Screen("settings", "Config", Icons.Default.Settings)
}

val navScreens = listOf(
    Screen.Home,
    Screen.Chat,
    Screen.Device,
    Screen.Notifications,
    Screen.Settings
)
