package com.slowbuild.storyverse.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Story(
    val id: StoryId,
    val title: String,
    val coverUrl: String? = null,
    val authors: List<Author> = emptyList(),
    val categories: List<Category> = emptyList(),
    val status: StoryStatus = StoryStatus.UNKNOWN,
    val origin: StoryOrigin = StoryOrigin.REMOTE,
    val description: String? = null,
    val totalChapters: Int = 0,
    val latestChapterTitle: String? = null,
    val updatedAt: Long? = null,
    val inLibrary: Boolean = false
) {
    val authorNames: String
        get() = if (authors.isEmpty()) "Unknown" else authors.joinToString(", ") { it.name }

    val categoryNames: String
        get() = categories.joinToString(", ") { it.name }
}
