package com.slowbuild.storyverse.data.i18n

import com.slowbuild.storyverse.core.logging.AppLogger
import com.slowbuild.storyverse.data.i18n.dictionary.EnglishDictionary
import com.slowbuild.storyverse.data.i18n.dictionary.VietnameseDictionary
import com.slowbuild.storyverse.domain.i18n.AppLanguage
import com.slowbuild.storyverse.domain.i18n.AppStringKey
import com.slowbuild.storyverse.domain.i18n.LocalizationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LocalizationRepositoryImpl(
    initialLanguage: AppLanguage = AppLanguage.DEFAULT,
    customDictionaries: Map<AppLanguage, Map<AppStringKey, String>> = emptyMap()
) : LocalizationRepository {

    private val _currentLanguage = MutableStateFlow(initialLanguage)
    override val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    private val dictionaries = mutableMapOf<AppLanguage, Map<AppStringKey, String>>(
        AppLanguage.VIETNAMESE to VietnameseDictionary,
        AppLanguage.ENGLISH to EnglishDictionary
    ).apply {
        putAll(customDictionaries)
    }

    override fun setLanguage(language: AppLanguage) {
        _currentLanguage.value = language
        AppLogger.i("Localization") { "App language changed to: ${language.code} (${language.displayName})" }
    }

    override fun getString(key: AppStringKey, vararg args: Any): String {
        val lang = _currentLanguage.value
        val template = dictionaries[lang]?.get(key)
            ?: dictionaries[AppLanguage.DEFAULT]?.get(key)
            ?: dictionaries[AppLanguage.ENGLISH]?.get(key)
            ?: key.name

        return if (args.isEmpty()) {
            template
        } else {
            formatTemplate(template, *args)
        }
    }

    override fun getSupportedLanguages(): List<AppLanguage> {
        return dictionaries.keys.toList()
    }

    fun registerDictionary(language: AppLanguage, dictionary: Map<AppStringKey, String>) {
        dictionaries[language] = dictionary
        AppLogger.i("Localization") { "Registered dictionary for language: ${language.code}" }
    }

    private fun formatTemplate(template: String, vararg args: Any): String {
        var result = template
        args.forEachIndexed { index, arg ->
            result = result.replace("{$index}", arg.toString())
        }
        return result
    }
}
