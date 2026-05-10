package com.example.markdownreader.domain.usecase

import android.net.Uri
import com.example.markdownreader.domain.repository.MarkdownRepository

class OpenMarkdownFileUseCase(
    private val repository: MarkdownRepository,
) {
    suspend operator fun invoke(uri: Uri): String = repository.openMarkdown(uri)
}
