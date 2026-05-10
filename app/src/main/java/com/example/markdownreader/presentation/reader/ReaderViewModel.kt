package com.example.markdownreader.presentation.reader

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.markdownreader.core.file.FileUtils
import com.example.markdownreader.data.datasource.EmptyMarkdownFileException
import com.example.markdownreader.data.datasource.FileTooLargeException
import com.example.markdownreader.data.datasource.MarkdownPermissionException
import com.example.markdownreader.domain.repository.MarkdownRepository
import com.example.markdownreader.domain.usecase.GetReadingProgressUseCase
import com.example.markdownreader.domain.usecase.OpenMarkdownFileUseCase
import com.example.markdownreader.domain.usecase.SaveReadingProgressUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.FileNotFoundException

class ReaderViewModel(
    private val repository: MarkdownRepository,
    private val openMarkdownFileUseCase: OpenMarkdownFileUseCase,
    private val saveReadingProgressUseCase: SaveReadingProgressUseCase,
    private val getReadingProgressUseCase: GetReadingProgressUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ReaderUiState())
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    private var currentUri: Uri? = null
    private var searchDebounceJob: Job? = null

    fun openDocument(uri: Uri?) {
        if (uri == null) {
            _uiState.value = ReaderUiState(errorMessage = "File uri is empty")
            return
        }

        currentUri = uri
        _uiState.value = ReaderUiState(
            isLoading = true,
            uri = uri.toString(),
            title = FileUtils.titleFromUri(uri),
        )

        viewModelScope.launch {
            runCatching {
                val markdown = openMarkdownFileUseCase(uri)
                val progress = getReadingProgressUseCase(uri.toString())
                val document = repository.getDocument(uri.toString())
                Triple(markdown, progress, document)
            }.onSuccess { (markdown, progress, document) ->
                _uiState.value = ReaderUiState(
                    isLoading = false,
                    uri = uri.toString(),
                    title = FileUtils.titleFromUri(uri),
                    displayName = document?.displayName.orEmpty(),
                    category = document?.category.orEmpty(),
                    size = document?.size,
                    lastModified = document?.lastModified,
                    lastOpenedAt = document?.lastOpenedAt,
                    markdown = markdown,
                    restoredScrollOffset = progress?.scrollOffset ?: 0,
                )
            }.onFailure { error ->
                _uiState.value = ReaderUiState(
                    isLoading = false,
                    uri = uri.toString(),
                    title = FileUtils.titleFromUri(uri),
                    errorMessage = error.toUserMessage(),
                )
            }
        }
    }

    fun reload() {
        openDocument(currentUri)
    }

    fun saveScrollPosition(offset: Int) {
        val documentUri = currentUri?.toString() ?: return
        viewModelScope.launch {
            runCatching {
                saveReadingProgressUseCase(documentUri, offset)
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        searchDebounceJob?.cancel()
        searchDebounceJob = viewModelScope.launch {
            delay(80)
            _uiState.value = _uiState.value.copy(
                effectiveSearchQuery = query,
                currentMatchIndex = if (query.isNotBlank()) 0 else -1,
            )
        }
    }

    fun nextMatch(matchCount: Int) {
        if (matchCount <= 0) return
        val current = _uiState.value.currentMatchIndex.coerceAtLeast(0)
        _uiState.value = _uiState.value.copy(
            currentMatchIndex = (current + 1) % matchCount,
        )
    }

    fun previousMatch(matchCount: Int) {
        if (matchCount <= 0) return
        val current = _uiState.value.currentMatchIndex.coerceAtLeast(0)
        _uiState.value = _uiState.value.copy(
            currentMatchIndex = if (current <= 0) matchCount - 1 else current - 1,
        )
    }

    fun toggleSearch() {
        val active = _uiState.value.isSearching
        searchDebounceJob?.cancel()
        _uiState.value = _uiState.value.copy(
            isSearching = !active,
            searchQuery = if (active) "" else _uiState.value.searchQuery,
            effectiveSearchQuery = "",
            currentMatchIndex = -1,
        )
    }

    fun loadReadingProgress(uri: Uri) {
        viewModelScope.launch {
            val progress = getReadingProgressUseCase(uri.toString())
            _uiState.value = _uiState.value.copy(
                restoredScrollOffset = progress?.scrollOffset ?: 0,
            )
        }
    }

    private fun Throwable.toUserMessage(): String = when (this) {
        is EmptyMarkdownFileException -> "File is empty"
        is MarkdownPermissionException -> "File permission expired. Please reopen the file."
        is FileTooLargeException -> "File is too large to open at once"
        is FileNotFoundException -> "File does not exist or was moved"
        else -> message ?: "Failed to read Markdown file"
    }
}

class ReaderViewModelFactory(
    private val repository: MarkdownRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ReaderViewModel(
            repository = repository,
            openMarkdownFileUseCase = OpenMarkdownFileUseCase(repository),
            saveReadingProgressUseCase = SaveReadingProgressUseCase(repository),
            getReadingProgressUseCase = GetReadingProgressUseCase(repository),
        ) as T
    }
}
