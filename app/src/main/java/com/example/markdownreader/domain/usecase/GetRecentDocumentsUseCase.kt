package com.example.markdownreader.domain.usecase

import com.example.markdownreader.domain.repository.MarkdownRepository

class GetRecentDocumentsUseCase(
    private val repository: MarkdownRepository,
) {
    operator fun invoke() = repository.observeRecentDocuments()
}
