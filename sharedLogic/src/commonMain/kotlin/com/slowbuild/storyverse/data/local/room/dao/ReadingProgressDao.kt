package com.slowbuild.storyverse.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.slowbuild.storyverse.data.local.room.entity.ReadingProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProgress(progress: ReadingProgressEntity)

    @Query("SELECT * FROM reading_progress WHERE storyId = :storyId LIMIT 1")
    suspend fun getProgressByStoryId(storyId: String): ReadingProgressEntity?

    @Query("SELECT * FROM reading_progress WHERE storyId = :storyId LIMIT 1")
    fun observeProgressByStoryId(storyId: String): Flow<ReadingProgressEntity?>

    @Query("DELETE FROM reading_progress WHERE storyId = :storyId")
    suspend fun deleteProgress(storyId: String)
}
