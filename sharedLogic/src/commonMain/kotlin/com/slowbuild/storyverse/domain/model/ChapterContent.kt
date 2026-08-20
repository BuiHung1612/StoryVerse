package com.slowbuild.storyverse.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ChapterContent(
    val chapterId: String,
    val storyId: StoryId,
    val title: String,
    val content: String,
    val rawHtml: String? = null,
    val paragraphs: List<String> = emptyList(),
    val wordCount: Int = 0,
    val sourceInfo: String? = null
)
