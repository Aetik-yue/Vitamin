package com.example.markdownreader.presentation.settings

data class SettingsUiState(
    val fontSize: Float = 16f,
    val isDarkTheme: Boolean = false,
    val rememberRecentFiles: Boolean = true,
    val languageCode: String = "zh",
)
