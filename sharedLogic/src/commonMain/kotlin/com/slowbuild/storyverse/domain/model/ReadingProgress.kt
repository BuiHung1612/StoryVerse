package com.slowbuild.storyverse.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ReadingProgress(
    val storyId: StoryId,
    val lastReadChapterId: String,
    val lastReadChapterIndex: Int,
    val scrollOffset: Int = 0,
    val progressPercentage: Float = 0f,
    val lastReadAt: Long
)
