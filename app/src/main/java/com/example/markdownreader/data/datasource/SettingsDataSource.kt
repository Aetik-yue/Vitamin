package com.example.markdownreader.data.datasource

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore("settings")

data class SettingsSnapshot(
    val fontSize: Float,
    val isDarkTheme: Boolean,
    val rememberRecentFiles: Boolean,
    val languageCode: String,
)

class SettingsDataSource(
    private val context: Context,
) {
    private val fontSizeKey = floatPreferencesKey("font_size")
    private val darkThemeKey = booleanPreferencesKey("dark_theme")
    private val rememberRecentKey = booleanPreferencesKey("remember_recent_files")
    private val languageKey = stringPreferencesKey("language_code")

    val settingsFlow = context.settingsDataStore.data.map { prefs ->
        SettingsSnapshot(
            fontSize = prefs[fontSizeKey] ?: 16f,
            isDarkTheme = prefs[darkThemeKey] ?: false,
            rememberRecentFiles = prefs[rememberRecentKey] ?: true,
            languageCode = prefs[languageKey] ?: "zh",
        )
    }

    suspend fun setFontSize(value: Float) {
        context.settingsDataStore.edit { it[fontSizeKey] = value.coerceIn(12f, 28f) }
    }

    suspend fun setDarkTheme(value: Boolean) {
        context.settingsDataStore.edit { it[darkThemeKey] = value }
    }

    suspend fun setRememberRecentFiles(value: Boolean) {
        context.settingsDataStore.edit { it[rememberRecentKey] = value }
    }

    suspend fun setLanguageCode(value: String) {
        context.settingsDataStore.edit { it[languageKey] = if (value == "en") "en" else "zh" }
    }
}
