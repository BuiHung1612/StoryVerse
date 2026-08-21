package com.slowbuild.storyverse.data.local.room

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.slowbuild.storyverse.data.local.room.dao.BookmarkDao
import com.slowbuild.storyverse.data.local.room.dao.ChapterContentDao
import com.slowbuild.storyverse.data.local.room.dao.ChapterDao
import com.slowbuild.storyverse.data.local.room.dao.DownloadDao
import com.slowbuild.storyverse.data.local.room.dao.HistoryDao
import com.slowbuild.storyverse.data.local.room.dao.ReadingProgressDao
import com.slowbuild.storyverse.data.local.room.dao.StoryDao
import com.slowbuild.storyverse.data.local.room.entity.BookmarkEntity
import com.slowbuild.storyverse.data.local.room.entity.ChapterContentEntity
import com.slowbuild.storyverse.data.local.room.entity.ChapterEntity
import com.slowbuild.storyverse.data.local.room.entity.DownloadEntity
import com.slowbuild.storyverse.data.local.room.entity.HistoryEntity
import com.slowbuild.storyverse.data.local.room.entity.ReadingProgressEntity
import com.slowbuild.storyverse.data.local.room.entity.StoryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Database(
    entities = [
        StoryEntity::class,
        ChapterEntity::class,
        ChapterContentEntity::class,
        ReadingProgressEntity::class,
        BookmarkEntity::class,
        HistoryEntity::class,
        DownloadEntity::class
    ],
    version = 1,
    exportSchema = false
)
@ConstructedBy(StoryVerseDatabaseConstructor::class)
abstract class StoryVerseDatabase : RoomDatabase() {
    abstract fun storyDao(): StoryDao
    abstract fun chapterDao(): ChapterDao
    abstract fun chapterContentDao(): ChapterContentDao
    abstract fun readingProgressDao(): ReadingProgressDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun historyDao(): HistoryDao
    abstract fun downloadDao(): DownloadDao

    companion object {
        const val DATABASE_NAME = "storyverse.db"

        fun getRoomDatabase(
            builder: RoomDatabase.Builder<StoryVerseDatabase>
        ): StoryVerseDatabase {
            return builder
                .setDriver(BundledSQLiteDriver())
                .setQueryCoroutineContext(Dispatchers.IO)
                .build()
        }
    }
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object StoryVerseDatabaseConstructor : RoomDatabaseConstructor<StoryVerseDatabase>
