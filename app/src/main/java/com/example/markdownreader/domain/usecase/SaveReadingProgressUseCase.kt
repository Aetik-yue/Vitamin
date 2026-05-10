package com.example.markdownreader.domain.usecase

import com.example.markdownreader.domain.repository.MarkdownRepository

class SaveReadingProgressUseCase(
    private val repository: MarkdownRepository,
) {
    suspend operator fun invoke(documentUri: String, scrollOffset: Int) {
        repository.saveReadingProgress(documentUri, scrollOffset)
    }
}
