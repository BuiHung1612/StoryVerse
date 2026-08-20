package com.slowbuild.storyverse.domain.source

import com.slowbuild.storyverse.domain.model.StoryOrigin
import com.slowbuild.storyverse.domain.model.StorySourceInfo
import kotlinx.serialization.Serializable

@Serializable
data class StorySourceMetadata(
    val id: String,
    val name: String,
    val displayName: String,
    val version: String = "1.0.0",
    val baseUrl: String? = null,
    val origin: StoryOrigin = StoryOrigin.REMOTE,
    val capabilities: SourceCapabilities = SourceCapabilities(),
    val iconUrl: String? = null,
    val isEnabled: Boolean = true
) {
    val isLocal: Boolean
        get() = origin == StoryOrigin.LOCAL_EPUB

    val isAi: Boolean
        get() = origin == StoryOrigin.AI_GENERATED

    fun toStorySourceInfo(): StorySourceInfo = StorySourceInfo(
        id = id,
        name = displayName,
        baseUrl = baseUrl,
        isLocal = isLocal,
        isAi = isAi
    )
}
