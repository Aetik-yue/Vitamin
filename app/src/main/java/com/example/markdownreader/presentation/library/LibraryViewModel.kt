package com.example.markdownreader.presentation.library

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.markdownreader.core.file.FolderScanner
import com.example.markdownreader.domain.model.MarkdownDocument
import com.example.markdownreader.domain.repository.MarkdownRepository
import com.example.markdownreader.domain.usecase.GetRecentDocumentsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LibraryViewModel(
    private val repository: MarkdownRepository,
    getRecentDocumentsUseCase: GetRecentDocumentsUseCase,
) : ViewModel() {
    private val selectedCategory = MutableStateFlow("All")
    private val sortMode = MutableStateFlow(LibrarySortMode.TIME)

    val uiState = combine(
        getRecentDocumentsUseCase(),
        repository.observeCategories(),
        selectedCategory,
        sortMode,
    ) { documents, categories, selected, sort ->
        val filtered = if (selected == "All") {
            documents
        } else {
            documents.filter { it.category == selected }
        }
        LibraryUiState(
            documents = filtered.sortedWith(sort.comparator()),
            categories = categories.ifEmpty { listOf("All") },
            selectedCategory = selected,
            sortMode = sort,
        )
    }
        .catch { emit(LibraryUiState(errorMessage = "加载文档库失败")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LibraryUiState(isLoading = true),
        )

    fun selectCategory(category: String) {
        selectedCategory.value = category
    }

    fun setSortMode(mode: LibrarySortMode) {
        sortMode.value = mode
    }

    fun updateCategory(uri: String, category: String) {
        viewModelScope.launch {
            repository.updateDocumentCategory(uri, category)
        }
    }

    fun renameDocument(uri: String, displayName: String) {
        viewModelScope.launch {
            repository.renameDocument(uri, displayName)
        }
    }

    fun setFavorite(uri: String, isFavorite: Boolean) {
        viewModelScope.launch {
            repository.setFavorite(uri, isFavorite)
        }
    }

    fun createMarkdown(uri: Uri, title: String = "Untitled note", onCreated: () -> Unit = {}) {
        viewModelScope.launch {
            repository.createMarkdown(uri, "# $title\n\n")
            onCreated()
        }
    }

    fun importFolder(context: Context, folderUri: Uri) {
        viewModelScope.launch {
            val files = withContext(Dispatchers.IO) {
                FolderScanner.scanForMarkdownFiles(context, folderUri)
            }
            for (file in files) {
                repository.importDocument(file.uri, file.displayName, file.size, file.lastModified)
            }
        }
    }

    fun deleteRecent(uri: String) {
        viewModelScope.launch {
            repository.deleteRecentDocument(uri)
        }
    }

    fun clearRecent() {
        viewModelScope.launch {
            repository.clearRecentDocuments()
        }
    }
}

private val timeComparator = compareByDescending<MarkdownDocument> { it.lastOpenedAt }
private val nameComparator = compareBy<MarkdownDocument> { it.displayName.lowercase() }
    .thenByDescending { it.lastOpenedAt }
private val sizeComparator = compareByDescending<MarkdownDocument> { it.size ?: 0L }
    .thenBy { it.displayName.lowercase() }

private fun LibrarySortMode.comparator(): Comparator<MarkdownDocument> = when (this) {
    LibrarySortMode.TIME -> timeComparator
    LibrarySortMode.NAME -> nameComparator
    LibrarySortMode.SIZE -> sizeComparator
}

class LibraryViewModelFactory(
    private val repository: MarkdownRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LibraryViewModel(
            repository = repository,
            getRecentDocumentsUseCase = GetRecentDocumentsUseCase(repository),
        ) as T
    }
}
