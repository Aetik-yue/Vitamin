package com.example.markdownreader.domain.usecase

import android.net.Uri
import com.example.markdownreader.domain.repository.MarkdownRepository

class SaveRecentDocumentUseCase(
    private val repository: MarkdownRepository,
) {
    suspend operator fun invoke(uri: Uri) = repository.saveRecentDocument(uri)
}
