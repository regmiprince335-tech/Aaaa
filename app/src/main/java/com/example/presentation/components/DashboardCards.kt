package com.example.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DeviceStatus
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.ElectricPurpleBright
import com.example.ui.theme.JarvisAlertRed
import com.example.ui.theme.JarvisBorder
import com.example.ui.theme.JarvisEmerald
import com.example.ui.theme.JarvisGlassBrush
import com.example.ui.theme.JarvisMatrixGreen
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.JarvisWarningAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonCyanBright

@Composable
fun BatteryCard(
    deviceStatus: DeviceStatus,
    modifier: Modifier = Modifier
) {
    val level = deviceStatus.batteryLevel
    val isCharging = deviceStatus.isCharging
    val temp = deviceStatus.batteryTemperature

    val batteryColor = when {
        isCharging -> JarvisMatrixGreen
        level < 20 -> JarvisAlertRed
        level < 50 -> JarvisWarningAmber
        else -> NeonCyan
    }

    val borderBrush = Brush.horizontalGradient(
        colors = listOf(
            batteryColor.copy(alpha = 0.7f),
            NeonCyan.copy(alpha = 0.25f),
            batteryColor.copy(alpha = 0.45f)
        )
    )

    HudCard(
        modifier = modifier.testTag("battery_card"),
        borderBrush = borderBrush
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = batteryColor.copy(alpha = 0.15f),
                        border = BorderStroke(0.5.dp, batteryColor.copy(alpha = 0.4f)),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                                contentDescription = "Battery Status",
                                tint = batteryColor,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "POWER CELL",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = JarvisTextSecondary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = if (isCharging) "CHARGING" else "DRAINING",
                            fontSize = 8.5.sp,
                            fontFamily = FontFamily.Monospace,
                            color = if (isCharging) JarvisMatrixGreen else JarvisTextMuted,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = batteryColor.copy(alpha = 0.14f),
                    border = BorderStroke(0.8.dp, batteryColor.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = if (level >= 0) "$level%" else "--",
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold,
                        color = batteryColor,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { (level.coerceIn(0, 100) / 100f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = batteryColor,
                trackColor = JarvisBorder.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isCharging) "AC Fast Power" else "Standard Load",
                    fontSize = 9.5.sp,
                    fontFamily = FontFamily.Monospace,
                    color = JarvisTextMuted
                )
                if (temp != null) {
                    Text(
                        text = "${temp}°C Core",
                        fontSize = 9.5.sp,
                        fontFamily = FontFamily.Monospace,
                        color = if (temp > 42) JarvisWarningAmber else JarvisTextMuted
                    )
                }
            }
        }
    }
}

@Composable
fun MemoryCard(
    deviceStatus: DeviceStatus,
    modifier: Modifier = Modifier
) {
    val used = deviceStatus.usedRamGb
    val total = deviceStatus.totalRamGb
    val pct = deviceStatus.ramUsagePercent

    val borderBrush = Brush.horizontalGradient(
        colors = listOf(
            ElectricPurple.copy(alpha = 0.7f),
            NeonCyan.copy(alpha = 0.3f),
            ElectricPurpleBright.copy(alpha = 0.5f)
        )
    )

    HudCard(
        modifier = modifier.testTag("memory_card"),
        borderBrush = borderBrush
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = ElectricPurple.copy(alpha = 0.15f),
                        border = BorderStroke(0.5.dp, ElectricPurple.copy(alpha = 0.4f)),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Memory,
                                contentDescription = "Memory Status",
                                tint = ElectricPurpleBright,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "RAM MATRIX",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = JarvisTextSecondary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "$used/$total GB",
                            fontSize = 8.5.sp,
                            fontFamily = FontFamily.Monospace,
                            color = JarvisTextMuted,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ElectricPurple.copy(alpha = 0.16f),
                    border = BorderStroke(0.8.dp, ElectricPurple.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "$pct%",
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold,
                        color = ElectricPurpleBright,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { (pct.coerceIn(0, 100) / 100f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = ElectricPurpleBright,
                trackColor = JarvisBorder.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "LPDDR4x Subsystem",
                    fontSize = 9.5.sp,
                    fontFamily = FontFamily.Monospace,
                    color = JarvisTextMuted
                )
                Text(
                    text = if (pct > 80) "Heavy Load" else "Optimal",
                    fontSize = 9.5.sp,
                    fontFamily = FontFamily.Monospace,
                    color = if (pct > 80) JarvisWarningAmber else JarvisMatrixGreen
                )
            }
        }
    }
}

