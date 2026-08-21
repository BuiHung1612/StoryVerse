package com.slowbuild.storyverse.storyverse.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slowbuild.storyverse.domain.i18n.AppLanguage
import com.slowbuild.storyverse.domain.i18n.LocalizationRepository
import com.slowbuild.storyverse.domain.source.StorySourceRegistry
import com.slowbuild.storyverse.domain.theme.AppThemePreset
import com.slowbuild.storyverse.domain.theme.ThemeColors
import com.slowbuild.storyverse.domain.theme.ThemeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val currentThemePreset: AppThemePreset = AppThemePreset.LIGHT,
    val currentLanguage: AppLanguage = AppLanguage.VIETNAMESE,
    val availableThemes: List<AppThemePreset> = AppThemePreset.entries,
    val availableLanguages: List<AppLanguage> = AppLanguage.entries,
    val activeSourceName: String = ""
)

class SettingsViewModel(
    private val themeRepository: ThemeRepository,
    private val localizationRepository: LocalizationRepository,
    private val storySourceRegistry: StorySourceRegistry
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            themeRepository.currentPreset.collect { preset ->
                _uiState.update { it.copy(currentThemePreset = preset) }
            }
        }

        viewModelScope.launch {
            localizationRepository.currentLanguage.collect { lang ->
                _uiState.update { it.copy(currentLanguage = lang) }
            }
        }

        val defaultSource = storySourceRegistry.getDefaultSource()
        _uiState.update {
            it.copy(
                activeSourceName = defaultSource?.metadata?.name ?: "StoryVerse Drive Catalog"
            )
        }
    }

    fun selectTheme(preset: AppThemePreset) {
        viewModelScope.launch {
            themeRepository.setPreset(preset)
        }
    }

    fun selectLanguage(language: AppLanguage) {
        viewModelScope.launch {
            localizationRepository.setLanguage(language)
        }
    }

    fun getColorsForPreset(preset: AppThemePreset): ThemeColors {
        return themeRepository.getColorsForPreset(preset)
    }
}
