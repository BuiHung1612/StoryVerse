package com.slowbuild.storyverse.data.network.dto

import com.slowbuild.storyverse.domain.model.Chapter
import com.slowbuild.storyverse.domain.model.ChapterContent
import com.slowbuild.storyverse.domain.model.StoryId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChapterDto(
    val id: String,
    val index: Int,
    val title: String,
    val url: String? = null,
    @SerialName("word_count") val wordCount: Int? = null,
    @SerialName("published_at") val publishedAt: Long? = null
)

@Serializable
data class ChapterContentDto(
    @SerialName("chapter_id") val chapterId: String,
    val title: String,
    val content: String,
    @SerialName("raw_html") val rawHtml: String? = null,
    val paragraphs: List<String> = emptyList(),
    @SerialName("word_count") val wordCount: Int = 0,
    @SerialName("source_info") val sourceInfo: String? = null
)

// Mappers to Domain
fun ChapterDto.toDomain(storyId: StoryId): Chapter = Chapter(
    id = id,
    storyId = storyId,
    index = index,
    title = title,
    url = url,
    wordCount = wordCount,
    publishedAt = publishedAt
)

fun ChapterContentDto.toDomain(storyId: StoryId): ChapterContent = ChapterContent(
    chapterId = chapterId,
    storyId = storyId,
    title = title,
    content = content,
    rawHtml = rawHtml,
    paragraphs = paragraphs.ifEmpty {
        content.split("\n\n").map { it.trim() }.filter { it.isNotEmpty() }
    },
    wordCount = if (wordCount > 0) wordCount else content.split("\\s+".toRegex()).size,
    sourceInfo = sourceInfo
)
