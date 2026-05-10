package com.example.markdownreader.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents ORDER BY lastOpenedAt DESC")
    fun observeRecentDocuments(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE uri = :uri LIMIT 1")
    suspend fun getDocument(uri: String): DocumentEntity?

    @Query("SELECT DISTINCT category FROM documents ORDER BY category ASC")
    fun observeCategories(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDocument(entity: DocumentEntity)

    @Query("SELECT category FROM documents WHERE uri = :uri LIMIT 1")
    suspend fun getCategory(uri: String): String?

    @Query("UPDATE documents SET category = :category WHERE uri = :uri")
    suspend fun updateCategory(uri: String, category: String)

    @Query("UPDATE documents SET displayName = :displayName, title = :title WHERE uri = :uri")
    suspend fun renameDocument(uri: String, displayName: String, title: String)

    @Query("UPDATE documents SET isFavorite = :isFavorite WHERE uri = :uri")
    suspend fun setFavorite(uri: String, isFavorite: Boolean)

    @Query("DELETE FROM documents WHERE uri = :uri")
    suspend fun deleteDocument(uri: String)

    @Query("DELETE FROM documents")
    suspend fun clearDocuments()
}
