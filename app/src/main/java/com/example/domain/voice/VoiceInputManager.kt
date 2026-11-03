package com.example.domain.voice

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

sealed class VoiceInputEvent {
    object Ready : VoiceInputEvent()
    object BeginningOfSpeech : VoiceInputEvent()
    data class RmsChanged(val rmsDb: Float) : VoiceInputEvent()
    data class PartialResult(val text: String) : VoiceInputEvent()
    data class FinalResult(val text: String) : VoiceInputEvent()
    data class Error(val message: String, val errorCode: Int) : VoiceInputEvent()
    object EndOfSpeech : VoiceInputEvent()
}

class VoiceInputManager(private val context: Context) {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var speechRecognizer: SpeechRecognizer? = null

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _liveTranscript = MutableStateFlow("")
    val liveTranscript: StateFlow<String> = _liveTranscript.asStateFlow()

    private val _rmsLevel = MutableStateFlow(0f)
    val rmsLevel: StateFlow<Float> = _rmsLevel.asStateFlow()

    var onEvent: ((VoiceInputEvent) -> Unit)? = null

    val isRecognitionAvailable: Boolean
        get() = SpeechRecognizer.isRecognitionAvailable(context)

    fun startListening() {
        mainHandler.post {
            if (!isRecognitionAvailable) {
                onEvent?.invoke(VoiceInputEvent.Error("Speech recognition is unavailable on this device", -1))
                return@post
            }

            // Cleanup previous instance if still active
            cleanupInternal()

            try {
                // Prefer On-Device Speech Recognizer on Android 12+ (API 31+) if supported
                val recognizer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    SpeechRecognizer.isOnDeviceRecognitionAvailable(context)
                ) {
                    try {
                        SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
                    } catch (e: Exception) {
                        SpeechRecognizer.createSpeechRecognizer(context)
                    }
                } else {
                    SpeechRecognizer.createSpeechRecognizer(context)
                }

                speechRecognizer = recognizer

                recognizer.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isListening.value = true
                        _liveTranscript.value = ""
                        vibrateBriefly(25L)
                        onEvent?.invoke(VoiceInputEvent.Ready)
                    }

                    override fun onBeginningOfSpeech() {
                        onEvent?.invoke(VoiceInputEvent.BeginningOfSpeech)
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        // Normalize RMS level between 0 and 10 for audio orb visualizer
                        val clamped = rmsdB.coerceIn(0f, 10f)
                        _rmsLevel.value = clamped
                        onEvent?.invoke(VoiceInputEvent.RmsChanged(clamped))
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _isListening.value = false
                        _rmsLevel.value = 0f
                        onEvent?.invoke(VoiceInputEvent.EndOfSpeech)
                    }

                    override fun onError(error: Int) {
                        _isListening.value = false
                        _rmsLevel.value = 0f
                        val message = when (error) {
                            SpeechRecognizer.ERROR_AUDIO -> "Audio recording failure"
                            SpeechRecognizer.ERROR_CLIENT -> "Client speech recognition error"
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Audio permission required"
                            SpeechRecognizer.ERROR_NETWORK -> "Network error during speech recognition"
                            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                            SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized"
                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer is busy"
                            SpeechRecognizer.ERROR_SERVER -> "Recognition server error"
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input heard"
                            else -> "Recognition issue (code $error)"
                        }
                        onEvent?.invoke(VoiceInputEvent.Error(message, error))
                    }

                    override fun onResults(results: Bundle?) {
                        _isListening.value = false
                        _rmsLevel.value = 0f
                        vibrateBriefly(40L)
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull() ?: ""
                        _liveTranscript.value = text
                        if (text.isNotBlank()) {
                            onEvent?.invoke(VoiceInputEvent.FinalResult(text))
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull() ?: ""
                        if (text.isNotBlank()) {
                            _liveTranscript.value = text
                            onEvent?.invoke(VoiceInputEvent.PartialResult(text))
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)

                    // Pacing and natural speech timeouts for hands-free conversations
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1200L)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 1200L)
                }

                recognizer.startListening(intent)
            } catch (e: Exception) {
                _isListening.value = false
                onEvent?.invoke(VoiceInputEvent.Error(e.localizedMessage ?: "Failed to initiate listening", -1))
            }
        }
    }

    fun stopListening() {
        mainHandler.post {
            cleanupInternal()
        }
    }

    private fun cleanupInternal() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            // Ignore safe cleanup
        } finally {
            speechRecognizer = null
            _isListening.value = false
            _rmsLevel.value = 0f
        }
    }

    private fun vibrateBriefly(durationMs: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(durationMs)
                }
            }
        } catch (e: Exception) {
            // Haptic is optional
        }
    }

    fun destroy() {
        stopListening()
    }
}
