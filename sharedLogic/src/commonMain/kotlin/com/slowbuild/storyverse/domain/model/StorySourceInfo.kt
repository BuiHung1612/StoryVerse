package com.slowbuild.storyverse.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class StorySourceInfo(
    val id: String,
    val name: String,
    val baseUrl: String? = null,
    val isLocal: Boolean = false,
    val isAi: Boolean = false
)
