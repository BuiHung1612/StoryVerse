package com.slowbuild.storyverse.core.dispatcher

import kotlin.test.Test
import kotlin.test.assertNotNull

class DispatcherProviderTest {

    @Test
    fun default_dispatcher_provider_provides_non_null_dispatchers() {
        val provider: DispatcherProvider = DefaultDispatcherProvider()
        assertNotNull(provider.main)
        assertNotNull(provider.io)
        assertNotNull(provider.default)
        assertNotNull(provider.unconfined)
    }
}
