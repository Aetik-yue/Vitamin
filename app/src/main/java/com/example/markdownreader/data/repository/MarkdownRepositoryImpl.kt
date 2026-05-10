package com.example.markdownreader.data.repository

import android.net.Uri
import com.example.markdownreader.data.datasource.MarkdownFileDataSource
import com.example.markdownreader.data.db.DocumentDao
import com.example.markdownreader.data.db.DocumentEntity
import com.example.markdownreader.data.db.ReadingProgressDao
import com.example.markdownreader.data.db.ReadingProgressEntity
import com.example.markdownreader.data.db.toDomain
import com.example.markdownreader.domain.model.MarkdownDocument
import com.example.markdownreader.domain.model.ReadingProgress
import com.example.markdownreader.domain.repository.MarkdownRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MarkdownRepositoryImpl(
    private val fileDataSource: MarkdownFileDataSource,
    private val documentDao: DocumentDao,
    private val progressDao: ReadingProgressDao,
    private val rememberRecentFiles: () -> Boolean,
) : MarkdownRepository {
    override fun observeRecentDocuments(): Flow<List<MarkdownDocument>> =
        documentDao.observeRecentDocuments().map { list -> list.map { it.toDomain() } }

    override fun observeCategories(): Flow<List<String>> =
        documentDao.observeCategories().map { categories ->
            listOf("All") + categories.filter { it.isNotBlank() }
        }

    override suspend fun getDocument(uri: String): MarkdownDocument? =
        documentDao.getDocument(uri)?.toDomain()

    override suspend fun openMarkdown(uri: Uri): String {
        val content = fileDataSource.readMarkdown(uri)
        if (rememberRecentFiles()) saveRecentDocument(uri)
        return content
    }

    override suspend fun saveMarkdown(uri: Uri, content: String) {
        fileDataSource.writeMarkdown(uri, content)
        if (rememberRecentFiles()) saveRecentDocument(uri)
    }

    override suspend fun createMarkdown(uri: Uri, content: String) {
        fileDataSource.writeMarkdown(uri, content)
        saveRecentDocument(uri)
    }

    override suspend fun saveRecentDocument(uri: Uri) {
        val meta = fileDataSource.queryMetadata(uri)
        val existingCategory = documentDao.getCategory(uri.toString()) ?: "未分类"
        val existingDocument = documentDao.getDocument(uri.toString())
        documentDao.upsertDocument(
            DocumentEntity(
                uri = uri.toString(),
                displayName = existingDocument?.displayName ?: meta.displayName,
                title = existingDocument?.title ?: meta.displayName.substringBeforeLast('.'),
                category = existingCategory,
                isFavorite = existingDocument?.isFavorite ?: false,
                lastOpenedAt = System.currentTimeMillis(),
                size = meta.size,
                lastModified = meta.lastModified,
            )
        )
    }

    override suspend fun updateDocumentCategory(uri: String, category: String) {
        documentDao.updateCategory(uri, category.ifBlank { "未分类" })
    }

    override suspend fun renameDocument(uri: String, displayName: String) {
        val trimmed = displayName.trim()
        if (trimmed.isNotEmpty()) {
            documentDao.renameDocument(uri, trimmed, trimmed.substringBeforeLast('.'))
        }
    }

    override suspend fun importDocument(uri: Uri, displayName: String, size: Long, lastModified: Long) {
        val existing = documentDao.getDocument(uri.toString())
        documentDao.upsertDocument(
            DocumentEntity(
                uri = uri.toString(),
                displayName = existing?.displayName ?: displayName,
                title = existing?.title ?: displayName.substringBeforeLast('.'),
                category = existing?.category ?: "未分类",
                isFavorite = existing?.isFavorite ?: false,
                lastOpenedAt = existing?.lastOpenedAt ?: System.currentTimeMillis(),
                size = if (size > 0) size else existing?.size,
                lastModified = if (lastModified > 0) lastModified else existing?.lastModified,
            )
        )
    }

    override suspend fun setFavorite(uri: String, isFavorite: Boolean) {
        documentDao.setFavorite(uri, isFavorite)
    }

    override suspend fun deleteRecentDocument(uri: String) {
        documentDao.deleteDocument(uri)
        progressDao.deleteProgress(uri)
    }

    override suspend fun clearRecentDocuments() {
        documentDao.clearDocuments()
    }

    override suspend fun saveReadingProgress(documentUri: String, scrollOffset: Int) {
        progressDao.upsertProgress(
            ReadingProgressEntity(
                documentUri = documentUri,
                scrollOffset = scrollOffset.coerceAtLeast(0),
                updatedAt = System.currentTimeMillis(),
            )
        )
    }

    override suspend fun getReadingProgress(documentUri: String): ReadingProgress? =
        progressDao.getProgress(documentUri)?.toDomain()
}
