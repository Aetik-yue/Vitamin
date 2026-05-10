package com.example.markdownreader.presentation.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.markdownreader.core.common.AppStrings
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun ReaderScreen(
    uiState: ReaderUiState,
    strings: AppStrings,
    fontSize: Float,
    isDarkTheme: Boolean,
    onBack: () -> Unit,
    onReload: () -> Unit,
    onEdit: () -> Unit,
    onAbout: () -> Unit,
    onSaveScroll: (Int) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onNextMatch: (Int) -> Unit,
    onPreviousMatch: (Int) -> Unit,
    onToggleSearch: () -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scrollState = rememberScrollState()
    var matchCount by remember { mutableIntStateOf(0) }
    var scrollToY by remember { mutableIntStateOf(-1) }
    var showShareMenu by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.uri, uiState.restoredScrollOffset, uiState.markdown) {
        if (uiState.markdown.isNotBlank() && uiState.restoredScrollOffset > 0) {
            scrollState.scrollTo(uiState.restoredScrollOffset)
        }
    }

    LaunchedEffect(uiState.uri) {
        snapshotFlow { scrollState.value }
            .distinctUntilChanged()
            .debounce(500)
            .collect { onSaveScroll(it) }
    }

    LaunchedEffect(scrollToY) {
        if (scrollToY >= 0) {
            scrollState.animateScrollTo(scrollToY)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.title.ifBlank { strings.reader },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text(strings.back)
                    }
                },
                actions = {
                    if (!uiState.isSearching) {
                        TextButton(onClick = onToggleSearch) {
                            Text(strings.search)
                        }
                        TextButton(onClick = onReload) {
                            Text(strings.reload)
                        }
                        Box {
                            TextButton(onClick = { showMoreMenu = true }) {
                                Text("...")
                            }
                            DropdownMenu(
                                expanded = showMoreMenu,
                                onDismissRequest = { showMoreMenu = false },
                            ) {
                                DropdownMenuItem(
                                    text = { Text(strings.edit) },
                                    enabled = uiState.markdown.isNotBlank(),
                                    onClick = {
                                        showMoreMenu = false
                                        onEdit()
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text(strings.share) },
                                    onClick = {
                                        showMoreMenu = false
                                        showShareMenu = true
                                    },
                                )
                            }
                        }
                        Box {
                            DropdownMenu(
                                expanded = showShareMenu,
                                onDismissRequest = { showShareMenu = false },
                            ) {
                                DropdownMenuItem(
                                    text = { Text(strings.shareFile) },
                                    onClick = {
                                        showShareMenu = false
                                        val uri = uiState.uri?.let { android.net.Uri.parse(it) }
                                        if (uri != null) {
                                            val intent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/markdown"
                                                putExtra(Intent.EXTRA_STREAM, uri)
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            }
                                            context.startActivity(Intent.createChooser(intent, strings.shareFile))
                                        }
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text(strings.shareText) },
                                    onClick = {
                                        showShareMenu = false
                                        val intent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, uiState.markdown)
                                        }
                                        context.startActivity(Intent.createChooser(intent, strings.shareText))
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text(strings.shareRendered) },
                                    onClick = {
                                        showShareMenu = false
                                        val intent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, uiState.markdown)
                                            putExtra(Intent.EXTRA_SUBJECT, uiState.title)
                                        }
                                        context.startActivity(Intent.createChooser(intent, strings.shareRendered))
                                    },
                                )
                            }
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            if (!uiState.isSearching) {
                FloatingActionButton(
                    onClick = onAbout,
                    shape = CircleShape,
                ) {
                    Text(
                        text = "i",
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                AnimatedVisibility(
                    visible = uiState.isSearching,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(onClick = onToggleSearch) {
                            Text(strings.back)
                        }
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = onSearchQueryChange,
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            placeholder = { Text(strings.searchHint) },
                        )
                        if (matchCount > 0) {
                            Text(
                                text = "${uiState.currentMatchIndex.coerceAtLeast(0) + 1}/$matchCount",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(horizontal = 4.dp),
                            )
                        } else if (uiState.searchQuery.isNotBlank()) {
                            Text(
                                text = strings.noMatches,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(horizontal = 4.dp),
                            )
                        }
                        IconButton(onClick = { onPreviousMatch(matchCount) }) {
                            Text(
                                text = "▲",
                                fontSize = MaterialTheme.typography.bodySmall.fontSize,
                            )
                        }
                        IconButton(onClick = { onNextMatch(matchCount) }) {
                            Text(
                                text = "▼",
                                fontSize = MaterialTheme.typography.bodySmall.fontSize,
                            )
                        }
                    }
                }

                when {
                    uiState.isLoading -> Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }

                    uiState.errorMessage != null -> Text(
                        text = uiState.errorMessage,
                        modifier = Modifier.padding(24.dp),
                    )

                    uiState.markdown.isBlank() -> Text(
                        text = strings.markdownEmpty,
                        modifier = Modifier.padding(24.dp),
                    )

                    else -> MarkdownText(
                        markdown = uiState.markdown,
                        fontSize = fontSize,
                        isDarkTheme = isDarkTheme,
                        searchQuery = uiState.effectiveSearchQuery,
                        activeMatchIndex = uiState.currentMatchIndex,
                        onMatchCountChanged = { matchCount = it },
                        onActiveMatchYChanged = { y -> scrollToY = y },
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(20.dp),
                    )
                }
            }
        }
    }
}

