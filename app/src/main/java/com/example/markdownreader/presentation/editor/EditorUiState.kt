package com.example.markdownreader.presentation.editor

data class EditorUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val uri: String? = null,
    val title: String = "",
    val content: String = "",
    val errorMessage: String? = null,
    val savedMessage: String? = null,
)
