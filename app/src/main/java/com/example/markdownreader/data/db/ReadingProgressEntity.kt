package com.example.markdownreader.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.markdownreader.domain.model.ReadingProgress

@Entity(tableName = "reading_progress")
data class ReadingProgressEntity(
    @PrimaryKey val documentUri: String,
    val scrollOffset: Int,
    val updatedAt: Long,
)

fun ReadingProgressEntity.toDomain() = ReadingProgress(
    documentUri = documentUri,
    scrollOffset = scrollOffset,
    updatedAt = updatedAt,
)
