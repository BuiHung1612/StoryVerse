package com.slowbuild.storyverse.di

import android.content.Context
import com.slowbuild.storyverse.core.platform.AndroidContextProvider
import com.slowbuild.storyverse.core.platform.AndroidPlatform
import com.slowbuild.storyverse.core.platform.Platform
import com.slowbuild.storyverse.data.local.room.StoryVerseDatabase
import com.slowbuild.storyverse.data.local.room.getDatabaseBuilder
import com.slowbuild.storyverse.data.local.room.getInMemoryDatabaseBuilder
import org.koin.dsl.module

actual val platformModule = module {
    single<Platform> { AndroidPlatform() }
    single<StoryVerseDatabase> {
        val context = getOrNull<Context>() ?: AndroidContextProvider.context
        val builder = if (context != null) {
            getDatabaseBuilder(context)
        } else {
            getInMemoryDatabaseBuilder()
        }
        StoryVerseDatabase.getRoomDatabase(builder)
    }
}
