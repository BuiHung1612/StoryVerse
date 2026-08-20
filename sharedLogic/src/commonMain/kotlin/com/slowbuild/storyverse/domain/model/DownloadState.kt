package com.slowbuild.storyverse.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class DownloadStatus {
    QUEUED,
    DOWNLOADING,
    COMPLETED,
    PAUSED,
    FAILED,
    CANCELLED
}

@Serializable
data class ChapterDownload(
    val storyId: StoryId,
    val chapterId: String,
    val chapterTitle: String,
    val chapterIndex: Int,
    val status: DownloadStatus,
    val progress: Float = 0f,
    val error: String? = null,
    val downloadedAt: Long? = null
)

@Serializable
data class StoryDownloadSummary(
    val storyId: StoryId,
    val totalChapters: Int,
    val downloadedChapters: Int,
    val status: DownloadStatus
) {
    val progress: Float
        get() = if (totalChapters > 0) downloadedChapters.toFloat() / totalChapters else 0f
}
