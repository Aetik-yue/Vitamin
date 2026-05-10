package com.example.markdownreader.presentation.library

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.markdownreader.core.common.AppStrings
import com.example.markdownreader.domain.model.MarkdownDocument
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    uiState: LibraryUiState,
    strings: AppStrings,
    onOpenDocument: (Uri) -> Unit,
    onCreateDocument: (Uri) -> Unit,
    onOpenRecent: (String) -> Unit,
    onDeleteRecent: (String) -> Unit,
    onSelectCategory: (String) -> Unit,
    onUpdateCategory: (String, String) -> Unit,
    onRenameDocument: (String, String) -> Unit,
    onSetFavorite: (String, Boolean) -> Unit,
    onSortModeChange: (LibrarySortMode) -> Unit,
    onImportFolder: (Uri) -> Unit,
    onOpenSettings: () -> Unit,
) {
    val context = LocalContext.current
    var selectedPage by remember { mutableIntStateOf(0) }
    var editingCategoryFor by remember { mutableStateOf<MarkdownDocument?>(null) }
    var renamingDocument by remember { mutableStateOf<MarkdownDocument?>(null) }
    var showCreateMenu by remember { mutableStateOf(false) }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                runCatching { context.contentResolver.takePersistableUriPermission(uri, flags) }
                    .recoverCatching {
                        context.contentResolver.takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION,
                        )
                    }
                onOpenDocument(uri)
            }
        },
    )

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/markdown"),
        onResult = { uri ->
            if (uri != null) {
                runCatching {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
                    )
                }
                onCreateDocument(uri)
            }
        },
    )

    val openFolderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
        onResult = { uri ->
            if (uri != null) {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
                onImportFolder(uri)
            }
        },
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.appName) },
                actions = {
                    TextButton(onClick = onOpenSettings) {
                        Text(strings.settings)
                    }
                },
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                NavigationBar(
                    modifier = Modifier.widthIn(max = 320.dp),
                    containerColor = MaterialTheme.colorScheme.surface,
                ) {
                    NavigationBarItem(
                        selected = selectedPage == 0,
                        onClick = { selectedPage = 0 },
                        icon = { Text("R") },
                        label = { Text(strings.recentFiles) },
                    )
                    NavigationBarItem(
                        selected = selectedPage == 1,
                        onClick = { selectedPage = 1 },
                        icon = { Text("C") },
                        label = { Text(strings.categoriesPage) },
                    )
                }
            }
        },
        floatingActionButton = {
            Box {
                FloatingActionButton(onClick = { showCreateMenu = true }) {
                    Text("+")
                }
                DropdownMenu(
                    expanded = showCreateMenu,
                    onDismissRequest = { showCreateMenu = false },
                ) {
                    DropdownMenuItem(
                        text = { Text(strings.createMarkdown) },
                        onClick = {
                            showCreateMenu = false
                            createDocumentLauncher.launch(strings.newMarkdownTitle)
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(strings.importFile) },
                        onClick = {
                            showCreateMenu = false
                            openDocumentLauncher.launch(supportedMimeTypes)
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(strings.importFolder) },
                        onClick = {
                            showCreateMenu = false
                            openFolderLauncher.launch(null)
                        },
                    )
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            VitaminHeader(strings)
            SortBar(
                sortMode = uiState.sortMode,
                strings = strings,
                onSortModeChange = onSortModeChange,
            )

            when (selectedPage) {
                0 -> DocumentList(
                    documents = uiState.documents,
                    strings = strings,
                    onOpen = onOpenRecent,
                    onDelete = onDeleteRecent,
                    onEditCategory = { editingCategoryFor = it },
                    onRename = { renamingDocument = it },
                    onSetFavorite = onSetFavorite,
                )
                else -> CategoryPage(
                    uiState = uiState,
                    strings = strings,
                    onSelectCategory = onSelectCategory,
                    onOpen = onOpenRecent,
                    onDelete = onDeleteRecent,
                    onEditCategory = { editingCategoryFor = it },
                    onRename = { renamingDocument = it },
                    onSetFavorite = onSetFavorite,
                )
            }
        }
    }

    editingCategoryFor?.let { document ->
        TextInputDialog(
            title = strings.classifyDocument,
            label = strings.category,
            initialValue = document.category,
            strings = strings,
            onDismiss = { editingCategoryFor = null },
            onSave = { category ->
                onUpdateCategory(document.uri, category)
                editingCategoryFor = null
            },
        )
    }

    renamingDocument?.let { document ->
        TextInputDialog(
            title = strings.rename,
            label = strings.displayName,
            initialValue = document.displayName,
            strings = strings,
            onDismiss = { renamingDocument = null },
            onSave = { displayName ->
                onRenameDocument(document.uri, displayName)
                renamingDocument = null
            },
        )
    }
}

