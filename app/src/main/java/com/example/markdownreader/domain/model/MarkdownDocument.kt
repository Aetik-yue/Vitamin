package com.example.markdownreader.domain.model

data class MarkdownDocument(
    val uri: String,
    val displayName: String,
    val title: String,
    val category: String,
    val isFavorite: Boolean,
    val lastOpenedAt: Long,
    val size: Long?,
    val lastModified: Long?,
)
