package com.example.presentation.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.JarvisState
import com.example.presentation.components.ActionFeedbackBanner
import com.example.presentation.components.BatteryCard
import com.example.presentation.components.ChatBubble
import com.example.presentation.components.CpuCard
import com.example.presentation.components.HudCard
import com.example.presentation.components.JarvisOrb
import com.example.presentation.components.MemoryCard
import com.example.presentation.components.NetworkCard
import com.example.presentation.components.PulsingNeonCircle
import com.example.presentation.components.QuickActionGrid
import com.example.presentation.components.jarvisBackground
import com.example.presentation.viewmodel.JarvisUiState
import com.example.presentation.viewmodel.JarvisViewModel
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.ElectricPurpleBright
import com.example.ui.theme.ElectricPurpleGlow
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
import com.example.ui.theme.NeonCyanGlow

@Composable
fun HomeScreen(
    uiState: JarvisUiState,
    viewModel: JarvisViewModel,
    onNavigateToChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    var textInput by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .jarvisBackground()
    ) {
        // TOP HUD BAR
        TopHudBar(
            uiState = uiState,
            onToggleHandsFree = { viewModel.toggleHandsFreeMode() }
        )

        // SCROLLABLE CONTENT
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. ANIMATED AI ORB WITH HOLOGRAPHIC PROJECTION AURA
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .drawBehind {
                            // Holographic projector aura on floor/backdrop
                            val w = size.width
                            val h = size.height
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        when (uiState.jarvisState) {
                                            JarvisState.LISTENING -> NeonCyanGlow.copy(alpha = 0.35f)
                                            JarvisState.THINKING -> ElectricPurpleGlow.copy(alpha = 0.35f)
                                            JarvisState.SPEAKING -> NeonCyanGlow.copy(alpha = 0.3f)
                                            else -> NeonCyanGlow.copy(alpha = 0.15f)
                                        },
                                        Color.Transparent
                                    ),
                                    center = Offset(w / 2f, h / 2f),
                                    radius = w * 0.48f
                                ),
                                center = Offset(w / 2f, h / 2f),
                                radius = w * 0.48f
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    JarvisOrb(
                        state = uiState.jarvisState,
                        rmsLevel = uiState.rmsAudioLevel,
                        statusText = uiState.statusText,
                        performanceMode = uiState.userSettings.performanceMode,
                        onClick = {
                            if (uiState.isListening) viewModel.stopListening()
                            else viewModel.startListening()
                        }
                    )
                }
            }

            // 1.5 HANDS-FREE MODE BUTTON
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    val isHf = uiState.isHandsFreeActive
                    val shape = RoundedCornerShape(20.dp)
                    val borderStroke = if (isHf) {
                        BorderStroke(1.2.dp, Brush.horizontalGradient(listOf(JarvisMatrixGreen, NeonCyan, JarvisMatrixGreen)))
                    } else {
                        BorderStroke(1.dp, JarvisBorder)
                    }

                    Box(
                        modifier = Modifier
                            .clip(shape)
                            .then(
                                if (isHf) Modifier.background(JarvisMatrixGreen.copy(alpha = 0.14f), shape)
                                else Modifier.background(JarvisGlassBrush, shape)
                            )
                            .border(borderStroke, shape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { viewModel.toggleHandsFreeMode() }
                            )
                            .padding(horizontal = 16.dp, vertical = 7.dp)
                            .testTag("btn_toggle_hands_free"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(8.dp),
                                shape = CircleShape,
                                color = if (isHf) JarvisMatrixGreen else JarvisTextMuted
                            ) {}
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isHf) "HANDS-FREE DUPLEX ACTIVE" else "ACTIVATE HANDS-FREE DUPLEX",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isHf) JarvisMatrixGreen else JarvisTextSecondary,
                                letterSpacing = 0.8.sp
                            )
                        }
                    }
                }
            }

            // 2. LIVE SPEECH TRANSCRIPTION CARD WITH PULSING NEON VISUALIZER
            if (uiState.isListening || uiState.liveTranscript.isNotBlank()) {
                item {
                    HudCard(
                        borderBrush = JarvisNeonBorderBrush,
                        backgroundColor = JarvisSurfaceElevated,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("live_transcript_card")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Custom Pulsing Neon Circle Animation
                            PulsingNeonCircle(
                                isListening = uiState.isListening,
                                rmsLevel = uiState.rmsAudioLevel,
                                size = 76.dp,
                                onClick = {
                                    if (uiState.isListening) viewModel.stopListening()
                                    else viewModel.startListening()
                                }
                            )

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            modifier = Modifier.size(8.dp),
                                            shape = CircleShape,
                                            color = if (uiState.isListening) NeonCyan else JarvisTextMuted
                                        ) {}
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (uiState.isListening) "NEURAL RECEPTOR LIVE" else "PROCESSING INPUT",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NeonCyan,
                                            letterSpacing = 1.sp
                                        )
                                    }

                                    if (uiState.isListening) {
                                        Text(
                                            text = "${(uiState.rmsAudioLevel * 10).toInt()}% dB",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 10.sp,
                                            color = JarvisTextSecondary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = if (uiState.liveTranscript.isNotBlank()) uiState.liveTranscript else "Listening for speech...",
                                    fontSize = 13.5.sp,
                                    color = JarvisTextPrimary,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }

            // 3. ACTION FEEDBACK BANNER (IF PRESENT)
            if (uiState.lastActionFeedback != null) {
                item {
                    ActionFeedbackBanner(feedback = uiState.lastActionFeedback)
                }
            }

            // 4. QUICK ACTIONS GRID
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

            // 5. SYSTEM STATUS TELEMETRY SECTION
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "HARDWARE TELEMETRY STREAM",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = JarvisTextSecondary,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "POCO C61 OPTIMIZED",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.5.sp,
                        color = JarvisTextMuted,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BatteryCard(
                        deviceStatus = uiState.deviceStatus,
                        modifier = Modifier.weight(1f)
                    )
                    MemoryCard(
                        deviceStatus = uiState.deviceStatus,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NetworkCard(
                        deviceStatus = uiState.deviceStatus,
                        modifier = Modifier.weight(1f)
                    )
                    CpuCard(
                        deviceStatus = uiState.deviceStatus,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 6. CONVERSATION PREVIEW SECTION
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "NEURAL CONVERSATION STREAM",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = JarvisTextSecondary,
                        letterSpacing = 1.5.sp
                    )

                    Button(
                        onClick = onNavigateToChat,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = NeonCyan
                        ),
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.testTag("btn_view_full_chat")
                    ) {
                        Text(
                            text = "FULL LOG",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "View chat",
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            // Show last 2 messages as preview
            val previewMessages = uiState.messages.takeLast(2)
            items(previewMessages.size) { index ->
                val msg = previewMessages[index]
                ChatBubble(
                    message = msg,
                    onSpeak = { text -> viewModel.speak(text) },
                    onRetry = { viewModel.retryLastPrompt() }
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // BOTTOM INPUT CONTROLS BAR
        BottomInputBar(
            textInput = textInput,
            onTextChanged = { textInput = it },
            isListening = uiState.isListening,
            isHandsFreeActive = uiState.isHandsFreeActive,
            onToggleMic = {
                if (uiState.isListening) viewModel.stopListening()
                else viewModel.startListening()
            },
            onSend = {
                if (textInput.isNotBlank()) {
                    viewModel.submitUserPrompt(textInput)
                    textInput = ""
                    focusManager.clearFocus()
                }
            }
        )
    }
}

@Composable
fun TopHudBar(
    uiState: JarvisUiState,
    onToggleHandsFree: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "BeaconPulse")
    val beaconAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BeaconAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(JarvisGlassBrush)
            .drawBehind {
                // Specular bottom hairline glow
                val strokePx = 1.dp.toPx()
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            NeonCyan.copy(alpha = 0.5f),
                            ElectricPurple.copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    ),
                    start = Offset(0f, size.height - strokePx / 2f),
                    end = Offset(size.width, size.height - strokePx / 2f),
                    strokeWidth = strokePx
                )
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "J.A.R.V.I.S.",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = NeonCyan,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = JarvisMatrixGreen.copy(alpha = 0.16f),
                        border = BorderStroke(0.6.dp, JarvisMatrixGreen.copy(alpha = 0.6f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = JarvisMatrixGreen.copy(alpha = beaconAlpha),
                                modifier = Modifier.size(5.dp)
                            ) {}
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "ONLINE",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = JarvisMatrixGreen
                            )
                        }
                    }
                }
                Text(
                    text = "ADVANCED AI TACTICAL INTERFACE",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 8.5.sp,
                    color = JarvisTextMuted,
                    letterSpacing = 1.sp
                )
            }

            // Hands-Free, Battery, and RAM pills
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    onClick = onToggleHandsFree,
                    shape = RoundedCornerShape(12.dp),
                    color = if (uiState.isHandsFreeActive) JarvisMatrixGreen.copy(alpha = 0.2f) else JarvisSurfaceElevated,
                    border = BorderStroke(0.8.dp, if (uiState.isHandsFreeActive) JarvisMatrixGreen else JarvisBorder),
                    modifier = Modifier.testTag("top_pill_hands_free")
                ) {
                    Text(
                        text = if (uiState.isHandsFreeActive) "DUPLEX ON" else "DUPLEX",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.isHandsFreeActive) JarvisMatrixGreen else JarvisTextMuted,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
                StatusPill(
                    label = if (uiState.deviceStatus.batteryLevel >= 0) "${uiState.deviceStatus.batteryLevel}%" else "--",
                    color = NeonCyan
                )
                StatusPill(
                    label = "${uiState.deviceStatus.ramUsagePercent}% RAM",
                    color = ElectricPurpleBright
                )
            }
        }
    }
}

