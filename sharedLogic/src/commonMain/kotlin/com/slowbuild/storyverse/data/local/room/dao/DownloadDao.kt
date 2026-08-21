package com.slowbuild.storyverse.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.slowbuild.storyverse.data.local.room.entity.DownloadEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(download: DownloadEntity)

    @Query("SELECT * FROM downloads WHERE storyId = :storyId LIMIT 1")
    suspend fun getDownloadState(storyId: String): DownloadEntity?

    @Query("SELECT * FROM downloads WHERE storyId = :storyId LIMIT 1")
    fun observeDownloadState(storyId: String): Flow<DownloadEntity?>

    @Query("SELECT * FROM downloads ORDER BY updatedAt DESC")
    fun observeAllDownloads(): Flow<List<DownloadEntity>>

    @Query("DELETE FROM downloads WHERE storyId = :storyId")
    suspend fun deleteDownload(storyId: String)
}
