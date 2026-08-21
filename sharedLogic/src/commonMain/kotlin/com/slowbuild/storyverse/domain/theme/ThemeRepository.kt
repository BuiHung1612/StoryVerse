package com.slowbuild.storyverse.domain.theme

import kotlinx.coroutines.flow.StateFlow

interface ThemeRepository {
    val currentPreset: StateFlow<AppThemePreset>
    val currentThemeMode: StateFlow<ThemeMode>
    val currentColors: StateFlow<ThemeColors>

    fun setPreset(preset: AppThemePreset)
    fun setThemeMode(mode: ThemeMode)
    fun getColorsForPreset(preset: AppThemePreset): ThemeColors
    fun getAllPresets(): List<AppThemePreset>
}
