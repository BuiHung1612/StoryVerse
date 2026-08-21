package com.slowbuild.storyverse.data.local.room

import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
actual fun getDatabaseBuilder(): RoomDatabase.Builder<StoryVerseDatabase> {
    val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null
    )
    val dbFilePath = (documentDirectory?.path ?: "") + "/${StoryVerseDatabase.DATABASE_NAME}"
    return Room.databaseBuilder<StoryVerseDatabase>(
        name = dbFilePath
    )
}

actual fun getInMemoryDatabaseBuilder(): RoomDatabase.Builder<StoryVerseDatabase> {
    return Room.inMemoryDatabaseBuilder<StoryVerseDatabase>()
}
