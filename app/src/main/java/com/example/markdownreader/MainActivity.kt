package com.example.markdownreader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.markdownreader.core.navigation.AppNavGraph
import com.example.markdownreader.core.theme.MarkdownReaderTheme
import com.example.markdownreader.presentation.settings.SettingsViewModel
import com.example.markdownreader.presentation.settings.SettingsViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val container = (application as MarkdownReaderApplication).appContainer

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModelFactory(container.settingsDataSource)
            )
            val settingsUiState by settingsViewModel.uiState.collectAsState()

            MarkdownReaderTheme(darkTheme = settingsUiState.isDarkTheme) {
                AppNavGraph(
                    repository = container.markdownRepository,
                    settingsDataSource = container.settingsDataSource,
                )
            }
        }
    }
}
