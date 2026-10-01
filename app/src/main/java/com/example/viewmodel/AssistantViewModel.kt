package com.example.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.SalimApplication
import com.example.data.local.SettingsManager
import com.example.data.model.AgentStep
import com.example.data.model.ChatMessage
import com.example.data.repository.AssistantRepository
import com.example.data.repository.AssistantResponseResult
import com.example.data.system.PermissionManager
import com.example.data.voice.HapticFeedbackHelper
import com.example.data.voice.SpeechManager
import com.example.data.voice.TtsManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AssistantNavTab {
    ASSISTANT,
    HISTORY,
    TOOLS,
    SETTINGS
}

sealed class KeyValidationState {
    object Idle : KeyValidationState()
    object Testing : KeyValidationState()
    data class Valid(val latencyMs: Long) : KeyValidationState()
    data class Error(val message: String) : KeyValidationState()
}

class AssistantViewModel(
    private val repository: AssistantRepository,
    private val settingsManager: SettingsManager,
    private val speechManager: SpeechManager,
    private val ttsManager: TtsManager,
    private val hapticHelper: HapticFeedbackHelper
) : ViewModel() {

    val messages: StateFlow<List<ChatMessage>> = repository.allMessages.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val apiKey = settingsManager.apiKeyFlow
    val selectedModel = settingsManager.selectedModelFlow
    val ttsEnabled = settingsManager.ttsEnabledFlow
    val ttsPitch = settingsManager.ttsPitchFlow
    val ttsSpeed = settingsManager.ttsSpeedFlow
    val vibrateEnabled = settingsManager.vibrateFlow
    val autoListenEnabled = settingsManager.autoListenFlow
    val systemPrompt = settingsManager.systemPromptFlow
    val themeMode = settingsManager.themeModeFlow
    val isOnboardingCompleted = settingsManager.onboardingCompletedFlow

    val isListening: StateFlow<Boolean> = speechManager.isListening
    val rmsDb: StateFlow<Float> = speechManager.rmsDb
    val partialSpeechText: StateFlow<String> = speechManager.partialText
    val isSpeaking: StateFlow<Boolean> = ttsManager.isSpeaking

    private val _isThinking = MutableStateFlow(false)
    val isThinking: StateFlow<Boolean> = _isThinking.asStateFlow()

    private val _activePlanSteps = MutableStateFlow<List<AgentStep>>(emptyList())
    val activePlanSteps: StateFlow<List<AgentStep>> = _activePlanSteps.asStateFlow()

    private val _currentTab = MutableStateFlow(AssistantNavTab.ASSISTANT)
    val currentTab: StateFlow<AssistantNavTab> = _currentTab.asStateFlow()

    private val _isQuickSheetOpen = MutableStateFlow(false)
    val isQuickSheetOpen: StateFlow<Boolean> = _isQuickSheetOpen.asStateFlow()

    private val _keyValidationState = MutableStateFlow<KeyValidationState>(KeyValidationState.Idle)
    val keyValidationState: StateFlow<KeyValidationState> = _keyValidationState.asStateFlow()

    private val _actionNotice = MutableStateFlow<String?>(null)
    val actionNotice: StateFlow<String?> = _actionNotice.asStateFlow()

    private val _micPermissionGranted = MutableStateFlow(false)
    val micPermissionGranted: StateFlow<Boolean> = _micPermissionGranted.asStateFlow()

    private val _accessibilityActive = MutableStateFlow(false)
    val accessibilityActive: StateFlow<Boolean> = _accessibilityActive.asStateFlow()

    private val _defaultAssistantSet = MutableStateFlow(false)
    val defaultAssistantSet: StateFlow<Boolean> = _defaultAssistantSet.asStateFlow()

    init {
        speechManager.onSpeechResult = { recognizedText ->
            processPrompt(recognizedText, isVoice = true)
        }
        speechManager.onErrorOccurred = { errorMsg ->
            _actionNotice.value = errorMsg
        }
    }

    fun refreshSystemPermissions(context: Context) {
        val mic = PermissionManager.isMicrophoneGranted(context)
        _micPermissionGranted.value = mic

        val acc = PermissionManager.isAccessibilityServiceEnabled(context)
        _accessibilityActive.value = acc

        val assist = PermissionManager.isDefaultAssistant(context)
        _defaultAssistantSet.value = assist

        // If user already has mic permission, auto-complete onboarding so they are never bothered again
        if (mic && !settingsManager.isOnboardingCompleted()) {
            settingsManager.setOnboardingCompleted(true)
        }
    }

    fun completeOnboarding() {
        settingsManager.setOnboardingCompleted(true)
    }

    fun setMicPermissionGranted(granted: Boolean) {
        _micPermissionGranted.value = granted
        if (granted) {
            settingsManager.setOnboardingCompleted(true)
        }
    }

    fun setTab(tab: AssistantNavTab) {
        _currentTab.value = tab
    }

    fun setQuickSheetOpen(open: Boolean) {
        _isQuickSheetOpen.value = open
        if (open && autoListenEnabled.value && _micPermissionGranted.value) {
            startListening()
        }
    }

    fun clearActionNotice() {
        _actionNotice.value = null
    }

    fun startListening() {
        if (!_micPermissionGranted.value) {
            _actionNotice.value = "Microphone permission is required for voice commands."
            return
        }
        ttsManager.stop()
        if (vibrateEnabled.value) {
            hapticHelper.pulseActivation()
        }
        speechManager.startListening()
    }

    fun stopListening() {
        speechManager.stopListening()
    }

    fun stopSpeaking() {
        ttsManager.stop()
    }

    fun processPrompt(prompt: String, isVoice: Boolean) {
        if (prompt.isBlank()) return
        stopListening()
        stopSpeaking()

        if (settingsManager.getApiKey().isBlank()) {
            _actionNotice.value = "Please configure your Groq API key in Settings."
            _currentTab.value = AssistantNavTab.SETTINGS
            return
        }

        viewModelScope.launch {
            _isThinking.value = true
            _activePlanSteps.value = emptyList()

            val result = repository.processUserPrompt(
                prompt = prompt,
                isVoice = isVoice,
                onStepUpdate = { updatedStep ->
                    val currentList = _activePlanSteps.value.toMutableList()
                    val existingIdx = currentList.indexOfFirst { it.stepIndex == updatedStep.stepIndex }
                    if (existingIdx >= 0) {
                        currentList[existingIdx] = updatedStep
                    } else {
                        currentList.add(updatedStep)
                    }
                    _activePlanSteps.value = currentList
                }
            )

            _isThinking.value = false

            when (result) {
                is AssistantResponseResult.Success -> {
                    if (vibrateEnabled.value) {
                        hapticHelper.pulseActionSuccess()
                    }
                    if (result.lastExecutedAction != null) {
                        _actionNotice.value = result.lastExecutedAction.summary
                    }
                    if (ttsEnabled.value && !result.assistantMessage.isError) {
                        ttsManager.speak(
                            text = result.assistantMessage.content,
                            pitch = ttsPitch.value,
                            speed = ttsSpeed.value
                        )
                    }
                }
                is AssistantResponseResult.Error -> {
                    if (vibrateEnabled.value) {
                        hapticHelper.pulseActionError()
                    }
                    _actionNotice.value = result.userFriendlyMessage
                }
            }

            // Keep visible for a brief moment then clear live plan buffer
            delay(1200)
            _activePlanSteps.value = emptyList()
        }
    }

    fun saveApiKey(newKey: String) {
        settingsManager.setApiKey(newKey)
        _keyValidationState.value = KeyValidationState.Idle
    }

    fun testApiKey(key: String? = null) {
        val testTarget = (key ?: settingsManager.getApiKey()).trim()
        if (testTarget.isBlank()) {
            _keyValidationState.value = KeyValidationState.Error("Key cannot be empty.")
            return
        }

        viewModelScope.launch {
            _keyValidationState.value = KeyValidationState.Testing
            val result = repository.testApiKey(testTarget)
            result.onSuccess { latency ->
                _keyValidationState.value = KeyValidationState.Valid(latency)
            }.onFailure { ex ->
                _keyValidationState.value = KeyValidationState.Error(ex.localizedMessage ?: "Verification failed.")
            }
        }
    }

    fun setModel(modelId: String) {
        settingsManager.setSelectedModel(modelId)
    }

    fun setTtsEnabled(enabled: Boolean) {
        settingsManager.setTtsEnabled(enabled)
        if (!enabled) ttsManager.stop()
    }

    fun setTtsPitch(pitch: Float) {
        settingsManager.setTtsPitch(pitch)
    }

    fun setTtsSpeed(speed: Float) {
        settingsManager.setTtsSpeed(speed)
    }

    fun previewTtsVoice() {
        ttsManager.speak(
            text = "Hello! I am Salim, your personal assistant.",
            pitch = ttsPitch.value,
            speed = ttsSpeed.value
        )
    }

    fun setVibrateEnabled(enabled: Boolean) {
        settingsManager.setVibrateEnabled(enabled)
    }

    fun setAutoListenEnabled(enabled: Boolean) {
        settingsManager.setAutoListenEnabled(enabled)
    }

    fun setSystemPrompt(prompt: String) {
        settingsManager.setSystemPrompt(prompt)
    }

    fun resetSystemPrompt() {
        settingsManager.resetSystemPrompt()
    }

    fun setThemeMode(mode: String) {
        settingsManager.setThemeMode(mode)
    }

    fun deleteMessage(id: Long) {
        viewModelScope.launch {
            repository.deleteMessage(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun openAccessibilitySettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (ignored: Exception) {}
    }

    fun openDefaultAssistantSettings(context: Context) {
        val intentList = listOf(
            Intent(Settings.ACTION_VOICE_INPUT_SETTINGS),
            Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS),
            Intent(Settings.ACTION_SETTINGS)
        )
        for (intent in intentList) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return
            } catch (ignored: Exception) {}
        }
    }

    fun openAppSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (ignored: Exception) {}
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.stopListening()
        ttsManager.shutdown()
    }

    companion object {
        fun provideFactory(application: SalimApplication): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AssistantViewModel(
                        repository = application.repository,
                        settingsManager = application.settingsManager,
                        speechManager = application.speechManager,
                        ttsManager = application.ttsManager,
                        hapticHelper = application.hapticHelper
                    ) as T
                }
            }
    }
}
