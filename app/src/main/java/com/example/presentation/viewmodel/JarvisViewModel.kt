package com.example.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.JarvisPreferences
import com.example.data.model.ActionFeedback
import com.example.data.model.ActionStatus
import com.example.data.model.ChatMessage
import com.example.data.model.DeviceStatus
import com.example.data.model.JarvisState
import com.example.data.model.MessageSender
import com.example.data.model.UserSettings
import com.example.domain.ai.GeminiRepository
import com.example.domain.command.CommandProcessor
import com.example.domain.command.CommandResult
import com.example.domain.device.AppLauncherRepository
import com.example.domain.device.DeviceControlRepository
import com.example.domain.device.DeviceStatusRepository
import com.example.domain.permission.PermissionManager
import com.example.domain.permission.PermissionStatus
import com.example.domain.voice.JarvisTtsManager
import com.example.domain.voice.VoiceInputEvent
import com.example.domain.voice.VoiceInputManager
import com.example.service.JarvisNotificationListenerService
import com.example.service.JarvisVoiceService
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class JarvisUiState(
    val jarvisState: JarvisState = JarvisState.IDLE,
    val statusText: String = "JARVIS ONLINE",
    val messages: List<ChatMessage> = listOf(
        ChatMessage(
            text = "Systems online, sir. All core diagnostics nominal. How may I be of assistance?",
            sender = MessageSender.JARVIS
        )
    ),
    val deviceStatus: DeviceStatus = DeviceStatus(),
    val liveTranscript: String = "",
    val rmsAudioLevel: Float = 0f,
    val lastActionFeedback: ActionFeedback? = null,
    val userSettings: UserSettings = UserSettings(),
    val permissionStatus: PermissionStatus = PermissionStatus(false, false, false, false, false),
    val isFlashlightOn: Boolean = false,
    val isTtsReady: Boolean = false,
    val isListening: Boolean = false,
    val activeNotificationCount: Int = 0,
    val isHandsFreeActive: Boolean = false
)

class JarvisViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences = JarvisPreferences(application)
    private val aiRepository = GeminiRepository(preferences = preferences)
    private val deviceControl = DeviceControlRepository(application)
    private val appLauncher = AppLauncherRepository(application)
    private val deviceStatusRepo = DeviceStatusRepository(application)
    private val commandProcessor = CommandProcessor(deviceControl, appLauncher)
    private val permissionManager = PermissionManager(application)

    private val voiceInputManager = VoiceInputManager(application)
    private var ttsManager: JarvisTtsManager? = null

    private val _uiState = MutableStateFlow(JarvisUiState())
    val uiState: StateFlow<JarvisUiState> = _uiState.asStateFlow()

    private var activeAiJob: Job? = null
    private var lastPrompt: String? = null

    init {
        initTts()
        setupVoiceListeners()
        observePreferences()
        observeDeviceStatus()
        observeNotifications()
        refreshPermissions()
    }

    private var consecutiveSilenceErrors = 0

    private fun initTts() {
        ttsManager = JarvisTtsManager(getApplication()) { isSpeaking ->
            if (isSpeaking) {
                _uiState.update {
                    it.copy(
                        jarvisState = JarvisState.SPEAKING,
                        statusText = "JARVIS VOCALIZING"
                    )
                }
            } else {
                if (_uiState.value.jarvisState == JarvisState.SPEAKING) {
                    val isHandsFree = _uiState.value.isHandsFreeActive
                    _uiState.update {
                        it.copy(
                            jarvisState = JarvisState.IDLE,
                            statusText = if (isHandsFree) "HANDS-FREE LISTENING..." else "JARVIS ONLINE"
                        )
                    }

                    // Auto-listen loop in hands-free mode
                    if (isHandsFree) {
                        viewModelScope.launch {
                            kotlinx.coroutines.delay(450L)
                            if (_uiState.value.isHandsFreeActive && !_uiState.value.isListening) {
                                startListening()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setupVoiceListeners() {
        voiceInputManager.onEvent = { event ->
            when (event) {
                is VoiceInputEvent.Ready -> {
                    consecutiveSilenceErrors = 0
                    _uiState.update {
                        it.copy(
                            jarvisState = JarvisState.LISTENING,
                            statusText = if (it.isHandsFreeActive) "HANDS-FREE ACTIVE (LISTENING...)" else "LISTENING...",
                            isListening = true,
                            liveTranscript = ""
                        )
                    }
                }
                is VoiceInputEvent.BeginningOfSpeech -> {
                    _uiState.update { it.copy(statusText = "DETECTING SPEECH...") }
                }
                is VoiceInputEvent.RmsChanged -> {
                    _uiState.update { it.copy(rmsAudioLevel = event.rmsDb) }
                }
                is VoiceInputEvent.PartialResult -> {
                    _uiState.update { it.copy(liveTranscript = event.text) }
                }
                is VoiceInputEvent.FinalResult -> {
                    consecutiveSilenceErrors = 0
                    _uiState.update {
                        it.copy(
                            liveTranscript = event.text,
                            rmsAudioLevel = 0f,
                            isListening = false
                        )
                    }
                    submitUserPrompt(event.text)
                }
                is VoiceInputEvent.EndOfSpeech -> {
                    _uiState.update {
                        it.copy(
                            isListening = false,
                            rmsAudioLevel = 0f,
                            statusText = if (it.jarvisState == JarvisState.LISTENING) "PROCESSING..." else it.statusText
                        )
                    }
                }
                is VoiceInputEvent.Error -> {
                    _uiState.update {
                        it.copy(
                            isListening = false,
                            rmsAudioLevel = 0f,
                            jarvisState = if (it.isHandsFreeActive) JarvisState.IDLE else JarvisState.ERROR,
                            statusText = if (it.isHandsFreeActive) "HANDS-FREE STANDBY" else event.message,
                            lastActionFeedback = ActionFeedback(
                                status = ActionStatus.UNAVAILABLE,
                                title = "Voice Recognition",
                                message = event.message
                            )
                        )
                    }

                    // In hands-free mode, if silence/timeout occurred once, retry after gentle pause
                    if (_uiState.value.isHandsFreeActive) {
                        consecutiveSilenceErrors++
                        if (consecutiveSilenceErrors < 2) {
                            viewModelScope.launch {
                                kotlinx.coroutines.delay(1200L)
                                if (_uiState.value.isHandsFreeActive && !_uiState.value.isListening) {
                                    startListening()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun observePreferences() {
        viewModelScope.launch {
            preferences.userSettingsFlow.collectLatest { settings ->
                _uiState.update {
                    it.copy(
                        userSettings = settings,
                        isHandsFreeActive = settings.handsFreeMode
                    )
                }
                JarvisNotificationListenerService.isReadingEnabled = settings.notificationReadingEnabled

                // Wake word foreground service management
                if (settings.wakeWordEnabled && permissionManager.isAudioPermissionGranted()) {
                    JarvisVoiceService.start(getApplication())
                } else {
                    JarvisVoiceService.stop(getApplication())
                }
            }
        }
    }

    private fun observeDeviceStatus() {
        viewModelScope.launch {
            deviceStatusRepo.getStatusFlow(6000L).collectLatest { status ->
                _uiState.update { it.copy(deviceStatus = status) }
            }
        }
        viewModelScope.launch {
            deviceControl.isFlashlightOn.collectLatest { on ->
                _uiState.update { it.copy(isFlashlightOn = on) }
            }
        }
    }

    private fun observeNotifications() {
        viewModelScope.launch {
            JarvisNotificationListenerService.notifications.collectLatest { notifs ->
                _uiState.update { it.copy(activeNotificationCount = notifs.size) }
            }
        }
    }

    fun refreshPermissions() {
        _uiState.update {
            it.copy(permissionStatus = permissionManager.getCurrentStatus())
        }
    }

    fun startListening() {
        if (!permissionManager.isAudioPermissionGranted()) {
            _uiState.update {
                it.copy(
                    jarvisState = JarvisState.ERROR,
                    statusText = "Microphone Permission Required",
                    lastActionFeedback = ActionFeedback(
                        status = ActionStatus.PERMISSION_REQUIRED,
                        title = "Permission Required",
                        message = "JARVIS requires Audio Recording permission to receive voice input."
                    )
                )
            }
            return
        }

        stopSpeaking()
        voiceInputManager.startListening()
    }

    fun stopListening() {
        voiceInputManager.stopListening()
        _uiState.update {
            it.copy(
                isListening = false,
                rmsAudioLevel = 0f,
                jarvisState = if (it.jarvisState == JarvisState.LISTENING) JarvisState.IDLE else it.jarvisState,
                statusText = if (it.jarvisState == JarvisState.LISTENING) "JARVIS ONLINE" else it.statusText
            )
        }
    }

    fun submitUserPrompt(input: String) {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return

        lastPrompt = trimmed

        // Append user message
        val userMsg = ChatMessage(text = trimmed, sender = MessageSender.USER)
        _uiState.update { it.copy(messages = it.messages + userMsg) }

        // Process through CommandProcessor
        val currentStatus = _uiState.value.deviceStatus
        when (val result = commandProcessor.processInput(trimmed, currentStatus)) {
            is CommandResult.Handled -> {
                if (result.commandName == "Hands-Free ON") {
                    _uiState.update { it.copy(isHandsFreeActive = true) }
                    viewModelScope.launch { preferences.setHandsFreeMode(true) }
                } else if (result.commandName == "Hands-Free OFF") {
                    _uiState.update { it.copy(isHandsFreeActive = false) }
                    viewModelScope.launch { preferences.setHandsFreeMode(false) }
                    stopListening()
                }

                val jarvisMsg = ChatMessage(
                    text = result.speechResponse,
                    sender = MessageSender.JARVIS,
                    executedAction = result.commandName
                )
                _uiState.update {
                    it.copy(
                        messages = it.messages + jarvisMsg,
                        lastActionFeedback = result.feedback,
                        jarvisState = JarvisState.IDLE,
                        statusText = "JARVIS ONLINE"
                    )
                }

                if (_uiState.value.userSettings.ttsEnabled) {
                    speak(result.speechResponse)
                } else if (_uiState.value.isHandsFreeActive) {
                    viewModelScope.launch {
                        kotlinx.coroutines.delay(800L)
                        if (_uiState.value.isHandsFreeActive && !_uiState.value.isListening) {
                            startListening()
                        }
                    }
                }
            }
            is CommandResult.RequiresAi -> {
                executeAiQuery(result.query)
            }
        }
    }

    private fun executeAiQuery(prompt: String) {
        activeAiJob?.cancel()

        _uiState.update {
            it.copy(
                jarvisState = JarvisState.THINKING,
                statusText = "THINKING..."
            )
        }

        val aiMsgId = java.util.UUID.randomUUID().toString()
        val initialAiMsg = ChatMessage(
            id = aiMsgId,
            text = "",
            sender = MessageSender.JARVIS,
            isStreaming = true
        )

        _uiState.update { it.copy(messages = it.messages + initialAiMsg) }

        activeAiJob = viewModelScope.launch {
            val responseBuilder = StringBuilder()

            aiRepository.sendMessage(prompt, _uiState.value.messages)
                .catch { e ->
                    val errorText = "Diagnostic failure communicating with AI core: ${e.localizedMessage ?: "Unknown error"}"
                    _uiState.update { state ->
                        state.copy(
                            jarvisState = JarvisState.ERROR,
                            statusText = "COMMUNICATION FAILURE",
                            messages = state.messages.map { msg ->
                                if (msg.id == aiMsgId) msg.copy(
                                    text = errorText,
                                    isStreaming = false,
                                    isError = true
                                ) else msg
                            }
                        )
                    }
                }
                .collect { chunk ->
                    responseBuilder.append(chunk)
                    val currentText = responseBuilder.toString()

                    _uiState.update { state ->
                        state.copy(
                            messages = state.messages.map { msg ->
                                if (msg.id == aiMsgId) msg.copy(
                                    text = currentText,
                                    isStreaming = true
                                ) else msg
                            }
                        )
                    }
                }

            val finalText = responseBuilder.toString().ifBlank {
                "Command acknowledged, sir, but AI output was empty."
            }

            _uiState.update { state ->
                state.copy(
                    jarvisState = JarvisState.IDLE,
                    statusText = "JARVIS ONLINE",
                    messages = state.messages.map { msg ->
                        if (msg.id == aiMsgId) msg.copy(
                            text = finalText,
                            isStreaming = false
                        ) else msg
                    }
                )
            }

            if (_uiState.value.userSettings.ttsEnabled) {
                speak(finalText)
            }
        }
    }

    fun retryLastPrompt() {
        val prompt = lastPrompt ?: return
        submitUserPrompt(prompt)
    }

    fun speak(text: String) {
        ttsManager?.speak(text)
    }

    fun stopSpeaking() {
        ttsManager?.stop()
        if (_uiState.value.jarvisState == JarvisState.SPEAKING) {
            _uiState.update {
                it.copy(
                    jarvisState = JarvisState.IDLE,
                    statusText = "JARVIS ONLINE"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.update {
            it.copy(
                messages = listOf(
                    ChatMessage(
                        text = "Diagnostics nominal. Conversation buffer cleared. Ready for your instructions.",
                        sender = MessageSender.JARVIS
                    )
                )
            )
        }
    }

    // Quick Action Handlers
    fun toggleFlashlight() {
        val fb = deviceControl.toggleFlashlight()
        _uiState.update { it.copy(lastActionFeedback = fb) }
        if (fb.status == ActionStatus.SUCCESS) {
            speak(if (_uiState.value.isFlashlightOn) "Flashlight engaged" else "Flashlight turned off")
        }
    }

    fun adjustVolume(direction: Int) {
        val fb = deviceControl.adjustVolume(direction)
        _uiState.update { it.copy(lastActionFeedback = fb) }
    }

    fun setBrightness(percent: Int) {
        val fb = deviceControl.setBrightness(percent)
        _uiState.update { it.copy(lastActionFeedback = fb) }
    }

    fun openWifi() {
        val fb = deviceControl.handleWifiAction()
        _uiState.update { it.copy(lastActionFeedback = fb) }
    }

    fun openBluetooth() {
        val fb = deviceControl.handleBluetoothAction()
        _uiState.update { it.copy(lastActionFeedback = fb) }
    }

    fun openSettings() {
        val fb = deviceControl.openSystemSettings()
        _uiState.update { it.copy(lastActionFeedback = fb) }
    }

    fun launchApp(appName: String) {
        val fb = appLauncher.launchAppByName(appName)
        _uiState.update { it.copy(lastActionFeedback = fb) }
    }

    // Settings
    fun setWakeWordEnabled(enabled: Boolean) {
        viewModelScope.launch { preferences.setWakeWordEnabled(enabled) }
    }

    fun setTtsEnabled(enabled: Boolean) {
        viewModelScope.launch { preferences.setTtsEnabled(enabled) }
    }

    fun setPerformanceMode(enabled: Boolean) {
        viewModelScope.launch { preferences.setPerformanceMode(enabled) }
    }

    fun setNeonIntensity(intensity: Float) {
        viewModelScope.launch { preferences.setNeonIntensity(intensity) }
    }

    fun setNotificationReading(enabled: Boolean) {
        viewModelScope.launch { preferences.setNotificationReadingEnabled(enabled) }
    }

    fun setCustomApiKey(key: String) {
        viewModelScope.launch { preferences.setCustomApiKey(key) }
    }

    fun toggleHandsFreeMode() {
        val newState = !_uiState.value.isHandsFreeActive
        _uiState.update { it.copy(isHandsFreeActive = newState) }
        viewModelScope.launch { preferences.setHandsFreeMode(newState) }

        if (newState) {
            val announcement = "Hands-free continuous interaction engaged, sir. Speak whenever you wish."
            speak(announcement)
            _uiState.update {
                it.copy(
                    lastActionFeedback = ActionFeedback(
                        status = ActionStatus.SUCCESS,
                        title = "Hands-Free Protocol Active",
                        message = "Continuous conversational listening activated."
                    )
                )
            }
        } else {
            stopListening()
            val announcement = "Hands-free interaction paused."
            speak(announcement)
            _uiState.update {
                it.copy(
                    lastActionFeedback = ActionFeedback(
                        status = ActionStatus.SUCCESS,
                        title = "Hands-Free Disengaged",
                        message = "Continuous listening disengaged."
                    )
                )
            }
        }
    }

    fun openAppSettings() = permissionManager.openAppSettings()
    fun openNotificationListenerSettings() = permissionManager.openNotificationListenerSettings()
    fun openWriteSettings() = permissionManager.openWriteSettings()

    override fun onCleared() {
        voiceInputManager.destroy()
        ttsManager?.shutdown()
        super.onCleared()
    }
}
