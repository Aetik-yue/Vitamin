package com.example.markdownreader

import android.app.Application
import androidx.room.Room
import com.example.markdownreader.core.common.Constants
import com.example.markdownreader.data.datasource.MarkdownFileDataSource
import com.example.markdownreader.data.datasource.SettingsDataSource
import com.example.markdownreader.data.db.AppDatabase
import com.example.markdownreader.data.db.AppDatabase.Companion.MIGRATION_1_2
import com.example.markdownreader.data.db.AppDatabase.Companion.MIGRATION_2_3
import com.example.markdownreader.data.db.AppDatabase.Companion.MIGRATION_3_4
import com.example.markdownreader.data.repository.MarkdownRepositoryImpl
import com.example.markdownreader.domain.repository.MarkdownRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MarkdownReaderApplication : Application() {
    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
    }
}

class AppContainer(
    application: Application,
) {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val database: AppDatabase = Room.databaseBuilder(
        application,
        AppDatabase::class.java,
        Constants.DATABASE_NAME,
    ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4).build()

    val settingsDataSource = SettingsDataSource(application)

    @Volatile
    private var rememberRecentFiles = runBlocking {
        settingsDataSource.settingsFlow.first().rememberRecentFiles
    }

    val markdownRepository: MarkdownRepository = MarkdownRepositoryImpl(
        fileDataSource = MarkdownFileDataSource(application.contentResolver),
        documentDao = database.documentDao(),
        progressDao = database.readingProgressDao(),
        rememberRecentFiles = { rememberRecentFiles },
    )

    init {
        appScope.launch {
            settingsDataSource.settingsFlow.collect {
                rememberRecentFiles = it.rememberRecentFiles
            }
        }
    }
}
