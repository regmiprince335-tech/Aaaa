package com.example.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.components.HudCard
import com.example.presentation.viewmodel.JarvisUiState
import com.example.presentation.viewmodel.JarvisViewModel
import com.example.presentation.components.JarvisScreenHeader
import com.example.presentation.components.jarvisBackground
import com.example.service.JarvisNotificationItem
import com.example.service.JarvisNotificationListenerService
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.JarvisAlertRed
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisBorder
import com.example.ui.theme.JarvisMatrixGreen
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.JarvisWarningAmber
import com.example.ui.theme.NeonCyan
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationReaderScreen(
    uiState: JarvisUiState,
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val notifications by JarvisNotificationListenerService.notifications.collectAsState()
    val isConnected by JarvisNotificationListenerService.isConnected.collectAsState()
    val hasPermission = uiState.permissionStatus.notificationListenerGranted

    Column(
        modifier = modifier
            .fillMaxSize()
            .jarvisBackground()
    ) {
        // TOP HEADER
        JarvisScreenHeader(
            title = "COMMUNICATIONS MONITOR",
            subtitle = if (hasPermission) "Security clearance granted" else "Awaiting listener authorization"
        ) {
            if (notifications.isNotEmpty()) {
                IconButton(
                    onClick = { JarvisNotificationListenerService.clearNotifications() },
                    modifier = Modifier.testTag("btn_clear_notifications")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear logs",
                        tint = JarvisTextSecondary
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // PERMISSION & SECURITY EXPLAINER
            item {
                HudCard(
                    borderColor = if (hasPermission) JarvisMatrixGreen.copy(alpha = 0.4f) else JarvisWarningAmber.copy(alpha = 0.5f),
                    backgroundColor = JarvisSurfaceElevated,
                    modifier = Modifier.fillMaxWidth().testTag("notification_explainer_card")
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (hasPermission) Icons.Default.CheckCircle else Icons.Default.Security,
                                contentDescription = "Security",
                                tint = if (hasPermission) JarvisMatrixGreen else JarvisWarningAmber,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "NOTIFICATION ACCESS SECURITY PROTOCOL",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (hasPermission) JarvisMatrixGreen else JarvisWarningAmber,
                                letterSpacing = 1.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "JARVIS needs Notification Access to read incoming system notifications. No personal data is stored or transmitted outside this device. Access can be revoked at any time.",
                            fontSize = 12.5.sp,
                            color = JarvisTextPrimary,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (!hasPermission) {
                            Button(
                                onClick = { viewModel.openNotificationListenerSettings() },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("btn_grant_notification_access")
                            ) {
                                Text(
                                    text = "GRANT NOTIFICATION ACCESS",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = JarvisBackground
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "MONITORING ACTIVE",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = JarvisMatrixGreen
                                )

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (uiState.userSettings.notificationReadingEnabled) "ON" else "OFF",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = JarvisTextSecondary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Switch(
                                        checked = uiState.userSettings.notificationReadingEnabled,
                                        onCheckedChange = { viewModel.setNotificationReading(it) },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = NeonCyan,
                                            checkedTrackColor = NeonCyan.copy(alpha = 0.3f),
                                            uncheckedThumbColor = JarvisTextMuted,
                                            uncheckedTrackColor = JarvisBorder
                                        ),
                                        modifier = Modifier.testTag("switch_notification_reading")
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // NOTIFICATION STREAM LIST
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "INTERCEPTED COMMUNICATONS",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = JarvisTextSecondary,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "${notifications.size} captured",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = JarvisTextMuted
                    )
                }
            }

            if (notifications.isEmpty()) {
                item {
                    HudCard(
                        borderColor = JarvisBorder,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsOff,
                                contentDescription = "No notifications",
                                tint = JarvisTextMuted,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No notifications in communication log",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = JarvisTextMuted
                            )
                        }
                    }
                }
            } else {
                items(notifications, key = { it.id }) { notif ->
                    NotificationCardItem(
                        item = notif,
                        onReadAloud = {
                            val textToSpeak = "Notification from ${notif.appName}. ${notif.title}. ${notif.text}"
                            viewModel.speak(textToSpeak)
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun NotificationCardItem(
    item: JarvisNotificationItem,
    onReadAloud: () -> Unit
) {
    val timeFormatted = remember(item.timestamp) {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        sdf.format(Date(item.timestamp))
    }

    HudCard(
        borderColor = NeonCyan.copy(alpha = 0.35f),
        backgroundColor = JarvisSurfaceElevated,
        modifier = Modifier.fillMaxWidth().testTag("notif_item_${item.id}")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = ElectricPurple.copy(alpha = 0.2f),
                        border = BorderStroke(0.5.dp, ElectricPurple.copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = item.appName.uppercase(),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = ElectricPurple,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = timeFormatted,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = JarvisTextMuted
                    )
                }

                IconButton(
                    onClick = onReadAloud,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Read notification aloud",
                        tint = NeonCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            if (item.title.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = JarvisTextPrimary
                )
            }

            if (item.text.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.text,
                    fontSize = 12.sp,
                    color = JarvisTextSecondary,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
