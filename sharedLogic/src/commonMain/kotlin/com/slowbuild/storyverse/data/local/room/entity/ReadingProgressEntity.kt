package com.slowbuild.storyverse.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.slowbuild.storyverse.domain.model.ReadingProgress
import com.slowbuild.storyverse.domain.model.StoryId

@Entity(tableName = "reading_progress")
data class ReadingProgressEntity(
    @PrimaryKey val storyId: String,
    val lastReadChapterId: String,
    val lastReadChapterIndex: Int,
    val scrollOffset: Int,
    val progressPercentage: Float,
    val lastReadAt: Long
) {
    fun toDomain(): ReadingProgress = ReadingProgress(
        storyId = StoryId.from(storyId),
        lastReadChapterId = lastReadChapterId,
        lastReadChapterIndex = lastReadChapterIndex,
        scrollOffset = scrollOffset,
        progressPercentage = progressPercentage,
        lastReadAt = lastReadAt
    )

    companion object {
        fun fromDomain(progress: ReadingProgress): ReadingProgressEntity = ReadingProgressEntity(
            storyId = progress.storyId.value,
            lastReadChapterId = progress.lastReadChapterId,
            lastReadChapterIndex = progress.lastReadChapterIndex,
            scrollOffset = progress.scrollOffset,
            progressPercentage = progress.progressPercentage,
            lastReadAt = progress.lastReadAt
        )
    }
}
