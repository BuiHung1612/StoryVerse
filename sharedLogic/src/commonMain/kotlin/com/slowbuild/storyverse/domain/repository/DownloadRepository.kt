package com.slowbuild.storyverse.domain.repository

import com.slowbuild.storyverse.core.result.AppResult
import com.slowbuild.storyverse.domain.model.ChapterDownload
import com.slowbuild.storyverse.domain.model.StoryDownloadSummary
import com.slowbuild.storyverse.domain.model.StoryId
import kotlinx.coroutines.flow.Flow

interface DownloadRepository {
    fun observeDownloads(storyId: StoryId): Flow<List<ChapterDownload>>
    
    fun observeStoryDownloadSummaries(): Flow<List<StoryDownloadSummary>>
    
    suspend fun queueChapterDownloads(storyId: StoryId, chapterIds: List<String>): AppResult<Unit>
    
    suspend fun pauseDownload(storyId: StoryId, chapterId: String? = null): AppResult<Unit>
    
    suspend fun resumeDownload(storyId: StoryId, chapterId: String? = null): AppResult<Unit>
    
    suspend fun cancelDownload(storyId: StoryId, chapterId: String? = null): AppResult<Unit>
    
    suspend fun deleteDownloadedChapters(storyId: StoryId, chapterIds: List<String>): AppResult<Unit>
}
