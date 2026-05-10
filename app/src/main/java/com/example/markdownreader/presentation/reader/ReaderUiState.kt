package com.example.markdownreader.presentation.reader

data class ReaderUiState(
    val isLoading: Boolean = false,
    val uri: String? = null,
    val title: String = "",
    val displayName: String = "",
    val category: String = "",
    val size: Long? = null,
    val lastModified: Long? = null,
    val lastOpenedAt: Long? = null,
    val markdown: String = "",
    val restoredScrollOffset: Int = 0,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val effectiveSearchQuery: String = "",
    val isSearching: Boolean = false,
    val currentMatchIndex: Int = -1,
) {
    val isEmpty: Boolean get() = !isLoading && errorMessage == null && markdown.isBlank()
}
