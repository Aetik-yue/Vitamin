package com.example.markdownreader.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.markdownreader.domain.model.MarkdownDocument

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey val uri: String,
    val displayName: String,
    val title: String,
    val category: String = "未分类",
    val isFavorite: Boolean = false,
    val lastOpenedAt: Long,
    val size: Long?,
    val lastModified: Long?,
)

fun DocumentEntity.toDomain() = MarkdownDocument(
    uri = uri,
    displayName = displayName,
    title = title,
    category = category,
    isFavorite = isFavorite,
    lastOpenedAt = lastOpenedAt,
    size = size,
    lastModified = lastModified,
)
