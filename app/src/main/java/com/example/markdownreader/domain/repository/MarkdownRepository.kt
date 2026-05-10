package com.example.markdownreader.domain.repository

import android.net.Uri
import com.example.markdownreader.domain.model.MarkdownDocument
import com.example.markdownreader.domain.model.ReadingProgress
import kotlinx.coroutines.flow.Flow

interface MarkdownRepository {
    fun observeRecentDocuments(): Flow<List<MarkdownDocument>>
    fun observeCategories(): Flow<List<String>>
    suspend fun getDocument(uri: String): MarkdownDocument?
    suspend fun openMarkdown(uri: Uri): String
    suspend fun saveMarkdown(uri: Uri, content: String)
    suspend fun createMarkdown(uri: Uri, content: String)
    suspend fun saveRecentDocument(uri: Uri)
    suspend fun importDocument(uri: Uri, displayName: String, size: Long, lastModified: Long)
    suspend fun updateDocumentCategory(uri: String, category: String)
    suspend fun renameDocument(uri: String, displayName: String)
    suspend fun setFavorite(uri: String, isFavorite: Boolean)
    suspend fun deleteRecentDocument(uri: String)
    suspend fun clearRecentDocuments()
    suspend fun saveReadingProgress(documentUri: String, scrollOffset: Int)
    suspend fun getReadingProgress(documentUri: String): ReadingProgress?
}
