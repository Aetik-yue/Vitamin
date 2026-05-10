package com.example.markdownreader.presentation.editor

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.markdownreader.core.file.FileUtils
import com.example.markdownreader.data.datasource.EmptyMarkdownFileException
import com.example.markdownreader.data.datasource.MarkdownPermissionException
import com.example.markdownreader.domain.repository.MarkdownRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditorViewModel(
    private val repository: MarkdownRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private var currentUri: Uri? = null

    fun load(uri: Uri?) {
        if (uri == null) {
            _uiState.value = EditorUiState(errorMessage = "File uri is empty")
            return
        }
        currentUri = uri
        _uiState.value = EditorUiState(
            isLoading = true,
            uri = uri.toString(),
            title = FileUtils.titleFromUri(uri),
        )
        viewModelScope.launch {
            runCatching {
                repository.openMarkdown(uri)
            }.onSuccess { markdown ->
                _uiState.value = EditorUiState(
                    uri = uri.toString(),
                    title = FileUtils.titleFromUri(uri),
                    content = markdown,
                )
            }.onFailure { error ->
                _uiState.value = EditorUiState(
                    uri = uri.toString(),
                    title = FileUtils.titleFromUri(uri),
                    errorMessage = error.toUserMessage(),
                )
            }
        }
    }

    fun updateContent(value: String) {
        _uiState.value = _uiState.value.copy(content = value, savedMessage = null)
    }

    fun save() {
        val uri = currentUri ?: return
        val content = _uiState.value.content
        _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
        viewModelScope.launch {
            runCatching {
                repository.saveMarkdown(uri, content)
            }.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    savedMessage = "Saved",
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = error.toUserMessage(),
                )
            }
        }
    }

    private fun Throwable.toUserMessage(): String = when (this) {
        is EmptyMarkdownFileException -> "File is empty"
        is MarkdownPermissionException -> "File permission is missing. Reopen the file with write access."
        else -> message ?: "Operation failed"
    }
}

class EditorViewModelFactory(
    private val repository: MarkdownRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        EditorViewModel(repository) as T
}
