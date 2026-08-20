package com.slowbuild.storyverse.storyverse

import android.app.Application
import com.slowbuild.storyverse.core.logging.AppLogger
import com.slowbuild.storyverse.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class StoryVerseApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidLogger()
            androidContext(this@StoryVerseApplication)
        }

        AppLogger.i("StoryVerseApplication") { "StoryVerse Android Application started" }
    }
}
