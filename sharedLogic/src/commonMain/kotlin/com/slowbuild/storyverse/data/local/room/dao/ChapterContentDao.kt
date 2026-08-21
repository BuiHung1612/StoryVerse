package com.slowbuild.storyverse.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.slowbuild.storyverse.data.local.room.entity.ChapterContentEntity

@Dao
interface ChapterContentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(content: ChapterContentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAll(contents: List<ChapterContentEntity>)

    @Query("SELECT * FROM chapter_contents WHERE chapterId = :chapterId LIMIT 1")
    suspend fun getContentByChapterId(chapterId: String): ChapterContentEntity?

    @Query("DELETE FROM chapter_contents WHERE chapterId = :chapterId")
    suspend fun deleteContent(chapterId: String)

    @Query("DELETE FROM chapter_contents WHERE storyId = :storyId")
    suspend fun deleteContentByStory(storyId: String)
}
