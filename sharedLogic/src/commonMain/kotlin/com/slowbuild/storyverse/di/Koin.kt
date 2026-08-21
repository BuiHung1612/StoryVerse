package com.slowbuild.storyverse.di

import com.slowbuild.storyverse.core.dispatcher.DefaultDispatcherProvider
import com.slowbuild.storyverse.core.dispatcher.DispatcherProvider
import com.slowbuild.storyverse.data.i18n.LocalizationRepositoryImpl
import com.slowbuild.storyverse.data.local.LocalStoryCache
import com.slowbuild.storyverse.data.local.room.StoryVerseDatabase
import com.slowbuild.storyverse.data.network.client.HttpClientFactory
import com.slowbuild.storyverse.data.repository.RoomReaderRepository
import com.slowbuild.storyverse.data.source.StorySourceRegistryImpl
import com.slowbuild.storyverse.data.source.catalog.DriveCatalogStorySource
import com.slowbuild.storyverse.data.source.stub.StubStorySource
import com.slowbuild.storyverse.data.theme.ThemeRepositoryImpl
import com.slowbuild.storyverse.domain.i18n.AppStrings
import com.slowbuild.storyverse.domain.i18n.LocalizationRepository
import com.slowbuild.storyverse.domain.repository.ReaderRepository
import com.slowbuild.storyverse.domain.source.StorySourceRegistry
import com.slowbuild.storyverse.domain.theme.AppTheme
import com.slowbuild.storyverse.domain.theme.ThemeRepository
import com.slowbuild.storyverse.core.epub.EpubParser
import io.ktor.client.HttpClient
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

import com.slowbuild.storyverse.data.reader.ReaderPreferencesRepositoryImpl
import com.slowbuild.storyverse.domain.reader.ReaderPreferencesRepository
import com.slowbuild.storyverse.domain.usecase.ReaderUseCase

val coreModule = module {
    single<DispatcherProvider> { DefaultDispatcherProvider() }
    single<LocalizationRepository> { LocalizationRepositoryImpl() }
    single<ThemeRepository> { ThemeRepositoryImpl() }
    single<ReaderPreferencesRepository> { ReaderPreferencesRepositoryImpl() }
}

val domainModule = module {
    single {
        ReaderUseCase(
            storySourceRegistry = get(),
            localStoryCache = get(),
            readerRepository = get(),
            readerPreferencesRepository = get()
        )
    }
}

val dataModule = module {
    single<HttpClient> { HttpClientFactory.create() }
    single { DriveCatalogStorySource(httpClient = get()) }
    single<StorySourceRegistry> {
        StorySourceRegistryImpl(
            initialSources = listOf(
                get<DriveCatalogStorySource>(),
                StubStorySource()
            )
        )
    }

    // Room DAOs & Local Persistence
    single { get<StoryVerseDatabase>().storyDao() }
    single { get<StoryVerseDatabase>().chapterDao() }
    single { get<StoryVerseDatabase>().chapterContentDao() }
    single { get<StoryVerseDatabase>().readingProgressDao() }
    single { get<StoryVerseDatabase>().bookmarkDao() }
    single { get<StoryVerseDatabase>().historyDao() }
    single { get<StoryVerseDatabase>().downloadDao() }
    single { LocalStoryCache(storyDao = get(), chapterDao = get(), chapterContentDao = get()) }
    single { EpubParser() }
    single<com.slowbuild.storyverse.domain.download.DownloadManager> {
        com.slowbuild.storyverse.data.download.DownloadManagerImpl(
            httpClient = get(),
            localStoryCache = get(),
            downloadDao = get(),
            storyDao = get(),
            epubParser = get()
        )
    }
    single<ReaderRepository> {
        RoomReaderRepository(
            readingProgressDao = get(),
            bookmarkDao = get(),
            historyDao = get()
        )
    }
}

expect val platformModule: Module

fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(
            coreModule,
            domainModule,
            dataModule,
            platformModule,
        )
    }.also { koinApp ->
        val localizationRepo = koinApp.koin.get<LocalizationRepository>()
        AppStrings.initialize(localizationRepo)

        val themeRepo = koinApp.koin.get<ThemeRepository>()
        AppTheme.initialize(themeRepo)
    }

fun initKoinIos() = initKoin {}
