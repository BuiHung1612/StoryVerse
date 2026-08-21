package com.slowbuild.storyverse.data.local.room

import androidx.room.RoomDatabase

expect fun getDatabaseBuilder(): RoomDatabase.Builder<StoryVerseDatabase>
expect fun getInMemoryDatabaseBuilder(): RoomDatabase.Builder<StoryVerseDatabase>

fun createStoryVerseDatabase(): StoryVerseDatabase {
    val builder = getDatabaseBuilder()
    return StoryVerseDatabase.getRoomDatabase(builder)
}

fun createInMemoryDatabase(): StoryVerseDatabase {
    val builder = getInMemoryDatabaseBuilder()
    return StoryVerseDatabase.getRoomDatabase(builder)
}
