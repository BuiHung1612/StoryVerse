package com.slowbuild.storyverse.data.reader

import com.slowbuild.storyverse.core.logging.AppLogger
import com.slowbuild.storyverse.domain.reader.ReaderFontFamily
import com.slowbuild.storyverse.domain.reader.ReaderPreferences
import com.slowbuild.storyverse.domain.reader.ReaderPreferencesRepository
import com.slowbuild.storyverse.domain.reader.ReaderThemePreset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ReaderPreferencesRepositoryImpl(
    initialPreferences: ReaderPreferences = ReaderPreferences()
) : ReaderPreferencesRepository {

    private val _preferences = MutableStateFlow(initialPreferences)
    override val preferences: StateFlow<ReaderPreferences> = _preferences.asStateFlow()

    override fun updatePreferences(transform: (ReaderPreferences) -> ReaderPreferences) {
        _preferences.update(transform)
        AppLogger.i("ReaderPrefs") { "Preferences updated: ${_preferences.value}" }
    }

    override fun setFontSize(size: Float) {
        val clamped = size.coerceIn(12f, 32f)
        updatePreferences { it.copy(fontSize = clamped) }
    }

    override fun setFontFamily(fontFamily: ReaderFontFamily) {
        updatePreferences { it.copy(fontFamily = fontFamily) }
    }

    override fun setLineSpacing(multiplier: Float) {
        val clamped = multiplier.coerceIn(1.2f, 2.2f)
        updatePreferences { it.copy(lineSpacingMultiplier = clamped) }
    }

    override fun setThemePreset(preset: ReaderThemePreset) {
        updatePreferences { it.copy(themePreset = preset) }
    }

    override fun setHorizontalPadding(paddingDp: Float) {
        val clamped = paddingDp.coerceIn(12f, 36f)
        updatePreferences { it.copy(horizontalPaddingDp = clamped) }
    }
}
