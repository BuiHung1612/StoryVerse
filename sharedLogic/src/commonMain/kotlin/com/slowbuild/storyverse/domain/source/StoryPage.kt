package com.slowbuild.storyverse.domain.source

import com.slowbuild.storyverse.domain.model.Story
import kotlinx.serialization.Serializable

@Serializable
data class StoryPage(
    val stories: List<Story> = emptyList(),
    val page: Int = 1,
    val hasNextPage: Boolean = false,
    val totalPages: Int? = null,
    val totalResults: Int? = null
)
