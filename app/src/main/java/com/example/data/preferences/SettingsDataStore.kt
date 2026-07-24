package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class SettingsDataStore(private val context: Context) {

    companion object {
        val RECITATION_SPEED_KEY = floatPreferencesKey("recitation_speed")
        val IS_DARK_MODE_KEY = booleanPreferencesKey("is_dark_mode")
        val IS_ENGLISH_LANGUAGE_KEY = booleanPreferencesKey("is_english_language")
        val FONT_SCALE_KEY = floatPreferencesKey("font_scale")
        val SELECTED_RECITER_KEY = stringPreferencesKey("selected_reciter")
        val AUTO_SCROLL_KEY = booleanPreferencesKey("auto_scroll")
        val TAJWEED_COLORING_KEY = booleanPreferencesKey("tajweed_coloring")
    }

    val recitationSpeed: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[RECITATION_SPEED_KEY] ?: 1.0f
    }

    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[IS_DARK_MODE_KEY] ?: false
    }

    val isEnglishLanguage: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[IS_ENGLISH_LANGUAGE_KEY] ?: false
    }

    val fontScale: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[FONT_SCALE_KEY] ?: 1.0f
    }

    val selectedReciter: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[SELECTED_RECITER_KEY] ?: "الشيخ مشاري العفاسي"
    }

    val autoScrollEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[AUTO_SCROLL_KEY] ?: true
    }

    val tajweedColoringEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[TAJWEED_COLORING_KEY] ?: true
    }

    suspend fun saveRecitationSpeed(speed: Float) {
        context.dataStore.edit { prefs ->
            prefs[RECITATION_SPEED_KEY] = speed
        }
    }

    suspend fun saveIsDarkMode(isDark: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[IS_DARK_MODE_KEY] = isDark
        }
    }

    suspend fun saveIsEnglishLanguage(isEnglish: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[IS_ENGLISH_LANGUAGE_KEY] = isEnglish
        }
    }

    suspend fun saveFontScale(scale: Float) {
        context.dataStore.edit { prefs ->
            prefs[FONT_SCALE_KEY] = scale
        }
    }

    suspend fun saveSelectedReciter(reciter: String) {
        context.dataStore.edit { prefs ->
            prefs[SELECTED_RECITER_KEY] = reciter
        }
    }

    suspend fun saveAutoScroll(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[AUTO_SCROLL_KEY] = enabled
        }
    }

    suspend fun saveTajweedColoring(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[TAJWEED_COLORING_KEY] = enabled
        }
    }
}
