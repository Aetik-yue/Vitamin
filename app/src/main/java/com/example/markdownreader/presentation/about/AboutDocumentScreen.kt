package com.example.markdownreader.presentation.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.markdownreader.core.common.AppStrings
import com.example.markdownreader.presentation.reader.ReaderUiState
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutDocumentScreen(
    uiState: ReaderUiState,
    strings: AppStrings,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.fileInfo) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text(strings.back)
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            InfoRow(strings.fileName, uiState.displayName.ifBlank { uiState.title.ifBlank { strings.unknown } })
            InfoRow(strings.fileCategory, uiState.category.ifBlank { strings.unknown })
            InfoRow(strings.fileSize, uiState.size?.let { formatSize(it) } ?: strings.unknown)
            InfoRow(strings.lastOpened, uiState.lastOpenedAt?.let { formatTime(it) } ?: strings.unknown)
            InfoRow(strings.lastModified, uiState.lastModified?.let { formatTime(it) } ?: strings.unknown)
            InfoRow(strings.fileUri, uiState.uri ?: strings.unknown)
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

private fun formatTime(timeMillis: Long): String =
    DateFormat.getDateTimeInstance().format(Date(timeMillis))

private fun formatSize(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024) return String.format("%.1f KB", kb)
    return String.format("%.1f MB", kb / 1024.0)
}
