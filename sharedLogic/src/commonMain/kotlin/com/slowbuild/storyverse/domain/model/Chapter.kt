package com.slowbuild.storyverse.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Chapter(
    val id: String,
    val storyId: StoryId,
    val index: Int,
    val title: String,
    val url: String? = null,
    val isDownloaded: Boolean = false,
    val isRead: Boolean = false,
    val wordCount: Int? = null,
    val publishedAt: Long? = null
)