@Composable
fun StatusPill(label: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(0.8.dp, color.copy(alpha = 0.45f))
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun BottomInputBar(
    textInput: String,
    onTextChanged: (String) -> Unit,
    isListening: Boolean,
    onToggleMic: () -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
    isHandsFreeActive: Boolean = false
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(JarvisGlassBrush)
            .drawBehind {
                // Specular top hairline border
                val strokePx = 1.dp.toPx()
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            if (isHandsFreeActive) JarvisMatrixGreen.copy(alpha = 0.6f) else NeonCyan.copy(alpha = 0.45f),
                            ElectricPurple.copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    ),
                    start = Offset(0f, strokePx / 2f),
                    end = Offset(size.width, strokePx / 2f),
                    strokeWidth = strokePx
                )
            }
            .imePadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Voice Microphone Button
            IconButton(
                onClick = onToggleMic,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isListening -> NeonCyan.copy(alpha = 0.28f)
                            isHandsFreeActive -> JarvisMatrixGreen.copy(alpha = 0.22f)
                            else -> JarvisSurfaceElevated
                        }
                    )
                    .border(
                        BorderStroke(
                            1.2.dp,
                            when {
                                isListening -> NeonCyan
                                isHandsFreeActive -> JarvisMatrixGreen
                                else -> JarvisBorder
                            }
                        ),
                        CircleShape
                    )
                    .testTag("btn_mic_toggle")
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = if (isListening) "Stop listening" else "Start voice input",
                    tint = when {
                        isListening -> NeonCyan
                        isHandsFreeActive -> JarvisMatrixGreen
                        else -> JarvisTextPrimary
                    },
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Text input field
            OutlinedTextField(
                value = textInput,
                onValueChange = onTextChanged,
                placeholder = {
                    Text(
                        text = if (isListening) "Receptor active: speak now..." else "Command or prompt...",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.5.sp,
                        color = JarvisTextMuted
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("input_prompt_field"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = JarvisBorder,
                    focusedTextColor = JarvisTextPrimary,
                    unfocusedTextColor = JarvisTextPrimary,
                    focusedContainerColor = JarvisSurfaceElevated,
                    unfocusedContainerColor = JarvisSurfaceElevated,
                    cursorColor = NeonCyan
                ),
                maxLines = 3,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() })
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Send Button
            IconButton(
                onClick = onSend,
                enabled = textInput.isNotBlank(),
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        if (textInput.isNotBlank()) NeonCyan else JarvisBorder.copy(alpha = 0.35f)
                    )
                    .testTag("btn_send_prompt")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send prompt",
                    tint = if (textInput.isNotBlank()) JarvisBackground else JarvisTextMuted,
                    modifier = Modifier.size(19.dp)
                )
            }
        }
    }
}
