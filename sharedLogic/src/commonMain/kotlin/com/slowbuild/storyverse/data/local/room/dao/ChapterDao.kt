package com.slowbuild.storyverse.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.slowbuild.storyverse.data.local.room.entity.ChapterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChapterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAll(chapters: List<ChapterEntity>)

    @Query("SELECT * FROM chapters WHERE storyId = :storyId ORDER BY `index` ASC")
    suspend fun getChaptersByStoryId(storyId: String): List<ChapterEntity>

    @Query("SELECT * FROM chapters WHERE storyId = :storyId ORDER BY `index` ASC")
    fun observeChaptersByStoryId(storyId: String): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE id = :chapterId LIMIT 1")
    suspend fun getChapterById(chapterId: String): ChapterEntity?

    @Query("UPDATE chapters SET isRead = :isRead WHERE id = :chapterId")
    suspend fun markChapterRead(chapterId: String, isRead: Boolean)

    @Query("UPDATE chapters SET isDownloaded = :isDownloaded WHERE id = :chapterId")
    suspend fun updateDownloadStatus(chapterId: String, isDownloaded: Boolean)

    @Query("DELETE FROM chapters WHERE storyId = :storyId")
    suspend fun deleteChaptersByStory(storyId: String)
}
