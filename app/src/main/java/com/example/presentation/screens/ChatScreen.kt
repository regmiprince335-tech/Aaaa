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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.JarvisState
import com.example.presentation.components.ChatBubble
import com.example.presentation.components.PulsingNeonCircle
import com.example.presentation.viewmodel.JarvisUiState
import com.example.presentation.viewmodel.JarvisViewModel
import com.example.presentation.components.JarvisScreenHeader
import com.example.presentation.components.jarvisBackground
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisBorder
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.NeonCyan

@Composable
fun ChatScreen(
    uiState: JarvisUiState,
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current

    // Auto-scroll to the latest message
    LaunchedEffect(uiState.messages.size, uiState.messages.lastOrNull()?.text) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .jarvisBackground()
    ) {
        // TOP CONVERSATION HEADER
        JarvisScreenHeader(
            title = "NEURAL CONVERSATION LOG",
            subtitle = "${uiState.messages.size} entries recorded"
        ) {
            if (uiState.jarvisState == JarvisState.SPEAKING) {
                IconButton(
                    onClick = { viewModel.stopSpeaking() },
                    modifier = Modifier.testTag("btn_silence_tts")
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeOff,
                        contentDescription = "Silence audio",
                        tint = NeonCyan
                    )
                }
            }

            IconButton(
                onClick = { viewModel.clearMessages() },
                modifier = Modifier.testTag("btn_clear_conversation")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = "Clear conversation",
                    tint = JarvisTextSecondary
                )
            }
        }

        // MESSAGES STREAM
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(uiState.messages, key = { it.id }) { msg ->
                ChatBubble(
                    message = msg,
                    onSpeak = { text -> viewModel.speak(text) },
                    onRetry = { viewModel.retryLastPrompt() }
                )
            }
        }

        // LIVE TRANSCRIPT CARD IN CHAT WITH PULSING NEON VISUALIZER
        if (uiState.isListening || uiState.liveTranscript.isNotBlank()) {
            Surface(
                color = JarvisSurface,
                border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.6f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PulsingNeonCircle(
                        isListening = uiState.isListening,
                        rmsLevel = uiState.rmsAudioLevel,
                        size = 52.dp,
                        onClick = {
                            if (uiState.isListening) viewModel.stopListening()
                            else viewModel.startListening()
                        }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (uiState.isListening) "LISTENING TO MICROPHONE" else "SPEECH CAPTURED",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = NeonCyan,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (uiState.liveTranscript.isNotBlank()) uiState.liveTranscript else "Speak now...",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = JarvisTextPrimary
                        )
                    }
                }
            }
        }

        // BOTTOM INPUT BAR
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
