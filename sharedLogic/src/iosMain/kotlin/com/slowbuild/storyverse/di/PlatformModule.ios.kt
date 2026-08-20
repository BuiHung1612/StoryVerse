package com.slowbuild.storyverse.di

import com.slowbuild.storyverse.core.platform.IOSPlatform
import com.slowbuild.storyverse.core.platform.Platform
import org.koin.dsl.module

actual val platformModule = module {
    single<Platform> { IOSPlatform() }
}
