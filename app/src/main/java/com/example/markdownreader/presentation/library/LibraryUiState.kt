package com.example.markdownreader.presentation.library

import com.example.markdownreader.domain.model.MarkdownDocument

enum class LibrarySortMode {
    TIME,
    NAME,
    SIZE,
}

data class LibraryUiState(
    val documents: List<MarkdownDocument> = emptyList(),
    val categories: List<String> = listOf("All"),
    val selectedCategory: String = "All",
    val sortMode: LibrarySortMode = LibrarySortMode.TIME,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
