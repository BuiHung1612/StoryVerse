package com.slowbuild.storyverse.domain.theme

object AppTheme {
    private var repository: ThemeRepository? = null

    fun initialize(repo: ThemeRepository) {
        repository = repo
    }

    val currentPreset: AppThemePreset
        get() = repository?.currentPreset?.value ?: AppThemePreset.DEFAULT

    val currentColors: ThemeColors
        get() = repository?.currentColors?.value ?: getFallbackColors()

    fun getFallbackColors(): ThemeColors = ThemeColors(
        preset = AppThemePreset.LIGHT,
        isDark = false,
        primary = 0xFF4A6572L,
        onPrimary = 0xFFFFFFFFL,
        primaryContainer = 0xFFE2E8F0L,
        onPrimaryContainer = 0xFF1A202CL,
        secondary = 0xFFF9AA33L,
        onSecondary = 0xFF000000L,
        background = 0xFFF8F9FAL,
        onBackground = 0xFF1A202CL,
        surface = 0xFFFFFFFFL,
        onSurface = 0xFF1A202CL,
        surfaceVariant = 0xFFF1F5F9L,
        onSurfaceVariant = 0xFF64748BL,
        card = 0xFFFFFFFFL,
        border = 0xFFE2E8F0L,
        textPrimary = 0xFF1E293BL,
        textSecondary = 0xFF64748BL,
        textMuted = 0xFF94A3B8L,
        readerBackground = 0xFFFAF9F6L,
        readerTextColor = 0xFF2D3748L,
        readerSecondaryTextColor = 0xFF718096L,
        accent = 0xFFF9AA33L,
        success = 0xFF10B981L,
        warning = 0xFFF59E0BL,
        error = 0xFFEF4444L,
        onError = 0xFFFFFFFFL
    )
}
