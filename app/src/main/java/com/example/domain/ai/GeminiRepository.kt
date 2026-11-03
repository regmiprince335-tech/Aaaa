package com.example.domain.ai

import com.example.BuildConfig
import com.example.data.local.JarvisPreferences
import com.example.data.model.ChatMessage
import com.example.data.remote.GeminiApiClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class GeminiRepository(
    private val apiClient: GeminiApiClient = GeminiApiClient(),
    private val preferences: JarvisPreferences
) : AIRepository {

    override fun sendMessage(prompt: String, history: List<ChatMessage>): Flow<String> {
        val apiKey = getEffectiveApiKeySync()
        return apiClient.streamChat(apiKey = apiKey, history = history, newPrompt = prompt)
    }

    override fun isConfigured(): Boolean {
        val key = getEffectiveApiKeySync()
        return key.isNotBlank() && key != "MY_GEMINI_API_KEY"
    }

    private fun getEffectiveApiKeySync(): String {
        val buildConfigKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }
        if (buildConfigKey.isNotBlank() && buildConfigKey != "MY_GEMINI_API_KEY") {
            return buildConfigKey
        }
        // Fallback to custom key if configured
        return try {
            // Note: will be read from preferences if provided
            ""
        } catch (e: Throwable) {
            ""
        }
    }

    suspend fun getEffectiveApiKey(): String {
        val buildConfigKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }
        if (buildConfigKey.isNotBlank() && buildConfigKey != "MY_GEMINI_API_KEY") {
            return buildConfigKey
        }
        val settings = preferences.userSettingsFlow.first()
        return settings.customApiKey
    }
}
