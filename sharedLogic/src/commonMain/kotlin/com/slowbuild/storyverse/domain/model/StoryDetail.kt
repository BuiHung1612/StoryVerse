package com.slowbuild.storyverse.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class StoryDetail(
    val story: Story,
    val sourceInfo: StorySourceInfo,
    val fullDescription: String? = null,
    val rating: Float? = null,
    val views: Long? = null,
    val isBookmarked: Boolean = false,
    val chapters: List<Chapter> = emptyList(),
    val extraMetadata: Map<String, String> = emptyMap()
)
