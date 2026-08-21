package com.slowbuild.storyverse.domain.theme

import com.slowbuild.storyverse.data.theme.ThemeRepositoryImpl
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ThemeRepositoryTest {

    @Test
    fun theme_repository_initializes_with_default_light_preset() {
        val repo = ThemeRepositoryImpl()

        assertEquals(AppThemePreset.LIGHT, repo.currentPreset.value)
        assertFalse(repo.currentColors.value.isDark)
        assertEquals(ThemeMode.SYSTEM, repo.currentThemeMode.value)
    }

    @Test
    fun theme_repository_switches_presets_and_updates_colors() {
        val repo = ThemeRepositoryImpl()

        repo.setPreset(AppThemePreset.DARK)
        assertEquals(AppThemePreset.DARK, repo.currentPreset.value)
        assertTrue(repo.currentColors.value.isDark)

        repo.setPreset(AppThemePreset.SEPIA)
        assertEquals(AppThemePreset.SEPIA, repo.currentPreset.value)
        assertFalse(repo.currentColors.value.isDark)

        repo.setPreset(AppThemePreset.MIDNIGHT)
        assertEquals(AppThemePreset.MIDNIGHT, repo.currentPreset.value)
        assertTrue(repo.currentColors.value.isDark)
        assertEquals(0xFF000000L, repo.currentColors.value.background)
    }

    @Test
    fun all_theme_presets_have_valid_colors_and_defined_tokens() {
        val repo = ThemeRepositoryImpl()
        val presets = repo.getAllPresets()

        assertEquals(6, presets.size)

        presets.forEach { preset ->
            val colors = repo.getColorsForPreset(preset)
            assertNotNull(colors)
            assertEquals(preset, colors.preset)

            // Verify alpha channel is 0xFF (not transparent 0x00)
            assertTrue((colors.primary and 0xFF000000L) != 0L, "Primary color alpha must not be 0 for $preset")
            assertTrue((colors.background and 0xFF000000L) != 0L, "Background color alpha must not be 0 for $preset")
            assertTrue((colors.readerBackground and 0xFF000000L) != 0L, "Reader background alpha must not be 0 for $preset")
            assertTrue((colors.readerTextColor and 0xFF000000L) != 0L, "Reader text alpha must not be 0 for $preset")

            // Verify text and background are not identical (contrast)
            assertNotEquals(colors.background, colors.textPrimary, "Text and background must differ for $preset")
            assertNotEquals(colors.readerBackground, colors.readerTextColor, "Reader text and background must differ for $preset")
        }
    }
}
