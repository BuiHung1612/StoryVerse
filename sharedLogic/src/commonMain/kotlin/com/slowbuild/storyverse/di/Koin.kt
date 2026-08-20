package com.slowbuild.storyverse.di

import com.slowbuild.storyverse.core.dispatcher.DefaultDispatcherProvider
import com.slowbuild.storyverse.core.dispatcher.DispatcherProvider
import com.slowbuild.storyverse.data.network.client.HttpClientFactory
import com.slowbuild.storyverse.data.source.StorySourceRegistryImpl
import com.slowbuild.storyverse.data.source.stub.StubStorySource
import com.slowbuild.storyverse.domain.source.StorySourceRegistry
import io.ktor.client.HttpClient
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val coreModule = module {
    single<DispatcherProvider> { DefaultDispatcherProvider() }
}

val domainModule = module {
    // Shared domain services / use cases (Phase 2+)
}

val dataModule = module {
    single<HttpClient> { HttpClientFactory.create() }
    single<StorySourceRegistry> {
        StorySourceRegistryImpl(
            initialSources = listOf(StubStorySource())
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
    }

fun initKoinIos() = initKoin {}
