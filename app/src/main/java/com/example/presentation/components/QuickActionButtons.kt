package com.example.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.ElectricPurpleBright
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisBorder
import com.example.ui.theme.JarvisGlassBrush
import com.example.ui.theme.JarvisMatrixGreen
import com.example.ui.theme.JarvisNeonBorderBrush
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonCyanBright

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuickActionGrid(
    isFlashlightOn: Boolean,
    onToggleFlashlight: () -> Unit,
    onVolumeUp: () -> Unit,
    onVolumeDown: () -> Unit,
    onBrightnessChange: (Int) -> Unit,
    onWifiClick: () -> Unit,
    onBluetoothClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showBrightnessDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "OPERATIONAL CONTROLS",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = JarvisTextSecondary,
                letterSpacing = 1.5.sp
            )
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = NeonCyan.copy(alpha = 0.1f),
                border = BorderStroke(0.5.dp, NeonCyan.copy(alpha = 0.35f))
            ) {
                Text(
                    text = "HARDWARE BUS",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 8.5.sp,
                    color = NeonCyan,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                )
            }
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            maxItemsInEachRow = 4
        ) {
            // Flashlight
            QuickActionButton(
                label = if (isFlashlightOn) "TORCH ON" else "TORCH",
                icon = if (isFlashlightOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                isActive = isFlashlightOn,
                activeColor = NeonCyan,
                testTag = "btn_flashlight",
                onClick = onToggleFlashlight,
                modifier = Modifier.weight(1f)
            )

            // Wi-Fi
            QuickActionButton(
                label = "WI-FI",
                icon = Icons.Default.Wifi,
                isActive = false,
                testTag = "btn_wifi",
                onClick = onWifiClick,
                modifier = Modifier.weight(1f)
            )

            // Bluetooth
            QuickActionButton(
                label = "BT LINK",
                icon = Icons.Default.Bluetooth,
                isActive = false,
                testTag = "btn_bluetooth",
                onClick = onBluetoothClick,
                modifier = Modifier.weight(1f)
            )

            // Brightness
            QuickActionButton(
                label = "LUMEN",
                icon = Icons.Default.BrightnessMedium,
                isActive = false,
                testTag = "btn_brightness",
                onClick = { showBrightnessDialog = true },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Volume +
            QuickActionButton(
                label = "VOL +",
                icon = Icons.Default.VolumeUp,
                isActive = false,
                testTag = "btn_vol_up",
                onClick = onVolumeUp,
                modifier = Modifier.weight(1f)
            )

            // Volume -
            QuickActionButton(
                label = "VOL -",
                icon = Icons.Default.VolumeDown,
                isActive = false,
                testTag = "btn_vol_down",
                onClick = onVolumeDown,
                modifier = Modifier.weight(1f)
            )

            // Settings
            QuickActionButton(
                label = "SETTINGS",
                icon = Icons.Default.Settings,
                isActive = false,
                testTag = "btn_system_settings",
                onClick = onSettingsClick,
                modifier = Modifier.weight(1f)
            )
        }
    }

    if (showBrightnessDialog) {
        BrightnessAdjustDialog(
            onDismiss = { showBrightnessDialog = false },
            onApply = { percent ->
                onBrightnessChange(percent)
                showBrightnessDialog = false
            }
        )
    }
}

@Composable
fun QuickActionButton(
    label: String,
    icon: ImageVector,
    isActive: Boolean,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = NeonCyan
) {
    val shape = RoundedCornerShape(12.dp)

    val activeBackgroundBrush = Brush.verticalGradient(
        colors = listOf(
            activeColor.copy(alpha = 0.28f),
            activeColor.copy(alpha = 0.12f)
        )
    )

    val activeBorderBrush = Brush.horizontalGradient(
        colors = listOf(
            activeColor,
            ElectricPurpleBright,
            activeColor
        )
    )

    Box(
        modifier = modifier
            .height(58.dp)
            .clip(shape)
            .then(
                if (isActive) Modifier.background(activeBackgroundBrush, shape)
                else Modifier.background(JarvisGlassBrush, shape)
            )
            .then(
                if (isActive) Modifier.border(BorderStroke(1.2.dp, activeBorderBrush), shape)
                else Modifier.border(BorderStroke(1.dp, JarvisBorder.copy(alpha = 0.8f)), shape)
            )
            .drawBehind {
                if (isActive) {
                    // Top hairline glow
                    drawLine(
                        color = Color.White.copy(alpha = 0.45f),
                        start = Offset(12.dp.toPx(), 1.dp.toPx()),
                        end = Offset(size.width - 12.dp.toPx(), 1.dp.toPx()),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }
            .clickable(onClick = onClick)
            .testTag(testTag)
            .padding(horizontal = 4.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) activeColor else JarvisTextPrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = label,
                fontFamily = FontFamily.Monospace,
                fontSize = 9.5.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                color = if (isActive) activeColor else JarvisTextSecondary,
                maxLines = 1
            )
        }

        // Active indicator pip in top right
        if (isActive) {
            Surface(
                shape = CircleShape,
                color = activeColor,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 4.dp, top = 2.dp)
                    .size(5.dp)
            ) {}
        }
    }
}

@Composable
fun BrightnessAdjustDialog(
    onDismiss: () -> Unit,
    onApply: (Int) -> Unit
) {
    var sliderValue by remember { mutableFloatStateOf(65f) }

    Dialog(onDismissRequest = onDismiss) {
        HudCard(
            modifier = Modifier.fillMaxWidth(),
            borderBrush = JarvisNeonBorderBrush,
            backgroundColor = JarvisSurfaceElevated
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "LUMINANCE CALIBRATION",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    color = NeonCyan,
                    letterSpacing = 1.5.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = NeonCyan.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f)),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "${sliderValue.toInt()}%",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 32.sp,
                        color = NeonCyanBright,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    valueRange = 5f..100f,
                    colors = SliderDefaults.colors(
                        thumbColor = NeonCyan,
                        activeTrackColor = NeonCyan,
                        inactiveTrackColor = JarvisBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("slider_brightness")
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            "CANCEL",
                            color = JarvisTextSecondary,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onApply(sliderValue.toInt()) },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "CALIBRATE",
                            color = JarvisBackground,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}
