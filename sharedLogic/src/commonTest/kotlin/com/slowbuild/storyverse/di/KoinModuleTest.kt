package com.slowbuild.storyverse.di

import com.slowbuild.storyverse.core.dispatcher.DispatcherProvider
import com.slowbuild.storyverse.domain.source.StorySourceRegistry
import io.ktor.client.HttpClient
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.get
import kotlin.test.AfterTest
import kotlin.test.Test
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

        val httpClient = get<HttpClient>()
        assertNotNull(httpClient)

        val registry = get<StorySourceRegistry>()
        assertNotNull(registry)
        assertNotNull(registry.getDefaultSource())
    }
}
