package com.slowbuild.storyverse.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class StoryStatus {
    ONGOING,
    COMPLETED,
    HIATUS,
    UNKNOWN
}
