package com.slowbuild.storyverse.data.theme

import com.slowbuild.storyverse.core.logging.AppLogger
import com.slowbuild.storyverse.data.theme.presets.DarkThemeColors
import com.slowbuild.storyverse.data.theme.presets.ForestThemeColors
import com.slowbuild.storyverse.data.theme.presets.LightThemeColors
import com.slowbuild.storyverse.data.theme.presets.MidnightThemeColors
import com.slowbuild.storyverse.data.theme.presets.ParchmentThemeColors
import com.slowbuild.storyverse.data.theme.presets.SepiaThemeColors
import com.slowbuild.storyverse.domain.theme.AppThemePreset
import com.slowbuild.storyverse.domain.theme.ThemeColors
import com.slowbuild.storyverse.domain.theme.ThemeMode
import com.slowbuild.storyverse.domain.theme.ThemeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemeRepositoryImpl(
    initialPreset: AppThemePreset = AppThemePreset.DEFAULT,
    initialThemeMode: ThemeMode = ThemeMode.SYSTEM,
    customPresets: Map<AppThemePreset, ThemeColors> = emptyMap()
) : ThemeRepository {

    private val presetMap = mutableMapOf<AppThemePreset, ThemeColors>(
        AppThemePreset.LIGHT to LightThemeColors,
        AppThemePreset.DARK to DarkThemeColors,
        AppThemePreset.SEPIA to SepiaThemeColors,
        AppThemePreset.PARCHMENT to ParchmentThemeColors,
        AppThemePreset.MIDNIGHT to MidnightThemeColors,
        AppThemePreset.FOREST to ForestThemeColors
    ).apply {
        putAll(customPresets)
    }

    private val _currentPreset = MutableStateFlow(initialPreset)
    override val currentPreset: StateFlow<AppThemePreset> = _currentPreset.asStateFlow()

    private val _currentThemeMode = MutableStateFlow(initialThemeMode)
    override val currentThemeMode: StateFlow<ThemeMode> = _currentThemeMode.asStateFlow()

    private val _currentColors = MutableStateFlow(getColorsForPreset(initialPreset))
    override val currentColors: StateFlow<ThemeColors> = _currentColors.asStateFlow()

    override fun setPreset(preset: AppThemePreset) {
        _currentPreset.value = preset
        _currentColors.value = getColorsForPreset(preset)
        AppLogger.i("Theme") { "Theme preset changed to: ${preset.id} (${preset.displayNameEn})" }
    }

    override fun setThemeMode(mode: ThemeMode) {
        _currentThemeMode.value = mode
        AppLogger.i("Theme") { "Theme mode changed to: $mode" }
    }

    override fun getColorsForPreset(preset: AppThemePreset): ThemeColors {
        return presetMap[preset] ?: LightThemeColors
    }

    override fun getAllPresets(): List<AppThemePreset> {
        return AppThemePreset.entries
    }

    fun registerCustomPreset(preset: AppThemePreset, colors: ThemeColors) {
        presetMap[preset] = colors
        if (_currentPreset.value == preset) {
            _currentColors.value = colors
        }
        AppLogger.i("Theme") { "Registered custom theme preset: ${preset.id}" }
    }
}
