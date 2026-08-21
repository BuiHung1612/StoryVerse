package com.slowbuild.storyverse.data.local.room

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.slowbuild.storyverse.core.platform.AndroidContextProvider

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<StoryVerseDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath(StoryVerseDatabase.DATABASE_NAME)
    return Room.databaseBuilder(
        context = appContext,
        klass = StoryVerseDatabase::class.java,
        name = dbFile.absolutePath
    )
}

actual fun getDatabaseBuilder(): RoomDatabase.Builder<StoryVerseDatabase> {
    val context = AndroidContextProvider.context
        ?: throw IllegalStateException("Android Context must be set on AndroidContextProvider before accessing Room database")
    return getDatabaseBuilder(context)
}

actual fun getInMemoryDatabaseBuilder(): RoomDatabase.Builder<StoryVerseDatabase> {
    val context = AndroidContextProvider.context
        ?: try {
            val appProvider = Class.forName("androidx.test.core.app.ApplicationProvider")
            val getAppContextMethod = appProvider.getMethod("getApplicationContext")
            getAppContextMethod.invoke(null) as? Context
        } catch (_: Throwable) {
            null
        }
        ?: createTestContext()

    return Room.inMemoryDatabaseBuilder(context, StoryVerseDatabase::class.java)
}

open class TestContextStub : android.content.ContextWrapper(null) {
    private val tempDir = java.io.File(System.getProperty("java.io.tmpdir"), "storyverse_test_room").apply { mkdirs() }

    override fun getApplicationContext(): Context = this
    override fun getDatabasePath(name: String): java.io.File = java.io.File(tempDir, name)
    override fun getFilesDir(): java.io.File = tempDir
    override fun getNoBackupFilesDir(): java.io.File = tempDir
    override fun getCacheDir(): java.io.File = tempDir
    override fun getPackageName(): String = "com.slowbuild.storyverse"
}

private fun createTestContext(): Context {
    return try {
        val unsafeClass = Class.forName("sun.misc.Unsafe")
        val theUnsafeField = unsafeClass.getDeclaredField("theUnsafe").apply { isAccessible = true }
        val unsafe = theUnsafeField.get(null)
        val allocateInstanceMethod = unsafeClass.getMethod("allocateInstance", Class::class.java)
        allocateInstanceMethod.invoke(unsafe, TestContextStub::class.java) as Context
    } catch (_: Throwable) {
        object : android.content.ContextWrapper(null) {
            override fun getApplicationContext(): Context = this
            override fun getDatabasePath(name: String): java.io.File = java.io.File.createTempFile("test_room", ".db")
            override fun getPackageName(): String = "com.slowbuild.storyverse"
        }
    }
}
