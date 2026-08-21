package com.slowbuild.storyverse.domain.i18n

import com.slowbuild.storyverse.data.i18n.LocalizationRepositoryImpl
import com.slowbuild.storyverse.data.i18n.dictionary.EnglishDictionary
import com.slowbuild.storyverse.data.i18n.dictionary.VietnameseDictionary
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LocalizationRepositoryTest {

    @Test
    fun translation_dictionaries_have_complete_parity_with_all_keys() {
        val allKeys = AppStringKey.entries

        val missingInVietnamese = allKeys.filter { !VietnameseDictionary.containsKey(it) }
        val missingInEnglish = allKeys.filter { !EnglishDictionary.containsKey(it) }

        assertTrue(
            missingInVietnamese.isEmpty(),
            "Missing translations in Vietnamese dictionary: $missingInVietnamese"
        )
        assertTrue(
            missingInEnglish.isEmpty(),
            "Missing translations in English dictionary: $missingInEnglish"
        )
        assertEquals(allKeys.size, VietnameseDictionary.size)
        assertEquals(allKeys.size, EnglishDictionary.size)
    }

    @Test
    fun repository_returns_correct_strings_for_each_language() {
        val repo = LocalizationRepositoryImpl(initialLanguage = AppLanguage.VIETNAMESE)

        assertEquals("Khám Phá", repo.getString(AppStringKey.TAB_DISCOVER))
        assertEquals("Tủ Sách", repo.getString(AppStringKey.TAB_LIBRARY))

        repo.setLanguage(AppLanguage.ENGLISH)

        assertEquals("Discover", repo.getString(AppStringKey.TAB_DISCOVER))
        assertEquals("Library", repo.getString(AppStringKey.TAB_LIBRARY))
    }

    @Test
    fun repository_formats_string_arguments_properly() {
        val repo = LocalizationRepositoryImpl(initialLanguage = AppLanguage.VIETNAMESE)

        val formattedVi = repo.getString(AppStringKey.READER_CHAPTER_INDEX_FORMAT, 1, "Hàn Lập Tu Tiên")
        assertEquals("Chương 1: Hàn Lập Tu Tiên", formattedVi)

        repo.setLanguage(AppLanguage.ENGLISH)
        val formattedEn = repo.getString(AppStringKey.READER_CHAPTER_INDEX_FORMAT, 1, "Beginning of Journey")
        assertEquals("Chapter 1: Beginning of Journey", formattedEn)
    }

    @Test
    fun repository_falls_back_when_key_is_missing() {
        val repo = LocalizationRepositoryImpl(
            initialLanguage = AppLanguage.ENGLISH,
            customDictionaries = mapOf(
                AppLanguage.ENGLISH to mapOf(AppStringKey.APP_NAME to "StoryVerse")
            )
        )

        // Key defined in custom dictionary
        assertEquals("StoryVerse", repo.getString(AppStringKey.APP_NAME))
        // Key not in custom dictionary falls back to default dictionary
        assertEquals("Khám Phá", repo.getString(AppStringKey.TAB_DISCOVER))
    }
}
