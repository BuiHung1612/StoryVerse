package com.slowbuild.storyverse.data.network.dto

import com.slowbuild.storyverse.domain.model.Author
import com.slowbuild.storyverse.domain.model.Category
import com.slowbuild.storyverse.domain.model.Story
import com.slowbuild.storyverse.domain.model.StoryDetail
import com.slowbuild.storyverse.domain.model.StoryId
import com.slowbuild.storyverse.domain.model.StoryOrigin
import com.slowbuild.storyverse.domain.model.StorySourceInfo
import com.slowbuild.storyverse.domain.model.StoryStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthorDto(
    val id: String? = null,
    val name: String
)

@Serializable
data class CategoryDto(
    val id: String? = null,
    val name: String,
    val slug: String? = null
)

@Serializable
data class StoryDto(
    val id: String,
    val title: String,
    @SerialName("cover_url") val coverUrl: String? = null,
    val authors: List<AuthorDto> = emptyList(),
    val categories: List<CategoryDto> = emptyList(),
    val status: String? = null,
    val description: String? = null,
    @SerialName("total_chapters") val totalChapters: Int = 0,
    @SerialName("latest_chapter_title") val latestChapterTitle: String? = null,
    @SerialName("updated_at") val updatedAt: Long? = null
)

@Serializable
data class StoryDetailDto(
    val story: StoryDto,
    @SerialName("full_description") val fullDescription: String? = null,
    val rating: Float? = null,
    val views: Long? = null,
    val chapters: List<ChapterDto> = emptyList(),
    @SerialName("extra_metadata") val extraMetadata: Map<String, String> = emptyMap()
)

// Mappers to Domain
fun AuthorDto.toDomain(): Author = Author(
    id = id,
    name = name
)

fun CategoryDto.toDomain(): Category = Category(
    id = id,
    name = name,
    slug = slug
)

fun StoryDto.toDomain(sourceId: String, origin: StoryOrigin = StoryOrigin.REMOTE): Story = Story(
    id = StoryId.create(sourceId = sourceId, rawId = id),
    title = title,
    coverUrl = coverUrl,
    authors = authors.map { it.toDomain() },
    categories = categories.map { it.toDomain() },
    status = when (status?.lowercase()) {
        "ongoing", "dang_ra", "đang ra" -> StoryStatus.ONGOING
        "completed", "hoan_thanh", "hoàn thành", "full" -> StoryStatus.COMPLETED
        "hiatus", "tam_dung", "tạm dừng" -> StoryStatus.HIATUS
        else -> StoryStatus.UNKNOWN
    },
    origin = origin,
    description = description,
    totalChapters = totalChapters,
    latestChapterTitle = latestChapterTitle,
    updatedAt = updatedAt
)

fun StoryDetailDto.toDomain(sourceInfo: StorySourceInfo, origin: StoryOrigin = StoryOrigin.REMOTE): StoryDetail {
    val domainStory = story.toDomain(sourceId = sourceInfo.id, origin = origin)
    return StoryDetail(
        story = domainStory,
        sourceInfo = sourceInfo,
        fullDescription = fullDescription ?: story.description,
        rating = rating,
        views = views,
        chapters = chapters.map { it.toDomain(domainStory.id) },
        extraMetadata = extraMetadata
    )
}
