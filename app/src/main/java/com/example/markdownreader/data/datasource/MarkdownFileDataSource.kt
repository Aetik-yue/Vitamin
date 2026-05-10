package com.example.markdownreader.data.datasource

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import com.example.markdownreader.core.common.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

data class FileMetadata(
    val displayName: String,
    val size: Long?,
    val lastModified: Long?,
)

class EmptyMarkdownFileException : IOException("File is empty")
class MarkdownPermissionException : IOException("Missing permission for this file")
class FileTooLargeException(val fileSize: Long) : IOException("File is too large to open at once")
class MarkdownReadException(message: String, cause: Throwable? = null) : IOException(message, cause)
class MarkdownWriteException(message: String, cause: Throwable? = null) : IOException(message, cause)

class MarkdownFileDataSource(
    private val contentResolver: ContentResolver,
) {
    suspend fun readMarkdown(uri: Uri): String = withContext(Dispatchers.IO) {
        try {
            val metadata = queryMetadata(uri)
            metadata.size?.let { size ->
                if (size > Constants.LARGE_FILE_WARNING_BYTES * 10) {
                    throw FileTooLargeException(size)
                }
            }

            val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: throw FileNotFoundException("Cannot open file input stream")

            val text = when (metadata.displayName.extension()) {
                "docx" -> readDocx(bytes)
                "doc" -> readLegacyDoc(bytes)
                else -> bytes.toString(Charsets.UTF_8)
            }

            if (text.isBlank()) throw EmptyMarkdownFileException()
            text
        } catch (e: SecurityException) {
            throw MarkdownPermissionException()
        } catch (e: FileNotFoundException) {
            throw MarkdownReadException("File does not exist or was moved", e)
        } catch (e: EmptyMarkdownFileException) {
            throw e
        } catch (e: FileTooLargeException) {
            throw e
        } catch (e: IOException) {
            throw MarkdownReadException("Failed to read file", e)
        }
    }

    suspend fun writeMarkdown(uri: Uri, content: String) = withContext(Dispatchers.IO) {
        try {
            val metadata = queryMetadata(uri)
            val bytes = when (metadata.displayName.extension()) {
                "docx" -> createSimpleDocx(content)
                "doc" -> content.toByteArray(Charsets.UTF_8)
                else -> content.toByteArray(Charsets.UTF_8)
            }

            contentResolver.openOutputStream(uri, "wt")?.use { output ->
                output.write(bytes)
                output.flush()
            } ?: throw FileNotFoundException("Cannot open file output stream")
        } catch (e: SecurityException) {
            throw MarkdownPermissionException()
        } catch (e: IOException) {
            throw MarkdownWriteException("Failed to save file", e)
        }
    }

    suspend fun queryMetadata(uri: Uri): FileMetadata = withContext(Dispatchers.IO) {
        var name: String? = null
        var size: Long? = null
        var modified: Long? = null

        val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
            if (it.moveToFirst()) {
                if (nameIndex >= 0) name = it.getString(nameIndex)
                if (sizeIndex >= 0 && !it.isNull(sizeIndex)) size = it.getLong(sizeIndex)
            }
        }

        modified = runCatching {
            contentResolver.openAssetFileDescriptor(uri, "r")?.use { descriptor ->
                descriptor.length.takeIf { it >= 0 }
            }
        }.getOrNull()

        FileMetadata(
            displayName = name ?: uri.lastPathSegment ?: "Untitled",
            size = size,
            lastModified = modified,
        )
    }

    private fun readDocx(bytes: ByteArray): String {
        ZipInputStream(bytes.inputStream()).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                if (entry.name == "word/document.xml") {
                    val xml = zip.readBytes().toString(Charsets.UTF_8)
                    return xml
                        .replace("</w:p>", "<w:t>\n</w:t>")
                        .let { textXml ->
                            Regex("<w:t[^>]*>(.*?)</w:t>")
                                .findAll(textXml)
                                .joinToString("") { it.groupValues[1].unescapeXml() }
                        }
                        .replace(Regex("\n{3,}"), "\n\n")
                        .trim()
                }
            }
        }
        throw IOException("DOCX document body not found")
    }

    private fun readLegacyDoc(bytes: ByteArray): String {
        val utf16 = runCatching {
            String(bytes, Charsets.UTF_16LE)
                .filter { it == '\n' || it == '\r' || it == '\t' || it.code in 32..0x9FFF }
                .replace(Regex("[\\u0000-\\u001F&&[^\\n\\r\\t]]"), " ")
                .replace(Regex(" {2,}"), " ")
                .trim()
        }.getOrDefault("")

        if (utf16.length > 20) return utf16

        return bytes
            .map { it.toInt() and 0xff }
            .map { if (it in 32..126 || it == 10 || it == 13 || it == 9) it.toChar() else ' ' }
            .joinToString("")
            .replace(Regex(" {2,}"), " ")
            .trim()
    }

    private fun createSimpleDocx(content: String): ByteArray {
        val output = ByteArrayOutputStream()
        ZipOutputStream(output).use { zip ->
            zip.putTextEntry("[Content_Types].xml", contentTypesXml)
            zip.putTextEntry("_rels/.rels", relsXml)
            zip.putTextEntry("word/_rels/document.xml.rels", documentRelsXml)
            zip.putTextEntry("word/document.xml", documentXml(content))
        }
        return output.toByteArray()
    }

    private fun ZipOutputStream.putTextEntry(name: String, value: String) {
        putNextEntry(ZipEntry(name))
        write(value.toByteArray(Charsets.UTF_8))
        closeEntry()
    }

    private fun documentXml(content: String): String {
        val paragraphs = content
            .replace("\r\n", "\n")
            .split("\n")
            .joinToString("") { line ->
                "<w:p><w:r><w:t xml:space=\"preserve\">${line.escapeXml()}</w:t></w:r></w:p>"
            }
        return """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
              <w:body>$paragraphs<w:sectPr/></w:body>
            </w:document>
        """.trimIndent()
    }

    private fun String.extension(): String =
        substringAfterLast('.', missingDelimiterValue = "").lowercase()

    private fun String.escapeXml(): String =
        replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")

    private fun String.unescapeXml(): String =
        replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
            .replace("&amp;", "&")

    private val contentTypesXml = """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
          <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
          <Default Extension="xml" ContentType="application/xml"/>
          <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
        </Types>
    """.trimIndent()

    private val relsXml = """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
          <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
        </Relationships>
    """.trimIndent()

    private val documentRelsXml = """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"/>
    """.trimIndent()
}
