package com.slowbuild.storyverse.domain.source

import com.slowbuild.storyverse.domain.model.Story
import kotlinx.serialization.Serializable

@Serializable
enum class SectionType {
    FEATURED,
    POPULAR,
    LATEST,
    COMPLETED,
    RECOMMENDED,
    CUSTOM
}

@Serializable
data class StorySection(
    val id: String,
    val title: String,
    val type: SectionType = SectionType.CUSTOM,
    val stories: List<Story> = emptyList(),
    val hasMore: Boolean = false
)
