package com.slowbuild.storyverse.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.slowbuild.storyverse.data.local.room.entity.StoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(story: StoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAll(stories: List<StoryEntity>)

    @Query("SELECT * FROM stories WHERE storyId = :storyId LIMIT 1")
    suspend fun getStoryById(storyId: String): StoryEntity?

    @Query("SELECT * FROM stories WHERE inLibrary = 1 ORDER BY lastAccessedAt DESC")
    fun observeLibraryStories(): Flow<List<StoryEntity>>

    @Query("SELECT * FROM stories ORDER BY lastAccessedAt DESC")
    fun observeAllStories(): Flow<List<StoryEntity>>

    @Query("DELETE FROM stories WHERE storyId = :storyId")
    suspend fun deleteStory(storyId: String)
}
