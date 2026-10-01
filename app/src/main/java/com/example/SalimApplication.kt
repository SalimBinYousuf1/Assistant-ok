package com.example

import android.app.Application
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.SettingsManager
import com.example.data.repository.AssistantRepository
import com.example.data.tools.SystemActionExecutor
import com.example.data.voice.HapticFeedbackHelper
import com.example.data.voice.SpeechManager
import com.example.data.voice.TtsManager

class SalimApplication : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var settingsManager: SettingsManager
        private set

    lateinit var actionExecutor: SystemActionExecutor
        private set

    lateinit var repository: AssistantRepository
        private set

    lateinit var speechManager: SpeechManager
        private set

    lateinit var ttsManager: TtsManager
        private set

    lateinit var hapticHelper: HapticFeedbackHelper
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(this)
        settingsManager = SettingsManager(this)
        actionExecutor = SystemActionExecutor(this)
        speechManager = SpeechManager(this)
        ttsManager = TtsManager(this)
        hapticHelper = HapticFeedbackHelper(this)

        repository = AssistantRepository(
            chatMessageDao = database.chatMessageDao(),
            settingsManager = settingsManager,
            actionExecutor = actionExecutor
        )

        // Preload BuildConfig GROQ_API_KEY if available and not yet set by user
        try {
            val buildConfigKey = try {
                val field = BuildConfig::class.java.getField("GROQ_API_KEY")
                field.get(null) as? String
            } catch (e: Exception) {
                null
            }

            if (!buildConfigKey.isNullOrBlank() && settingsManager.getApiKey().isBlank()) {
                settingsManager.setApiKey(buildConfigKey)
                Log.d("SalimApp", "Injected GROQ_API_KEY from build configuration")
            }
        } catch (e: Exception) {
            Log.w("SalimApp", "No buildConfig key to load", e)
        }
    }
}
