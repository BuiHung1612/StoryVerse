package com.slowbuild.storyverse.data.local.room.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.slowbuild.storyverse.domain.model.Bookmark
import com.slowbuild.storyverse.domain.model.StoryId

@Entity(
    tableName = "bookmarks",
    indices = [
        Index(value = ["storyId"]),
        Index(value = ["storyId", "chapterId"])
    ]
)
data class BookmarkEntity(
    @PrimaryKey val id: String,
    val storyId: String,
    val chapterId: String,
    val chapterTitle: String,
    val paragraphIndex: Int,
    val note: String?,
    val createdAt: Long
) {
    fun toDomain(): Bookmark = Bookmark(
        id = id,
        storyId = StoryId.from(storyId),
        chapterId = chapterId,
        chapterTitle = chapterTitle,
        paragraphIndex = paragraphIndex,
        note = note,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(bookmark: Bookmark): BookmarkEntity = BookmarkEntity(
            id = bookmark.id,
            storyId = bookmark.storyId.value,
            chapterId = bookmark.chapterId,
            chapterTitle = bookmark.chapterTitle,
            paragraphIndex = bookmark.paragraphIndex,
            note = bookmark.note,
            createdAt = bookmark.createdAt
        )
    }
}
