package com.slowbuild.storyverse.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.slowbuild.storyverse.domain.model.DownloadStatus
import com.slowbuild.storyverse.domain.model.StoryDownloadSummary
import com.slowbuild.storyverse.domain.model.StoryId

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val storyId: String,
    val status: String,
    val downloadedChapters: Int,
    val totalChapters: Int,
    val bytesDownloaded: Long,
    val totalBytes: Long,
    val errorMessage: String?,
    val updatedAt: Long
) {
    fun toDomain(): StoryDownloadSummary = StoryDownloadSummary(
        storyId = StoryId.from(storyId),
        status = try { DownloadStatus.valueOf(status) } catch (_: Exception) { DownloadStatus.QUEUED },
        downloadedChapters = downloadedChapters,
        totalChapters = totalChapters
    )

    companion object {
        fun fromDomain(summary: StoryDownloadSummary, updatedAt: Long = 0L): DownloadEntity = DownloadEntity(
            storyId = summary.storyId.value,
            status = summary.status.name,
            downloadedChapters = summary.downloadedChapters,
            totalChapters = summary.totalChapters,
            bytesDownloaded = 0L,
            totalBytes = 0L,
            errorMessage = null,
            updatedAt = updatedAt
        )
    }
}
