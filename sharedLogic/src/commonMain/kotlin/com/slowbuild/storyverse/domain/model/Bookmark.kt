package com.slowbuild.storyverse.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Bookmark(
    val id: String,
    val storyId: StoryId,
    val chapterId: String,
    val chapterTitle: String,
    val paragraphIndex: Int = 0,
    val note: String? = null,
    val createdAt: Long
)
