package com.slowbuild.storyverse.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class StoryId(
    val sourceId: String,
    val rawId: String
) {
    val value: String = "$sourceId$DELIMITER$rawId"

    override fun toString(): String = value

    companion object {
        private const val DELIMITER = "::"

        fun create(sourceId: String, rawId: String): StoryId {
            require(sourceId.isNotBlank()) { "sourceId cannot be blank" }
            require(rawId.isNotBlank()) { "rawId cannot be blank" }
            return StoryId(sourceId = sourceId.trim(), rawId = rawId.trim())
        }

        fun from(compositeValue: String): StoryId {
            require(compositeValue.isNotBlank()) { "compositeValue cannot be blank" }
            val parts = compositeValue.split(DELIMITER, limit = 2)
            return if (parts.size == 2 && parts[0].isNotBlank() && parts[1].isNotBlank()) {
                StoryId(sourceId = parts[0], rawId = parts[1])
            } else {
                // Fallback for raw legacy IDs without delimiter
                StoryId(sourceId = "default", rawId = compositeValue)
            }
        }
    }
}
