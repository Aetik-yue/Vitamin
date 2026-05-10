package com.example.markdownreader.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ReadingProgressDao {
    @Query("SELECT * FROM reading_progress WHERE documentUri = :documentUri LIMIT 1")
    suspend fun getProgress(documentUri: String): ReadingProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(entity: ReadingProgressEntity)

    @Query("DELETE FROM reading_progress WHERE documentUri = :documentUri")
    suspend fun deleteProgress(documentUri: String)
}
