package com.slowbuild.storyverse.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Author(
    val id: String? = null,
    val name: String
)
