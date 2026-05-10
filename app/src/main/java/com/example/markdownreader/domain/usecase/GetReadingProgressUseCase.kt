package com.example.markdownreader.domain.usecase

import com.example.markdownreader.domain.repository.MarkdownRepository

class GetReadingProgressUseCase(
    private val repository: MarkdownRepository,
) {
    suspend operator fun invoke(documentUri: String) = repository.getReadingProgress(documentUri)
}
