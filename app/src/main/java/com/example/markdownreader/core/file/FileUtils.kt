package com.example.markdownreader.core.file

import android.net.Uri

object FileUtils {
    fun isMarkdownFile(displayName: String): Boolean {
        val lower = displayName.lowercase()
        return lower.endsWith(".md") || lower.endsWith(".markdown")
    }

    fun titleFromUri(uri: Uri): String {
        return uri.lastPathSegment
            ?.substringAfterLast('/')
            ?.substringBeforeLast('.', missingDelimiterValue = uri.lastPathSegment ?: "Markdown")
            ?: "Markdown"
    }
}
