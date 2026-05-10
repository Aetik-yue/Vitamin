package com.example.markdownreader.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.markdownreader.data.datasource.SettingsDataSource
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val dataSource: SettingsDataSource,
) : ViewModel() {
    val uiState = dataSource.settingsFlow
        .map {
            SettingsUiState(
                fontSize = it.fontSize,
                isDarkTheme = it.isDarkTheme,
                rememberRecentFiles = it.rememberRecentFiles,
                languageCode = it.languageCode,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState(),
        )

    fun setFontSize(value: Float) {
        viewModelScope.launch {
            dataSource.setFontSize(value)
        }
    }

    fun setDarkTheme(value: Boolean) {
        viewModelScope.launch {
            dataSource.setDarkTheme(value)
        }
    }

    fun setRememberRecentFiles(value: Boolean) {
        viewModelScope.launch {
            dataSource.setRememberRecentFiles(value)
        }
    }

    fun setLanguageCode(value: String) {
        viewModelScope.launch {
            dataSource.setLanguageCode(value)
        }
    }
}

class SettingsViewModelFactory(
    private val dataSource: SettingsDataSource,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SettingsViewModel(dataSource) as T
    }
}
