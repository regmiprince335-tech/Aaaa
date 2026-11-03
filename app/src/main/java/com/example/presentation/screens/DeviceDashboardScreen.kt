package com.example.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppInfo
import com.example.domain.device.AppLauncherRepository
import com.example.presentation.components.ActionFeedbackBanner
import com.example.presentation.components.BatteryCard
import com.example.presentation.components.CpuCard
import com.example.presentation.components.HudCard
import com.example.presentation.components.JarvisScreenHeader
import com.example.presentation.components.jarvisBackground
import com.example.presentation.components.MemoryCard
import com.example.presentation.components.NetworkCard
import com.example.presentation.components.QuickActionGrid
import com.example.presentation.components.StorageCard
import com.example.presentation.viewmodel.JarvisUiState
import com.example.presentation.viewmodel.JarvisViewModel
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisBorder
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.NeonCyan

@Composable
fun DeviceDashboardScreen(
    uiState: JarvisUiState,
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appLauncher = remember { AppLauncherRepository(context) }
    var installedApps by remember { mutableStateOf<List<AppInfo>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }

    // Load installed apps once
    remember {
        installedApps = appLauncher.getInstalledApps()
        true
    }

    val filteredApps = if (searchQuery.isBlank()) {
        installedApps.take(15)
    } else {
        installedApps.filter { it.appName.contains(searchQuery, ignoreCase = true) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .jarvisBackground()
    ) {
        // TOP HEADER
        JarvisScreenHeader(
            title = "DEVICE CONTROL & TELEMETRY",
            subtitle = "Direct Hardware Bus Interface"
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Action Feedback Banner
            if (uiState.lastActionFeedback != null) {
                item {
                    ActionFeedbackBanner(feedback = uiState.lastActionFeedback)
                }
            }

            // Quick Actions
            item {
                QuickActionGrid(
                    isFlashlightOn = uiState.isFlashlightOn,
                    onToggleFlashlight = { viewModel.toggleFlashlight() },
                    onVolumeUp = { viewModel.adjustVolume(1) },
                    onVolumeDown = { viewModel.adjustVolume(-1) },
                    onBrightnessChange = { percent -> viewModel.setBrightness(percent) },
                    onWifiClick = { viewModel.openWifi() },
                    onBluetoothClick = { viewModel.openBluetooth() },
                    onSettingsClick = { viewModel.openSettings() }
                )
            }

            // Telemetry Cards
            item {
                Text(
                    text = "HARDWARE SENSORS & METRICS",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = JarvisTextSecondary,
                    letterSpacing = 1.sp
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BatteryCard(deviceStatus = uiState.deviceStatus, modifier = Modifier.weight(1f))
                    MemoryCard(deviceStatus = uiState.deviceStatus, modifier = Modifier.weight(1f))
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StorageCard(deviceStatus = uiState.deviceStatus, modifier = Modifier.weight(1f))
                    NetworkCard(deviceStatus = uiState.deviceStatus, modifier = Modifier.weight(1f))
                }
            }

            item {
                CpuCard(deviceStatus = uiState.deviceStatus, modifier = Modifier.fillMaxWidth())
            }

            // App Launcher Section
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "APP LAUNCHER SUBSYSTEM",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = NeonCyan,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "${installedApps.size} apps available",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = JarvisTextMuted
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "Filter installed apps (e.g., Chrome, Camera)...",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = JarvisTextMuted
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = NeonCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_apps_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = JarvisBorder,
                        focusedTextColor = JarvisTextPrimary,
                        unfocusedTextColor = JarvisTextPrimary,
                        focusedContainerColor = JarvisSurfaceElevated,
                        unfocusedContainerColor = JarvisSurfaceElevated
                    ),
                    singleLine = true
                )
            }

            items(filteredApps, key = { it.packageName }) { app ->
                HudCard(
                    borderColor = JarvisBorder,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.launchApp(app.appName) }
                        .testTag("app_item_${app.packageName}")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = ElectricPurple.copy(alpha = 0.15f),
                                border = BorderStroke(0.5.dp, ElectricPurple.copy(alpha = 0.5f)),
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Apps,
                                        contentDescription = app.appName,
                                        tint = ElectricPurple,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Text(
                                    text = app.appName,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = JarvisTextPrimary
                                )
                                Text(
                                    text = app.packageName,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = JarvisTextMuted,
                                    maxLines = 1
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.Launch,
                            contentDescription = "Launch ${app.appName}",
                            tint = NeonCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
