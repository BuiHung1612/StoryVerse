package com.slowbuild.storyverse.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.slowbuild.storyverse.domain.model.Author
import com.slowbuild.storyverse.domain.model.Category
import com.slowbuild.storyverse.domain.model.Story
import com.slowbuild.storyverse.domain.model.StoryId
import com.slowbuild.storyverse.domain.model.StoryOrigin
import com.slowbuild.storyverse.domain.model.StoryStatus
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Entity(tableName = "stories")
data class StoryEntity(
    @PrimaryKey val storyId: String,
    val sourceId: String,
    val rawId: String,
    val title: String,
    val coverUrl: String?,
    val authorsJson: String,
    val categoriesJson: String,
    val status: String,
    val origin: String,
    val description: String?,
    val totalChapters: Int,
    val latestChapterTitle: String?,
    val updatedAt: Long?,
    val inLibrary: Boolean,
    val lastAccessedAt: Long
) {
    fun toDomain(): Story {
        val parsedAuthors = try {
            Json.decodeFromString<List<Author>>(authorsJson)
        } catch (_: Exception) {
            emptyList()
        }

        val parsedCategories = try {
            Json.decodeFromString<List<Category>>(categoriesJson)
        } catch (_: Exception) {
            emptyList()
        }

        return Story(
            id = StoryId.create(sourceId, rawId),
            title = title,
            coverUrl = coverUrl,
            authors = parsedAuthors,
            categories = parsedCategories,
            status = try { StoryStatus.valueOf(status) } catch (_: Exception) { StoryStatus.UNKNOWN },
            origin = try { StoryOrigin.valueOf(origin) } catch (_: Exception) { StoryOrigin.REMOTE },
            description = description,
            totalChapters = totalChapters,
            latestChapterTitle = latestChapterTitle,
            updatedAt = updatedAt,
            inLibrary = inLibrary
        )
    }

    companion object {
        fun fromDomain(story: Story, lastAccessedAt: Long = 0L): StoryEntity {
            return StoryEntity(
                storyId = story.id.value,
                sourceId = story.id.sourceId,
                rawId = story.id.rawId,
                title = story.title,
                coverUrl = story.coverUrl,
                authorsJson = Json.encodeToString(story.authors),
                categoriesJson = Json.encodeToString(story.categories),
                status = story.status.name,
                origin = story.origin.name,
                description = story.description,
                totalChapters = story.totalChapters,
                latestChapterTitle = story.latestChapterTitle,
                updatedAt = story.updatedAt,
                inLibrary = story.inLibrary,
                lastAccessedAt = lastAccessedAt
            )
        }
    }
}
