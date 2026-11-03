package com.example.domain.voice

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface WakeWordEngine {
    val isRunning: StateFlow<Boolean>
    fun start(onWakeWordDetected: () -> Unit)
    fun stop()
    fun destroy()
}

/**
 * Standard implementation of the WakeWordEngine for Android.
 * Listens for "Hey JARVIS" or "JARVIS" using Android's speech detection pipeline
 * and triggers the wake callback when the keyword phrase is heard.
 */
class DefaultWakeWordEngine(
    private val context: Context
) : WakeWordEngine {

    private val _isRunning = MutableStateFlow(false)
    override val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private var voiceInputManager: VoiceInputManager? = null
    private var callback: (() -> Unit)? = null

    override fun start(onWakeWordDetected: () -> Unit) {
        if (_isRunning.value) return
        this.callback = onWakeWordDetected
        _isRunning.value = true

        voiceInputManager = VoiceInputManager(context).apply {
            onEvent = { event ->
                when (event) {
                    is VoiceInputEvent.PartialResult -> checkPhrase(event.text)
                    is VoiceInputEvent.FinalResult -> checkPhrase(event.text)
                    is VoiceInputEvent.EndOfSpeech, is VoiceInputEvent.Error -> {
                        // Restart recognition loop if engine is still active
                        if (_isRunning.value) {
                            try {
                                startListening()
                            } catch (e: Exception) {
                                // wait next cycle
                            }
                        }
                    }
                    else -> {}
                }
            }
            try {
                startListening()
            } catch (e: Exception) {
                _isRunning.value = false
            }
        }
    }

    private fun checkPhrase(text: String) {
        val lower = text.lowercase()
        if (lower.contains("jarvis") || lower.contains("hey jarvis") || lower.contains("ok jarvis")) {
            callback?.invoke()
        }
    }

    override fun stop() {
        _isRunning.value = false
        voiceInputManager?.stopListening()
        voiceInputManager = null
        callback = null
    }

    override fun destroy() {
        stop()
    }
}
