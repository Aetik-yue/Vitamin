package com.example.markdownreader.domain.model

data class ReadingProgress(
    val documentUri: String,
    val scrollOffset: Int,
    val updatedAt: Long,
)
