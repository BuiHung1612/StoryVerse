package com.slowbuild.storyverse.di

import com.slowbuild.storyverse.core.dispatcher.DispatcherProvider
import com.slowbuild.storyverse.data.local.LocalStoryCache
import com.slowbuild.storyverse.data.local.room.StoryVerseDatabase
import com.slowbuild.storyverse.data.repository.RoomReaderRepository
import com.slowbuild.storyverse.data.source.catalog.DriveCatalogStorySource
import com.slowbuild.storyverse.domain.i18n.AppStringKey
import com.slowbuild.storyverse.domain.i18n.AppStrings
import com.slowbuild.storyverse.domain.i18n.LocalizationRepository
import com.slowbuild.storyverse.domain.repository.ReaderRepository
import com.slowbuild.storyverse.domain.source.StorySourceRegistry
import com.slowbuild.storyverse.domain.theme.AppTheme
import com.slowbuild.storyverse.domain.theme.ThemeRepository
import io.ktor.client.HttpClient
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.get
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class KoinModuleTest : KoinTest {

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun koin_initialization_resolves_core_and_data_dependencies() {
        initKoin()
        val dispatcherProvider = get<DispatcherProvider>()
        assertNotNull(dispatcherProvider)

        val localizationRepo = get<LocalizationRepository>()
        assertNotNull(localizationRepo)
        assertEquals("StoryVerse", AppStrings.get(AppStringKey.APP_NAME))

        val themeRepo = get<ThemeRepository>()
        assertNotNull(themeRepo)
        assertNotNull(AppTheme.currentColors)

        val httpClient = get<HttpClient>()
        assertNotNull(httpClient)

        val driveSource = get<DriveCatalogStorySource>()
        assertNotNull(driveSource)

        val database = get<StoryVerseDatabase>()
        assertNotNull(database)

        val localCache = get<LocalStoryCache>()
        assertNotNull(localCache)

        val readerRepo = get<ReaderRepository>()
        assertNotNull(readerRepo)

        val registry = get<StorySourceRegistry>()
        assertNotNull(registry)
        assertNotNull(registry.getDefaultSource())
        assertEquals(2, registry.getAllSources().size)
    }
}
