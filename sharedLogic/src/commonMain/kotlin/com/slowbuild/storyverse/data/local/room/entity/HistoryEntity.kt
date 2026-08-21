package com.slowbuild.storyverse.data.local.room.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.slowbuild.storyverse.domain.model.HistoryEntry
import com.slowbuild.storyverse.domain.model.Story
import com.slowbuild.storyverse.domain.model.StoryId
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Entity(
    tableName = "reading_history",
    indices = [Index(value = ["lastReadAt"])]
)
data class HistoryEntity(
    @PrimaryKey val storyId: String,
    val storyJson: String,
    val lastReadChapterId: String,
    val lastReadChapterTitle: String,
    val lastReadChapterIndex: Int,
    val lastReadAt: Long
) {
    fun toDomain(): HistoryEntry {
        val parsedStory = try {
            Json.decodeFromString<Story>(storyJson)
        } catch (_: Exception) {
            val sId = StoryId.from(storyId)
            Story(id = sId, title = "Unknown")
        }

        return HistoryEntry(
            story = parsedStory,
            lastReadChapterId = lastReadChapterId,
            lastReadChapterTitle = lastReadChapterTitle,
            lastReadChapterIndex = lastReadChapterIndex,
            lastReadAt = lastReadAt
        )
    }

    companion object {
        fun fromDomain(history: HistoryEntry): HistoryEntity = HistoryEntity(
            storyId = history.story.id.value,
            storyJson = Json.encodeToString(history.story),
            lastReadChapterId = history.lastReadChapterId,
            lastReadChapterTitle = history.lastReadChapterTitle,
            lastReadChapterIndex = history.lastReadChapterIndex,
            lastReadAt = history.lastReadAt
        )
    }
}
