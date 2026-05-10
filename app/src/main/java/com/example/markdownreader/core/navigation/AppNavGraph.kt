package com.example.markdownreader.core.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.markdownreader.core.common.stringsFor
import com.example.markdownreader.data.datasource.SettingsDataSource
import com.example.markdownreader.domain.repository.MarkdownRepository
import com.example.markdownreader.presentation.about.AboutDocumentScreen
import com.example.markdownreader.presentation.editor.EditorScreen
import com.example.markdownreader.presentation.editor.EditorViewModel
import com.example.markdownreader.presentation.editor.EditorViewModelFactory
import com.example.markdownreader.presentation.library.LibraryScreen
import com.example.markdownreader.presentation.library.LibraryViewModel
import com.example.markdownreader.presentation.library.LibraryViewModelFactory
import com.example.markdownreader.presentation.reader.ReaderScreen
import com.example.markdownreader.presentation.reader.ReaderViewModel
import com.example.markdownreader.presentation.reader.ReaderViewModelFactory
import com.example.markdownreader.presentation.settings.SettingsScreen
import com.example.markdownreader.presentation.settings.SettingsViewModel
import com.example.markdownreader.presentation.settings.SettingsViewModelFactory

object Routes {
    const val LIBRARY = "library"
    const val SETTINGS = "settings"
    const val READER = "reader?uri={uri}"
    const val EDITOR = "editor?uri={uri}"
    const val ABOUT = "about?uri={uri}"

    fun reader(uri: String): String = "reader?uri=${Uri.encode(uri)}"
    fun editor(uri: String): String = "editor?uri=${Uri.encode(uri)}"
    fun about(uri: String): String = "about?uri=${Uri.encode(uri)}"
}

@Composable
fun AppNavGraph(
    repository: MarkdownRepository,
    settingsDataSource: SettingsDataSource,
) {
    val navController = rememberNavController()
    val appSettingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(settingsDataSource)
    )
    val appSettingsUiState by appSettingsViewModel.uiState.collectAsState()
    val strings = stringsFor(appSettingsUiState.languageCode)

    NavHost(navController = navController, startDestination = Routes.LIBRARY) {
        composable(Routes.LIBRARY) {
            val libraryViewModel: LibraryViewModel = viewModel(
                factory = LibraryViewModelFactory(repository)
            )
            val uiState by libraryViewModel.uiState.collectAsState()
            val context = androidx.compose.ui.platform.LocalContext.current

            LibraryScreen(
                uiState = uiState,
                strings = strings,
                onOpenDocument = { uri -> navController.navigate(Routes.reader(uri.toString())) },
                onCreateDocument = { uri ->
                    libraryViewModel.createMarkdown(uri) {
                        navController.navigate(Routes.editor(uri.toString()))
                    }
                },
                onOpenRecent = { uri -> navController.navigate(Routes.reader(uri)) },
                onDeleteRecent = libraryViewModel::deleteRecent,
                onSelectCategory = libraryViewModel::selectCategory,
                onUpdateCategory = libraryViewModel::updateCategory,
                onRenameDocument = libraryViewModel::renameDocument,
                onSetFavorite = libraryViewModel::setFavorite,
                onSortModeChange = libraryViewModel::setSortMode,
                onImportFolder = { folderUri -> libraryViewModel.importFolder(context, folderUri) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
            )
        }

        composable(
            route = Routes.READER,
            arguments = listOf(navArgument("uri") { type = NavType.StringType }),
        ) { backStackEntry ->
            val uriString = backStackEntry.arguments?.getString("uri").orEmpty()
            val readerViewModel: ReaderViewModel = viewModel(
                factory = ReaderViewModelFactory(repository)
            )
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModelFactory(settingsDataSource)
            )
            val readerUiState by readerViewModel.uiState.collectAsState()
            val settingsUiState by settingsViewModel.uiState.collectAsState()

            LaunchedEffect(uriString) {
                if (uriString.isNotBlank()) {
                    readerViewModel.openDocument(Uri.parse(uriString))
                }
            }

            ReaderScreen(
                uiState = readerUiState,
                fontSize = settingsUiState.fontSize,
                isDarkTheme = settingsUiState.isDarkTheme,
                onBack = { navController.popBackStack() },
                onReload = readerViewModel::reload,
                onEdit = {
                    readerUiState.uri?.let { uri -> navController.navigate(Routes.editor(uri)) }
                },
                onAbout = {
                    readerUiState.uri?.let { uri -> navController.navigate(Routes.about(uri)) }
                },
                strings = strings,
                onSaveScroll = readerViewModel::saveScrollPosition,
                onSearchQueryChange = readerViewModel::setSearchQuery,
                onNextMatch = { readerViewModel.nextMatch(it) },
                onPreviousMatch = { readerViewModel.previousMatch(it) },
                onToggleSearch = readerViewModel::toggleSearch,
            )
        }

        composable(
            route = Routes.ABOUT,
            arguments = listOf(navArgument("uri") { type = NavType.StringType }),
        ) { backStackEntry ->
            val uriString = backStackEntry.arguments?.getString("uri").orEmpty()
            val readerViewModel: ReaderViewModel = viewModel(
                factory = ReaderViewModelFactory(repository)
            )
            val readerUiState by readerViewModel.uiState.collectAsState()

            LaunchedEffect(uriString) {
                if (uriString.isNotBlank()) {
                    readerViewModel.openDocument(Uri.parse(uriString))
                }
            }

            AboutDocumentScreen(
                uiState = readerUiState,
                strings = strings,
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Routes.EDITOR,
            arguments = listOf(navArgument("uri") { type = NavType.StringType }),
        ) { backStackEntry ->
            val uriString = backStackEntry.arguments?.getString("uri").orEmpty()
            val editorViewModel: EditorViewModel = viewModel(
                factory = EditorViewModelFactory(repository)
            )
            val editorUiState by editorViewModel.uiState.collectAsState()

            LaunchedEffect(uriString) {
                if (uriString.isNotBlank()) {
                    editorViewModel.load(Uri.parse(uriString))
                }
            }

            EditorScreen(
                uiState = editorUiState,
                strings = strings,
                onBack = { navController.popBackStack() },
                onContentChange = editorViewModel::updateContent,
                onSave = editorViewModel::save,
            )
        }

        composable(Routes.SETTINGS) {
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModelFactory(settingsDataSource)
            )
            val libraryViewModel: LibraryViewModel = viewModel(
                factory = LibraryViewModelFactory(repository)
            )
            val uiState by settingsViewModel.uiState.collectAsState()

            SettingsScreen(
                uiState = uiState,
                strings = strings,
                onFontSizeChange = settingsViewModel::setFontSize,
                onDarkThemeChange = settingsViewModel::setDarkTheme,
                onRememberRecentChange = settingsViewModel::setRememberRecentFiles,
                onLanguageChange = settingsViewModel::setLanguageCode,
                onClearRecent = libraryViewModel::clearRecent,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
