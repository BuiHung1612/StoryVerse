package com.slowbuild.storyverse.domain.source

import kotlinx.serialization.Serializable

@Serializable
data class SourceCapabilities(
    val supportsSearch: Boolean = true,
    val supportsCategories: Boolean = true,
    val supportsRanking: Boolean = false,
    val supportsLatestUpdates: Boolean = true,
    val supportsPagination: Boolean = true,
    val supportsOfflineDownload: Boolean = true,
    val supportsCustomHeaders: Boolean = false
)
