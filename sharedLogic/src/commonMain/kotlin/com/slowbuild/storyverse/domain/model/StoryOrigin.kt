package com.slowbuild.storyverse.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class StoryOrigin {
    REMOTE,
    LOCAL_EPUB,
    AI_GENERATED
}
