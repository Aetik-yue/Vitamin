package com.example.markdownreader.core.file

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract

object FolderScanner {

    data class MarkdownFileEntry(
        val uri: Uri,
        val displayName: String,
        val size: Long,
        val lastModified: Long,
    )

    fun scanForMarkdownFiles(context: Context, treeUri: Uri): List<MarkdownFileEntry> {
        val entries = mutableListOf<MarkdownFileEntry>()
        val rootDocId = DocumentsContract.getTreeDocumentId(treeUri)
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, rootDocId)
        scanRecursive(context, treeUri, childrenUri, entries)
        return entries
    }

    private fun scanRecursive(
        context: Context,
        treeUri: Uri,
        parentUri: Uri,
        entries: MutableList<MarkdownFileEntry>,
    ) {
        val cursor = context.contentResolver.query(
            parentUri,
            arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE,
                DocumentsContract.Document.COLUMN_SIZE,
                DocumentsContract.Document.COLUMN_LAST_MODIFIED,
            ),
            null, null, null,
        ) ?: return

        cursor.use {
            while (it.moveToNext()) {
                val docId = it.getString(0) ?: continue
                val name = it.getString(1) ?: continue
                val mimeType = it.getString(2)
                val size = it.getLong(3)
                val lastModified = it.getLong(4)

                if (mimeType == DocumentsContract.Document.MIME_TYPE_DIR) {
                    val childUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, docId)
                    scanRecursive(context, treeUri, childUri, entries)
                } else if (name.lowercase().let { n ->
                        n.endsWith(".md") || n.endsWith(".markdown")
                    }) {
                    entries.add(
                        MarkdownFileEntry(
                            uri = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId),
                            displayName = name,
                            size = if (size >= 0) size else 0,
                            lastModified = if (lastModified >= 0) lastModified else 0,
                        )
                    )
                }
            }
        }
    }
}
