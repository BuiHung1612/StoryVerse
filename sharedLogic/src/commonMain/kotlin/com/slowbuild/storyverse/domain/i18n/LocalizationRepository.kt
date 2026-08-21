package com.slowbuild.storyverse.domain.i18n

import kotlinx.coroutines.flow.StateFlow

interface LocalizationRepository {
    val currentLanguage: StateFlow<AppLanguage>

    fun setLanguage(language: AppLanguage)

    fun getString(key: AppStringKey, vararg args: Any): String

    fun getSupportedLanguages(): List<AppLanguage>
}
