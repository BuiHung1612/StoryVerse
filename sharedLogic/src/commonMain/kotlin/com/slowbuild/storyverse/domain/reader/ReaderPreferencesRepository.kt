package com.slowbuild.storyverse.domain.reader

import kotlinx.coroutines.flow.StateFlow

interface ReaderPreferencesRepository {
    val preferences: StateFlow<ReaderPreferences>
    fun updatePreferences(transform: (ReaderPreferences) -> ReaderPreferences)
    fun setFontSize(size: Float)
    fun setFontFamily(fontFamily: ReaderFontFamily)
    fun setLineSpacing(multiplier: Float)
    fun setThemePreset(preset: ReaderThemePreset)
    fun setHorizontalPadding(paddingDp: Float)
}
