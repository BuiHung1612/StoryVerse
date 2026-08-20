package com.slowbuild.storyverse.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class HistoryEntry(
    val story: Story,
    val lastReadChapterId: String,
    val lastReadChapterTitle: String,
    val lastReadChapterIndex: Int,
    val lastReadAt: Long
)
