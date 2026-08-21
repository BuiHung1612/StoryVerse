package com.slowbuild.storyverse.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.slowbuild.storyverse.data.local.room.entity.HistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(history: HistoryEntity)

    @Query("SELECT * FROM reading_history ORDER BY lastReadAt DESC")
    fun observeHistory(): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM reading_history ORDER BY lastReadAt DESC LIMIT :limit")
    suspend fun getRecentHistory(limit: Int = 20): List<HistoryEntity>

    @Query("SELECT * FROM reading_history WHERE storyId = :storyId LIMIT 1")
    suspend fun getHistoryByStoryId(storyId: String): HistoryEntity?

    @Query("DELETE FROM reading_history WHERE storyId = :storyId")
    suspend fun deleteHistoryForStory(storyId: String)

    @Query("DELETE FROM reading_history")
    suspend fun clearAllHistory()
}
