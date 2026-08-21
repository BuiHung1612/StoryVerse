package com.slowbuild.storyverse.domain.i18n

import kotlinx.serialization.Serializable

@Serializable
enum class AppLanguage(
    val code: String,
    val displayName: String,
    val flagEmoji: String
) {
    VIETNAMESE("vi", "Tiếng Việt", "🇻🇳"),
    ENGLISH("en", "English", "🇺🇸");

    companion object {
        val DEFAULT = VIETNAMESE

        fun fromCode(code: String): AppLanguage {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: DEFAULT
        }
    }
}
