package com.slowbuild.storyverse.data.source.catalog

import kotlinx.serialization.Serializable

@Serializable
data class DriveCatalogItemDto(
    val index: Int = 0,
    val id: String,
    val name: String,
    val title: String? = null,
    val sizeBytes: Long = 0,
    val updatedAt: String? = null,
    val downloadUrl: String? = null,
    val subjects: List<String> = emptyList()
)

@Serializable
data class DriveCatalogResponseDto(
    val generatedAt: String? = null,
    val rootFolderId: String? = null,
    val sourceMergedFolderName: String? = null,
    val totalItemsRead: Int = 0,
    val uniqueItemCount: Int = 0,
    val items: List<DriveCatalogItemDto> = emptyList()
)
