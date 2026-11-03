package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "jarvis_preferences")

class JarvisPreferences(private val context: Context) {

    companion object {
        val KEY_WAKE_WORD = booleanPreferencesKey("wake_word_enabled")
        val KEY_TTS = booleanPreferencesKey("tts_enabled")
        val KEY_PERFORMANCE_MODE = booleanPreferencesKey("performance_mode")
        val KEY_NEON_INTENSITY = floatPreferencesKey("neon_intensity")
        val KEY_NOTIFICATIONS = booleanPreferencesKey("notification_reading_enabled")
        val KEY_CUSTOM_API_KEY = stringPreferencesKey("custom_api_key")
        val KEY_HANDS_FREE = booleanPreferencesKey("hands_free_mode")
    }

    val userSettingsFlow: Flow<UserSettings> = context.dataStore.data.map { prefs ->
        UserSettings(
            wakeWordEnabled = prefs[KEY_WAKE_WORD] ?: false,
            ttsEnabled = prefs[KEY_TTS] ?: true,
            performanceMode = prefs[KEY_PERFORMANCE_MODE] ?: false,
            neonIntensity = prefs[KEY_NEON_INTENSITY] ?: 1.0f,
            notificationReadingEnabled = prefs[KEY_NOTIFICATIONS] ?: false,
            customApiKey = prefs[KEY_CUSTOM_API_KEY] ?: "",
            handsFreeMode = prefs[KEY_HANDS_FREE] ?: false
        )
    }

    suspend fun setWakeWordEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_WAKE_WORD] = enabled }
    }

    suspend fun setTtsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_TTS] = enabled }
    }

    suspend fun setPerformanceMode(enabled: Boolean) {
        context.dataStore.edit { it[KEY_PERFORMANCE_MODE] = enabled }
    }

    suspend fun setNeonIntensity(intensity: Float) {
        context.dataStore.edit { it[KEY_NEON_INTENSITY] = intensity }
    }

    suspend fun setNotificationReadingEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_NOTIFICATIONS] = enabled }
    }

    suspend fun setCustomApiKey(key: String) {
        context.dataStore.edit { it[KEY_CUSTOM_API_KEY] = key }
    }

    suspend fun setHandsFreeMode(enabled: Boolean) {
        context.dataStore.edit { it[KEY_HANDS_FREE] = enabled }
    }
}
