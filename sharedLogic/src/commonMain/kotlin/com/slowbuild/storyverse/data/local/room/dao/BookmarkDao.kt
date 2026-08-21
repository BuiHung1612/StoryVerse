package com.slowbuild.storyverse.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.slowbuild.storyverse.data.local.room.entity.BookmarkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("SELECT * FROM bookmarks WHERE storyId = :storyId ORDER BY createdAt DESC")
    fun observeBookmarksByStory(storyId: String): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks WHERE storyId = :storyId ORDER BY createdAt DESC")
    suspend fun getBookmarksByStory(storyId: String): List<BookmarkEntity>

    @Query("DELETE FROM bookmarks WHERE id = :bookmarkId")
    suspend fun deleteBookmark(bookmarkId: String)

    @Query("DELETE FROM bookmarks WHERE storyId = :storyId")
    suspend fun deleteAllBookmarksForStory(storyId: String)
}
