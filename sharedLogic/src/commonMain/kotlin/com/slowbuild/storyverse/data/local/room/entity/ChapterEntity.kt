package com.slowbuild.storyverse.data.local.room.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.slowbuild.storyverse.domain.model.Chapter
import com.slowbuild.storyverse.domain.model.StoryId

@Entity(
    tableName = "chapters",
    indices = [
        Index(value = ["storyId"]),
        Index(value = ["storyId", "index"])
    ]
)
data class ChapterEntity(
    @PrimaryKey val id: String,
    val storyId: String,
    val index: Int,
    val title: String,
    val url: String?,
    val isDownloaded: Boolean,
    val isRead: Boolean,
    val wordCount: Int?,
    val publishedAt: Long?
) {
    fun toDomain(): Chapter = Chapter(
        id = id,
        storyId = StoryId.from(storyId),
        index = index,
        title = title,
        url = url,
        isDownloaded = isDownloaded,
        isRead = isRead,
        wordCount = wordCount,
        publishedAt = publishedAt
    )

    companion object {
        fun fromDomain(chapter: Chapter): ChapterEntity = ChapterEntity(
            id = chapter.id,
            storyId = chapter.storyId.value,
            index = chapter.index,
            title = chapter.title,
            url = chapter.url,
            isDownloaded = chapter.isDownloaded,
            isRead = chapter.isRead,
            wordCount = chapter.wordCount,
            publishedAt = chapter.publishedAt
        )
    }
}
