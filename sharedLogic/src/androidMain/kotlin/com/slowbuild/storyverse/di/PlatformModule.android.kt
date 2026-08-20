package com.slowbuild.storyverse.di

import com.slowbuild.storyverse.core.platform.AndroidPlatform
import com.slowbuild.storyverse.core.platform.Platform
import org.koin.dsl.module

actual val platformModule = module {
    single<Platform> { AndroidPlatform() }
}
