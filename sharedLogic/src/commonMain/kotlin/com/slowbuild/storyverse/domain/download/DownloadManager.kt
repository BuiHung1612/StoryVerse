package com.slowbuild.storyverse.domain.download

import com.slowbuild.storyverse.core.result.AppResult
import com.slowbuild.storyverse.domain.model.DownloadStatus
import com.slowbuild.storyverse.domain.model.Story
import com.slowbuild.storyverse.domain.model.StoryDownloadSummary
import com.slowbuild.storyverse.domain.model.StoryId
import kotlinx.coroutines.flow.Flow

data class DownloadProgress(
    val storyId: StoryId,
    val status: DownloadStatus,
    val progress: Float = 0f,
    val downloadedBytes: Long = 0,
    val totalBytes: Long = 0,
    val errorMessage: String? = null
)

interface DownloadManager {
    fun downloadStory(story: Story, downloadUrl: String): Flow<DownloadProgress>
    suspend fun startDownload(story: Story, downloadUrl: String, onProgress: (DownloadProgress) -> Unit): AppResult<Unit>
    fun observeDownloadProgress(storyId: StoryId): Flow<DownloadProgress?>
    fun observeAllDownloads(): Flow<List<StoryDownloadSummary>>
    fun observeStoryDownload(storyId: StoryId): Flow<StoryDownloadSummary?>
    suspend fun deleteDownload(storyId: StoryId): AppResult<Unit>
    suspend fun isStoryDownloaded(storyId: StoryId): Boolean
}
