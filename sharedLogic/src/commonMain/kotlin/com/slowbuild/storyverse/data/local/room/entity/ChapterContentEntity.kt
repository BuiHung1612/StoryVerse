package com.slowbuild.storyverse.data.local.room.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.slowbuild.storyverse.domain.model.ChapterContent
import com.slowbuild.storyverse.domain.model.StoryId
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Entity(
    tableName = "chapter_contents",
    indices = [Index(value = ["storyId"])]
)
data class ChapterContentEntity(
    @PrimaryKey val chapterId: String,
    val storyId: String,
    val title: String,
    val content: String,
    val paragraphsJson: String,
    val wordCount: Int,
    val sourceInfo: String?,
    val cachedAt: Long
) {
    fun toDomain(): ChapterContent {
        val parsedParagraphs = try {
            Json.decodeFromString<List<String>>(paragraphsJson)
        } catch (_: Exception) {
            content.split("\n\n").filter { it.isNotBlank() }
        }

        return ChapterContent(
            chapterId = chapterId,
            storyId = StoryId.from(storyId),
            title = title,
            content = content,
            paragraphs = parsedParagraphs,
            wordCount = wordCount,
            sourceInfo = sourceInfo
        )
    }

    companion object {
        fun fromDomain(chapterContent: ChapterContent, cachedAt: Long = 0L): ChapterContentEntity {
            return ChapterContentEntity(
                chapterId = chapterContent.chapterId,
                storyId = chapterContent.storyId.value,
                title = chapterContent.title,
                content = chapterContent.content,
                paragraphsJson = Json.encodeToString(chapterContent.paragraphs),
                wordCount = chapterContent.wordCount,
                sourceInfo = chapterContent.sourceInfo,
                cachedAt = cachedAt
            )
        }
    }
}
