package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.GroqModels
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("salim_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_API_KEY = "groq_api_key"
        private const val KEY_MODEL = "selected_model"
        private const val KEY_SYSTEM_PROMPT = "system_prompt"
        private const val KEY_TTS_ENABLED = "tts_enabled"
        private const val KEY_TTS_PITCH = "tts_pitch"
        private const val KEY_TTS_SPEED = "tts_speed"
        private const val KEY_VIBRATE = "vibrate_enabled"
        private const val KEY_AUTO_LISTEN = "auto_listen_on_launch"
        private const val KEY_THEME = "theme_mode"

        const val DEFAULT_SYSTEM_PROMPT = """You are Salim, an exceptionally fast, highly capable, and intelligent Android personal AI assistant designed to replace Google Assistant.
You speak clearly, concisely, and helpfully.
When a user asks you to perform an Android action (such as setting an alarm, setting a timer, turning on/off the flashlight, opening an app, calling someone, sending a message, getting current weather, or checking device battery), ALWAYS use the available function/tool calls immediately.
Keep spoken responses natural, brief, and to the point without excessive formatting or robotic boilerplate."""
    }

    private val _apiKeyFlow = MutableStateFlow(getApiKey())
    val apiKeyFlow: StateFlow<String> = _apiKeyFlow.asStateFlow()

    private val _selectedModelFlow = MutableStateFlow(getSelectedModel())
    val selectedModelFlow: StateFlow<String> = _selectedModelFlow.asStateFlow()

    private val _ttsEnabledFlow = MutableStateFlow(isTtsEnabled())
    val ttsEnabledFlow: StateFlow<Boolean> = _ttsEnabledFlow.asStateFlow()

    private val _ttsPitchFlow = MutableStateFlow(getTtsPitch())
    val ttsPitchFlow: StateFlow<Float> = _ttsPitchFlow.asStateFlow()

    private val _ttsSpeedFlow = MutableStateFlow(getTtsSpeed())
    val ttsSpeedFlow: StateFlow<Float> = _ttsSpeedFlow.asStateFlow()

    private val _vibrateFlow = MutableStateFlow(isVibrateEnabled())
    val vibrateFlow: StateFlow<Boolean> = _vibrateFlow.asStateFlow()

    private val _autoListenFlow = MutableStateFlow(isAutoListenEnabled())
    val autoListenFlow: StateFlow<Boolean> = _autoListenFlow.asStateFlow()

    private val _systemPromptFlow = MutableStateFlow(getSystemPrompt())
    val systemPromptFlow: StateFlow<String> = _systemPromptFlow.asStateFlow()

    private val _themeModeFlow = MutableStateFlow(getThemeMode())
    val themeModeFlow: StateFlow<String> = _themeModeFlow.asStateFlow()

    fun getApiKey(): String {
        val saved = prefs.getString(KEY_API_KEY, "") ?: ""
        return saved.trim()
    }

    fun setApiKey(key: String) {
        val trimmed = key.trim()
        prefs.edit().putString(KEY_API_KEY, trimmed).apply()
        _apiKeyFlow.value = trimmed
    }

    fun getSelectedModel(): String {
        return prefs.getString(KEY_MODEL, GroqModels.DEFAULT_MODEL) ?: GroqModels.DEFAULT_MODEL
    }

    fun setSelectedModel(modelId: String) {
        prefs.edit().putString(KEY_MODEL, modelId).apply()
        _selectedModelFlow.value = modelId
    }

    fun getSystemPrompt(): String {
        return prefs.getString(KEY_SYSTEM_PROMPT, DEFAULT_SYSTEM_PROMPT) ?: DEFAULT_SYSTEM_PROMPT
    }

    fun setSystemPrompt(prompt: String) {
        prefs.edit().putString(KEY_SYSTEM_PROMPT, prompt).apply()
        _systemPromptFlow.value = prompt
    }

    fun isTtsEnabled(): Boolean {
        return prefs.getBoolean(KEY_TTS_ENABLED, true)
    }

    fun setTtsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_TTS_ENABLED, enabled).apply()
        _ttsEnabledFlow.value = enabled
    }

    fun getTtsPitch(): Float {
        return prefs.getFloat(KEY_TTS_PITCH, 1.0f)
    }

    fun setTtsPitch(pitch: Float) {
        prefs.edit().putFloat(KEY_TTS_PITCH, pitch).apply()
        _ttsPitchFlow.value = pitch
    }

    fun getTtsSpeed(): Float {
        return prefs.getFloat(KEY_TTS_SPEED, 1.0f)
    }

    fun setTtsSpeed(speed: Float) {
        prefs.edit().putFloat(KEY_TTS_SPEED, speed).apply()
        _ttsSpeedFlow.value = speed
    }

    fun isVibrateEnabled(): Boolean {
        return prefs.getBoolean(KEY_VIBRATE, true)
    }

    fun setVibrateEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_VIBRATE, enabled).apply()
        _vibrateFlow.value = enabled
    }

    fun isAutoListenEnabled(): Boolean {
        return prefs.getBoolean(KEY_AUTO_LISTEN, true)
    }

    fun setAutoListenEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_LISTEN, enabled).apply()
        _autoListenFlow.value = enabled
    }

    fun getThemeMode(): String {
        return prefs.getString(KEY_THEME, "SYSTEM") ?: "SYSTEM"
    }

    fun setThemeMode(mode: String) {
        prefs.edit().putString(KEY_THEME, mode).apply()
        _themeModeFlow.value = mode
    }

    fun resetSystemPrompt() {
        setSystemPrompt(DEFAULT_SYSTEM_PROMPT)
    }
}
