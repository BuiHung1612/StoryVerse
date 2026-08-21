package com.slowbuild.storyverse.storyverse

import android.app.Application
import com.slowbuild.storyverse.core.logging.AppLogger
import com.slowbuild.storyverse.di.initKoin
import com.slowbuild.storyverse.storyverse.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class StoryVerseApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidLogger()
            androidContext(this@StoryVerseApplication)
            modules(appModule)
        }

        AppLogger.i("StoryVerseApplication") { "StoryVerse Android Application started" }
    }
}
