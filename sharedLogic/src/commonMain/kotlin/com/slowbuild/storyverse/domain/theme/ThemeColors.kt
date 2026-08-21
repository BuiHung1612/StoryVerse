package com.slowbuild.storyverse.domain.theme

import kotlinx.serialization.Serializable

@Serializable
data class ThemeColors(
    val preset: AppThemePreset,
    val isDark: Boolean,

    // Brand & Interactive Colors
    val primary: Long,
    val onPrimary: Long,
    val primaryContainer: Long,
    val onPrimaryContainer: Long,
    val secondary: Long,
    val onSecondary: Long,

    // Surface & Background Colors
    val background: Long,
    val onBackground: Long,
    val surface: Long,
    val onSurface: Long,
    val surfaceVariant: Long,
    val onSurfaceVariant: Long,
    val card: Long,
    val border: Long,

    // Typography & Content Colors
    val textPrimary: Long,
    val textSecondary: Long,
    val textMuted: Long,

    // Dedicated Reader Specific Colors
    val readerBackground: Long,
    val readerTextColor: Long,
    val readerSecondaryTextColor: Long,

    // Functional & Feedback Colors
    val accent: Long,
    val success: Long,
    val warning: Long,
    val error: Long,
    val onError: Long
)
