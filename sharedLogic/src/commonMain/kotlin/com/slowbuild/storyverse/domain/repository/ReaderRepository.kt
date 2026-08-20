package com.slowbuild.storyverse.domain.repository

import com.slowbuild.storyverse.core.result.AppResult
import com.slowbuild.storyverse.domain.model.Bookmark
import com.slowbuild.storyverse.domain.model.HistoryEntry
import com.slowbuild.storyverse.domain.model.ReadingProgress
import com.slowbuild.storyverse.domain.model.StoryId
import kotlinx.coroutines.flow.Flow

interface ReaderRepository {
    fun observeReadingProgress(storyId: StoryId): Flow<ReadingProgress?>
    
    suspend fun saveReadingProgress(progress: ReadingProgress): AppResult<Unit>
    
    fun observeBookmarks(storyId: StoryId): Flow<List<Bookmark>>
    
    suspend fun addBookmark(bookmark: Bookmark): AppResult<Unit>
    
    suspend fun removeBookmark(bookmarkId: String): AppResult<Unit>
    
    fun observeHistory(limit: Int = 50): Flow<List<HistoryEntry>>
    
    suspend fun recordHistory(entry: HistoryEntry): AppResult<Unit>
    
    suspend fun clearHistory(): AppResult<Unit>
}
