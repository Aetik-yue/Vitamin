package com.example.markdownreader.presentation.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.example.markdownreader.core.common.AppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    uiState: EditorUiState,
    strings: AppStrings,
    onBack: () -> Unit,
    onContentChange: (String) -> Unit,
    onSave: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.title.ifBlank { strings.editor }) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text(strings.back)
                    }
                },
                actions = {
                    TextButton(onClick = onSave, enabled = !uiState.isSaving && !uiState.isLoading) {
                        Text(if (uiState.isSaving) strings.saving else strings.save)
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            when {
                uiState.isLoading -> CircularProgressIndicator()
                uiState.errorMessage != null -> Text(uiState.errorMessage)
                uiState.savedMessage != null -> Text(strings.saved)
            }

            OutlinedTextField(
                value = uiState.content,
                onValueChange = onContentChange,
                modifier = Modifier.fillMaxSize(),
                textStyle = TextStyle(fontFamily = FontFamily.Monospace),
                label = { Text(strings.markdown) },
            )
        }
    }
}