@Composable
fun StorageCard(
    deviceStatus: DeviceStatus,
    modifier: Modifier = Modifier
) {
    val used = deviceStatus.usedStorageGb
    val total = deviceStatus.totalStorageGb
    val pct = deviceStatus.storageUsagePercent

    val borderBrush = Brush.horizontalGradient(
        colors = listOf(
            NeonCyan.copy(alpha = 0.65f),
            Color(0xFF00B0FF).copy(alpha = 0.3f),
            NeonCyanBright.copy(alpha = 0.45f)
        )
    )

    HudCard(
        modifier = modifier.testTag("storage_card"),
        borderBrush = borderBrush
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = NeonCyan.copy(alpha = 0.15f),
                        border = BorderStroke(0.5.dp, NeonCyan.copy(alpha = 0.4f)),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.SdStorage,
                                contentDescription = "Storage Status",
                                tint = NeonCyan,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "NAND STORAGE",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = JarvisTextSecondary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "$used/$total GB",
                            fontSize = 8.5.sp,
                            fontFamily = FontFamily.Monospace,
                            color = JarvisTextMuted,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = NeonCyan.copy(alpha = 0.14f),
                    border = BorderStroke(0.8.dp, NeonCyan.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "$pct%",
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold,
                        color = NeonCyan,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { (pct.coerceIn(0, 100) / 100f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = NeonCyan,
                trackColor = JarvisBorder.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Internal UFS/eMMC",
                    fontSize = 9.5.sp,
                    fontFamily = FontFamily.Monospace,
                    color = JarvisTextMuted
                )
                Text(
                    text = "${total - used} GB Free",
                    fontSize = 9.5.sp,
                    fontFamily = FontFamily.Monospace,
                    color = NeonCyanBright
                )
            }
        }
    }
}

@Composable
fun NetworkCard(
    deviceStatus: DeviceStatus,
    modifier: Modifier = Modifier
) {
    val isConnected = deviceStatus.isNetworkConnected
    val type = deviceStatus.networkType
    val details = deviceStatus.networkDetails

    val color = if (isConnected) JarvisMatrixGreen else JarvisAlertRed

    val borderBrush = Brush.horizontalGradient(
        colors = listOf(
            color.copy(alpha = 0.7f),
            NeonCyan.copy(alpha = 0.25f),
            color.copy(alpha = 0.45f)
        )
    )

    HudCard(
        modifier = modifier.testTag("network_card"),
        borderBrush = borderBrush
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = color.copy(alpha = 0.15f),
                        border = BorderStroke(0.5.dp, color.copy(alpha = 0.4f)),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isConnected) Icons.Default.Wifi else Icons.Default.WifiOff,
                                contentDescription = "Network Link",
                                tint = color,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "TELEMETRY LINK",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = JarvisTextSecondary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = if (isConnected) "ONLINE" else "OFFLINE",
                            fontSize = 8.5.sp,
                            fontFamily = FontFamily.Monospace,
                            color = color,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = color.copy(alpha = 0.14f),
                    border = BorderStroke(0.8.dp, color.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = type.uppercase(),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = color,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(4.dp),
                color = JarvisSurfaceElevated,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (details.isNotBlank()) details else "Active Network Gateway",
                    fontSize = 9.5.sp,
                    fontFamily = FontFamily.Monospace,
                    color = JarvisTextSecondary,
                    maxLines = 1,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun CpuCard(
    deviceStatus: DeviceStatus,
    modifier: Modifier = Modifier
) {
    val borderBrush = Brush.horizontalGradient(
        colors = listOf(
            NeonCyan.copy(alpha = 0.65f),
            ElectricPurple.copy(alpha = 0.35f),
            NeonCyanBright.copy(alpha = 0.5f)
        )
    )

    HudCard(
        modifier = modifier.testTag("cpu_card"),
        borderBrush = borderBrush
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = NeonCyan.copy(alpha = 0.15f),
                        border = BorderStroke(0.5.dp, NeonCyan.copy(alpha = 0.4f)),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.DeveloperBoard,
                                contentDescription = "CPU Processors",
                                tint = NeonCyan,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "NEURAL PROCESSING CORES",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = JarvisTextSecondary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "ASYMETRIC OCTA-CORE",
                            fontSize = 8.5.sp,
                            fontFamily = FontFamily.Monospace,
                            color = JarvisTextMuted,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = JarvisMatrixGreen.copy(alpha = 0.14f),
                    border = BorderStroke(0.8.dp, JarvisMatrixGreen.copy(alpha = 0.5f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = JarvisMatrixGreen,
                            modifier = Modifier.size(6.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "ONLINE",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = JarvisMatrixGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = deviceStatus.cpuStatus,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = JarvisTextSecondary
            )
        }
    }
}
