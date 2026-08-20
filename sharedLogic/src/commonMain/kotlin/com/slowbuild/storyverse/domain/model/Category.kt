package com.slowbuild.storyverse.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val id: String? = null,
    val name: String,
    val slug: String? = null
)
