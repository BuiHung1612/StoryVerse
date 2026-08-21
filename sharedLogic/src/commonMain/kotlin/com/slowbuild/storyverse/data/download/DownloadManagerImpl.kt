package com.slowbuild.storyverse.data.download

import com.slowbuild.storyverse.core.epub.EpubParser
import com.slowbuild.storyverse.core.logging.AppLogger
import com.slowbuild.storyverse.core.result.AppError
import com.slowbuild.storyverse.core.result.AppResult
import com.slowbuild.storyverse.core.storage.getAppStorageDirectory
import com.slowbuild.storyverse.core.time.currentTimeMillis
import com.slowbuild.storyverse.data.local.LocalStoryCache
import com.slowbuild.storyverse.data.local.room.dao.DownloadDao
import com.slowbuild.storyverse.data.local.room.dao.StoryDao
import com.slowbuild.storyverse.data.local.room.entity.DownloadEntity
import com.slowbuild.storyverse.domain.download.DownloadManager
import com.slowbuild.storyverse.domain.download.DownloadProgress
import com.slowbuild.storyverse.domain.model.Chapter
import com.slowbuild.storyverse.domain.model.ChapterContent
import com.slowbuild.storyverse.domain.model.DownloadStatus
import com.slowbuild.storyverse.domain.model.Story
import com.slowbuild.storyverse.domain.model.StoryDownloadSummary
import com.slowbuild.storyverse.domain.model.StoryId
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.contentLength
import io.ktor.utils.io.readRemaining
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.io.readByteArray
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.buffer

