package com.example.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.components.HudCard
import com.example.presentation.components.JarvisScreenHeader
import com.example.presentation.components.jarvisBackground
import com.example.presentation.viewmodel.JarvisUiState
import com.example.presentation.viewmodel.JarvisViewModel
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

@Composable
fun SettingsScreen(
    uiState: JarvisUiState,
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    var apiKeyInput by remember { mutableStateOf(uiState.userSettings.customApiKey) }
    var neonSlider by remember { mutableFloatStateOf(uiState.userSettings.neonIntensity) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .jarvisBackground()
    ) {
        // TOP HEADER
        JarvisScreenHeader(
            title = "SYSTEM CONFIGURATION & PREFERENCES",
            subtitle = "J.A.R.V.I.S. Kernel Parameters"
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. AI CONFIGURATION SECTION
            item {
                SectionHeader(title = "AI & NEURAL PROTOCOL", icon = Icons.Default.AutoAwesome)
            }

            item {
                HudCard(modifier = Modifier.fillMaxWidth().testTag("ai_config_card")) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "GEMINI CORE STATUS",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = JarvisTextSecondary
                            )

                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = JarvisMatrixGreen.copy(alpha = 0.15f),
                                border = BorderStroke(0.5.dp, JarvisMatrixGreen.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "READY",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = JarvisMatrixGreen,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Model: Gemini 2.5 Flash stream generation with real-time SSE chunking and J.A.R.V.I.S. refined persona.",
                            fontSize = 12.sp,
                            color = JarvisTextPrimary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = apiKeyInput,
                            onValueChange = { apiKeyInput = it },
                            label = { Text("Custom Gemini API Key Override", fontFamily = FontFamily.Monospace, fontSize = 11.sp) },
                            placeholder = { Text("AI Studio Secrets or AIzaSy...", fontFamily = FontFamily.Monospace, fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth().testTag("input_custom_api_key"),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = JarvisBorder,
                                focusedTextColor = JarvisTextPrimary,
                                unfocusedTextColor = JarvisTextPrimary
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = { viewModel.setCustomApiKey(apiKeyInput.trim()) },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.testTag("btn_save_api_key")
                            ) {
                                Text("SAVE KEY", color = JarvisBackground, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // 2. VOICE & SPEECH CONFIGURATION
            item {
                SectionHeader(title = "VOICE RECEPTORS & SYNTHESIS", icon = Icons.Default.RecordVoiceOver)
            }

            item {
                HudCard(modifier = Modifier.fillMaxWidth().testTag("voice_config_card")) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Hands-Free Conversational Duplex toggle
                        SettingToggleRow(
                            title = "HANDS-FREE CONVERSATIONAL DUPLEX",
                            subtitle = "Continuous back-and-forth speech: automatically listens for your verbal reply after vocalizing responses",
                            checked = uiState.isHandsFreeActive,
                            onCheckedChange = { viewModel.toggleHandsFreeMode() },
                            testTag = "switch_hands_free_duplex"
                        )

                        // Text To Speech toggle
                        SettingToggleRow(
                            title = "TEXT-TO-SPEECH VOCALIZATION",
                            subtitle = "Speak responses using JARVIS speech synthesizer",
                            checked = uiState.userSettings.ttsEnabled,
                            onCheckedChange = { viewModel.setTtsEnabled(it) },
                            testTag = "switch_tts"
                        )

                        // Wake Word toggle ("Hey JARVIS")
                        SettingToggleRow(
                            title = "WAKE WORD DETECTION (\"Hey JARVIS\")",
                            subtitle = "Continuous background listening via Android foreground service",
                            checked = uiState.userSettings.wakeWordEnabled,
                            onCheckedChange = { viewModel.setWakeWordEnabled(it) },
                            testTag = "switch_wake_word"
                        )
                    }
                }
            }

            // 3. DEVICE PERMISSIONS & ACCESS
            item {
                SectionHeader(title = "SYSTEM CLEARANCES & ACCESS", icon = Icons.Default.Lock)
            }

            item {
                HudCard(modifier = Modifier.fillMaxWidth().testTag("permissions_config_card")) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        PermissionRow(
                            name = "Microphone (RECORD_AUDIO)",
                            granted = uiState.permissionStatus.audioGranted,
                            onOpen = { viewModel.openAppSettings() }
                        )

                        PermissionRow(
                            name = "Notification Reading Access",
                            granted = uiState.permissionStatus.notificationListenerGranted,
                            onOpen = { viewModel.openNotificationListenerSettings() }
                        )

                        PermissionRow(
                            name = "Write System Settings (Brightness)",
                            granted = uiState.permissionStatus.writeSettingsGranted,
                            onOpen = { viewModel.openWriteSettings() }
                        )
                    }
                }
            }

            // 4. APPEARANCE & POCO C61 OPTIMIZATION
            item {
                SectionHeader(title = "APPEARANCE & PERFORMANCE", icon = Icons.Default.Speed)
            }

            item {
                HudCard(modifier = Modifier.fillMaxWidth().testTag("performance_config_card")) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Performance Mode Toggle
                        SettingToggleRow(
                            title = "PERFORMANCE MODE (LOW RAM / POCO C61)",
                            subtitle = "Reduces canvas node count and limits particle passes for 4 GB RAM devices",
                            checked = uiState.userSettings.performanceMode,
                            onCheckedChange = { viewModel.setPerformanceMode(it) },
                            testTag = "switch_performance_mode"
                        )

                        // Neon Intensity Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "NEON GLOW INTENSITY",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = JarvisTextSecondary
                                )
                                Text(
                                    text = "${(neonSlider * 100).toInt()}%",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = NeonCyan
                                )
                            }
                            Slider(
                                value = neonSlider,
                                onValueChange = {
                                    neonSlider = it
                                    viewModel.setNeonIntensity(it)
                                },
                                valueRange = 0.2f..1.5f,
                                colors = SliderDefaults.colors(
                                    thumbColor = NeonCyan,
                                    activeTrackColor = NeonCyan,
                                    inactiveTrackColor = JarvisBorder
                                )
                            )
                        }
                    }
                }
            }

            // 5. ABOUT SECTION
            item {
                SectionHeader(title = "SYSTEM SPECIFICATIONS", icon = Icons.Default.Info)
            }

            item {
                HudCard(modifier = Modifier.fillMaxWidth().testTag("about_card")) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "J.A.R.V.I.S. AI ASSISTANT",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = NeonCyan
                        )
                        Text(
                            text = "Version: 1.0.0 (Release Build)",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = JarvisTextSecondary
                        )
                        Text(
                            text = "Architecture: Native Android Kotlin MVVM with Jetpack Compose & StateFlow",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = JarvisTextMuted
                        )
                        Text(
                            text = "Security: Strict allowlist device control. Zero arbitrary shell executions. CameraManager, AudioManager, and Settings APIs compliant with Google Play Policy.",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = JarvisTextMuted
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

@Composable
fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = NeonCyan,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = NeonCyan,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 11.5.sp,
                color = JarvisTextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 10.5.sp,
                color = JarvisTextMuted,
                lineHeight = 14.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = NeonCyan,
                checkedTrackColor = NeonCyan.copy(alpha = 0.3f),
                uncheckedThumbColor = JarvisTextMuted,
                uncheckedTrackColor = JarvisBorder
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
fun PermissionRow(
    name: String,
    granted: Boolean,
    onOpen: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(
                imageVector = if (granted) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = name,
                tint = if (granted) JarvisMatrixGreen else JarvisWarningAmber,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = name,
                fontSize = 11.5.sp,
                color = if (granted) JarvisTextPrimary else JarvisWarningAmber,
                fontFamily = FontFamily.Monospace
            )
        }

        TextButton(
            onClick = onOpen,
            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = if (granted) "MANAGE" else "AUTHORIZE",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = if (granted) JarvisTextSecondary else NeonCyan
            )
            Spacer(modifier = Modifier.width(2.dp))
            Icon(
                imageVector = Icons.Default.OpenInNew,
                contentDescription = "Open",
                tint = if (granted) JarvisTextSecondary else NeonCyan,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}