@Composable
private fun VitaminHeader(strings: AppStrings) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = strings.libraryTagline,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = strings.librarySubtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SortBar(
    sortMode: LibrarySortMode,
    strings: AppStrings,
    onSortModeChange: (LibrarySortMode) -> Unit,
) {
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = sortMode == LibrarySortMode.TIME,
            onClick = { onSortModeChange(LibrarySortMode.TIME) },
            label = { Text(strings.sortByTime) },
        )
        FilterChip(
            selected = sortMode == LibrarySortMode.NAME,
            onClick = { onSortModeChange(LibrarySortMode.NAME) },
            label = { Text(strings.sortByName) },
        )
        FilterChip(
            selected = sortMode == LibrarySortMode.SIZE,
            onClick = { onSortModeChange(LibrarySortMode.SIZE) },
            label = { Text(strings.sortBySize) },
        )
    }
}

@Composable
private fun CategoryPage(
    uiState: LibraryUiState,
    strings: AppStrings,
    onSelectCategory: (String) -> Unit,
    onOpen: (String) -> Unit,
    onDelete: (String) -> Unit,
    onEditCategory: (MarkdownDocument) -> Unit,
    onRename: (MarkdownDocument) -> Unit,
    onSetFavorite: (String, Boolean) -> Unit,
) {
    Column {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            uiState.categories.forEach { category ->
                FilterChip(
                    selected = category == uiState.selectedCategory,
                    onClick = { onSelectCategory(category) },
                    label = { Text(if (category == "All") strings.all else category) },
                )
            }
        }
        DocumentList(
            documents = uiState.documents,
            strings = strings,
            onOpen = onOpen,
            onDelete = onDelete,
            onEditCategory = onEditCategory,
            onRename = onRename,
            onSetFavorite = onSetFavorite,
        )
    }
}

@Composable
private fun DocumentList(
    documents: List<MarkdownDocument>,
    strings: AppStrings,
    onOpen: (String) -> Unit,
    onDelete: (String) -> Unit,
    onEditCategory: (MarkdownDocument) -> Unit,
    onRename: (MarkdownDocument) -> Unit,
    onSetFavorite: (String, Boolean) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (documents.isEmpty()) {
            Text(
                text = strings.emptyLibrary,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp),
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(documents, key = { it.uri }) { document ->
                    DocumentCard(
                        document = document,
                        onOpen = { onOpen(document.uri) },
                        onEditCategory = { onEditCategory(document) },
                        onRename = { onRename(document) },
                        onDelete = { onDelete(document.uri) },
                        onSetFavorite = { onSetFavorite(document.uri, !document.isFavorite) },
                        strings = strings,
                    )
                }
            }
        }
    }
}

@Composable
private fun DocumentCard(
    document: MarkdownDocument,
    onOpen: () -> Unit,
    onEditCategory: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onSetFavorite: () -> Unit,
    strings: AppStrings,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = document.displayName,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            supportingContent = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip(onClick = onEditCategory, label = { Text(document.category) })
                        Text("${strings.opened}: ${formatTime(document.lastOpenedAt)}")
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = onSetFavorite) {
                            Text(if (document.isFavorite) "★ ${strings.favorite}" else "☆ ${strings.favorite}")
                        }
                        TextButton(onClick = onRename) {
                            Text(strings.rename)
                        }
                        TextButton(onClick = onDelete) {
                            Text(strings.delete)
                        }
                    }
                }
            },
            modifier = Modifier.clickable(onClick = onOpen),
        )
    }
}

@Composable
private fun TextInputDialog(
    title: String,
    label: String,
    initialValue: String,
    strings: AppStrings,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var value by remember(initialValue) { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                label = { Text(label) },
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(value) }) {
                Text(strings.save)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel)
            }
        },
    )
}

private fun formatTime(timeMillis: Long): String =
    DateFormat.getDateTimeInstance().format(Date(timeMillis))

private val supportedMimeTypes = arrayOf(
    "text/markdown",
    "text/plain",
    "application/msword",
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    "*/*",
)