class DownloadManagerImpl(
    private val httpClient: HttpClient,
    private val localStoryCache: LocalStoryCache,
    private val downloadDao: DownloadDao,
    private val storyDao: StoryDao,
    private val epubParser: EpubParser = EpubParser(),
    private val fileSystem: FileSystem = FileSystem.SYSTEM
) : DownloadManager {

    private val progressMap = MutableStateFlow<Map<String, DownloadProgress>>(emptyMap())
    private val scope = CoroutineScope(Dispatchers.IO)

    private fun getEpubDirectory(): okio.Path {
        val baseDir = getAppStorageDirectory().toPath()
        val epubDir = baseDir / "epubs"
        if (!fileSystem.exists(epubDir)) {
            fileSystem.createDirectories(epubDir)
        }
        return epubDir
    }

    private fun getEpubFilePath(storyId: StoryId): okio.Path {
        val safeName = storyId.value.replace(":", "_").replace("/", "_")
        return getEpubDirectory() / "$safeName.epub"
    }

    private fun sanitizeDownloadUrl(url: String): String {
        return when {
            url.contains("drive.google.com/uc?") && !url.contains("confirm=") -> {
                "$url&confirm=t"
            }
            url.contains("drive.google.com/file/d/") -> {
                val fileId = Regex("/file/d/([^/?]+)").find(url)?.groupValues?.get(1)
                if (fileId != null) "https://drive.google.com/uc?export=download&id=$fileId&confirm=t" else url
            }
            else -> url
        }
    }

    override fun downloadStory(story: Story, downloadUrl: String): Flow<DownloadProgress> = flow {
        val storyId = story.id
        val targetPath = getEpubFilePath(storyId)

        val initialProgress = DownloadProgress(
            storyId = storyId,
            status = DownloadStatus.DOWNLOADING,
            progress = 0.05f
        )
        emit(initialProgress)
        updateProgress(initialProgress)

        try {
            val actualUrl = sanitizeDownloadUrl(downloadUrl)
            AppLogger.i("DownloadManager") { "Starting download for '${story.title}' from $actualUrl" }

            val response = httpClient.get(actualUrl)
            if (response.status.value !in 200..299) {
                throw Exception("Tải tệp thất bại: HTTP ${response.status.value} ${response.status.description}")
            }

            val totalBytes = response.contentLength() ?: 0L
            val channel = response.bodyAsChannel()

            fileSystem.delete(targetPath, mustExist = false)
            val sink = fileSystem.sink(targetPath)
            val bufferedSink = sink.buffer()

            var downloadedBytes = 0L

            while (!channel.isClosedForRead) {
                val packet = channel.readRemaining(16384)
                while (!packet.exhausted()) {
                    val bytes = packet.readByteArray()
                    bufferedSink.write(bytes)
                    downloadedBytes += bytes.size

                    val ratio = if (totalBytes > 0) {
                        (downloadedBytes.toFloat() / totalBytes).coerceIn(0.05f, 0.90f)
                    } else {
                        (0.05f + (downloadedBytes.toFloat() / (2 * 1024 * 1024)).coerceIn(0f, 0.85f))
                    }

                    val currentProgress = DownloadProgress(
                        storyId = storyId,
                        status = DownloadStatus.DOWNLOADING,
                        progress = ratio,
                        downloadedBytes = downloadedBytes,
                        totalBytes = if (totalBytes > 0) totalBytes else downloadedBytes
                    )
                    emit(currentProgress)
                    updateProgress(currentProgress)
                }
            }
            bufferedSink.flush()
            bufferedSink.close()

            if (downloadedBytes == 0L) {
                throw Exception("Tệp tải về rỗng (0 bytes). Vui lòng kiểm tra lại liên kết.")
            }

            // 2. Parse EPUB into Chapters and ChapterContent
            emit(DownloadProgress(storyId = storyId, status = DownloadStatus.DOWNLOADING, progress = 0.95f))
            AppLogger.i("DownloadManager") { "EPUB file saved to $targetPath ($downloadedBytes bytes). Parsing chapters..." }

            val parseResult = epubParser.parse(targetPath)
            if (parseResult is AppResult.Error) {
                throw Exception(parseResult.error.message)
            }

            val parsedEpub = (parseResult as AppResult.Success).data
            val domainChapters = parsedEpub.chapters.map { epChapter ->
                Chapter(
                    id = "${storyId.value}_${epChapter.index}",
                    storyId = storyId,
                    index = epChapter.index,
                    title = epChapter.title,
                    url = epChapter.href,
                    isDownloaded = true,
                    isRead = false,
                    wordCount = epChapter.wordCount,
                    publishedAt = null
                )
            }

            val domainContents = parsedEpub.chapters.map { epChapter ->
                ChapterContent(
                    chapterId = "${storyId.value}_${epChapter.index}",
                    storyId = storyId,
                    title = epChapter.title,
                    content = epChapter.paragraphs.joinToString("\n\n"),
                    paragraphs = epChapter.paragraphs,
                    wordCount = epChapter.wordCount,
                    sourceInfo = story.authorNames
                )
            }

            // 3. Cache into Room database
            localStoryCache.cacheStory(story.copy(inLibrary = true, totalChapters = domainChapters.size))
            localStoryCache.cacheChapters(domainChapters)
            localStoryCache.cacheChapterContents(domainContents)

            // 4. Save Download Entity
            val downloadEntity = DownloadEntity(
                storyId = storyId.value,
                downloadedChapters = domainChapters.size,
                totalChapters = domainChapters.size,
                bytesDownloaded = downloadedBytes,
                totalBytes = downloadedBytes,
                errorMessage = null,
                status = DownloadStatus.COMPLETED.name,
                updatedAt = currentTimeMillis()
            )
            downloadDao.insertOrUpdate(downloadEntity)

            val completedProgress = DownloadProgress(
                storyId = storyId,
                status = DownloadStatus.COMPLETED,
                progress = 1.0f,
                downloadedBytes = downloadedBytes,
                totalBytes = downloadedBytes
            )
            emit(completedProgress)
            updateProgress(completedProgress)

            AppLogger.i("DownloadManager") { "Successfully completed download & indexed ${domainChapters.size} chapters for '${story.title}'" }
        } catch (e: Exception) {
            AppLogger.e("DownloadManager", e) { "Failed to download '${story.title}': ${e.message}" }
            val failedProgress = DownloadProgress(
                storyId = storyId,
                status = DownloadStatus.FAILED,
                errorMessage = e.message ?: "Tải truyện thất bại"
            )
            emit(failedProgress)
            updateProgress(failedProgress)
        }
    }

    override suspend fun startDownload(
        story: Story,
        downloadUrl: String,
        onProgress: (DownloadProgress) -> Unit
    ): AppResult<Unit> {
        return try {
            downloadStory(story, downloadUrl).collect { progress ->
                onProgress(progress)
            }
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(AppError.Network(e.message ?: "Tải truyện thất bại"))
        }
    }

    private fun updateProgress(progress: DownloadProgress) {
        progressMap.update { current ->
            current + (progress.storyId.value to progress)
        }
    }

    override fun observeDownloadProgress(storyId: StoryId): Flow<DownloadProgress?> {
        return progressMap.map { it[storyId.value] }
    }

    override fun observeAllDownloads(): Flow<List<StoryDownloadSummary>> {
        return downloadDao.observeAllDownloads().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observeStoryDownload(storyId: StoryId): Flow<StoryDownloadSummary?> {
        return downloadDao.observeDownloadState(storyId.value).map { entity ->
            entity?.toDomain()
        }
    }

    override suspend fun deleteDownload(storyId: StoryId): AppResult<Unit> {
        return try {
            val path = getEpubFilePath(storyId)
            fileSystem.delete(path, mustExist = false)
            downloadDao.deleteDownload(storyId.value)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(AppError.Storage("Lỗi xóa bản tải về: ${e.message}"))
        }
    }

    override suspend fun isStoryDownloaded(storyId: StoryId): Boolean {
        val path = getEpubFilePath(storyId)
        return fileSystem.exists(path)
    }
}
