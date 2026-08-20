package com.slowbuild.storyverse.domain.source

import com.slowbuild.storyverse.domain.model.StoryStatus
import kotlinx.serialization.Serializable

@Serializable
enum class StorySort {
    POPULAR,
    LATEST,
    CHAPTERS,
    RATING,
    ALPHABETICAL
}

@Serializable
data class StoryFilter(
    val category: String? = null,
    val status: StoryStatus? = null,
    val sort: StorySort = StorySort.POPULAR,
    val author: String? = null
)
