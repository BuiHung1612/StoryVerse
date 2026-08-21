package com.slowbuild.storyverse.storyverse.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slowbuild.storyverse.core.result.AppResult
import com.slowbuild.storyverse.data.local.LocalStoryCache
import com.slowbuild.storyverse.data.local.room.dao.StoryDao
import com.slowbuild.storyverse.data.local.room.entity.StoryEntity
import com.slowbuild.storyverse.domain.download.DownloadManager
import com.slowbuild.storyverse.domain.download.DownloadProgress
import com.slowbuild.storyverse.domain.model.Chapter
import com.slowbuild.storyverse.domain.model.DownloadStatus
import com.slowbuild.storyverse.domain.model.ReadingProgress
import com.slowbuild.storyverse.domain.model.Story
import com.slowbuild.storyverse.domain.model.StoryDetail
import com.slowbuild.storyverse.domain.model.StoryId
import com.slowbuild.storyverse.domain.repository.ReaderRepository
import com.slowbuild.storyverse.domain.source.StorySourceRegistry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StoryDetailUiState(
    val isLoading: Boolean = true,
    val story: Story? = null,
    val chapters: List<Chapter> = emptyList(),
    val readingProgress: ReadingProgress? = null,
    val downloadProgress: DownloadProgress? = null,
    val isDownloaded: Boolean = false,
    val isInLibrary: Boolean = false,
    val errorMessage: String? = null
)

class StoryDetailViewModel(
    private val storySourceRegistry: StorySourceRegistry,
    private val localStoryCache: LocalStoryCache,
    private val storyDao: StoryDao,
    private val readerRepository: ReaderRepository,
    private val downloadManager: DownloadManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(StoryDetailUiState())
    val uiState: StateFlow<StoryDetailUiState> = _uiState.asStateFlow()

    private var currentStoryId: StoryId? = null

    fun loadStory(rawOrCompositeId: String) {
        val storyId = StoryId.from(rawOrCompositeId)
        currentStoryId = storyId

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // Observe reading progress
            launch {
                readerRepository.observeReadingProgress(storyId).collect { progress ->
                    _uiState.update { it.copy(readingProgress = progress) }
                }
            }

            // Observe download progress
            launch {
                downloadManager.observeDownloadProgress(storyId).collect { progress ->
                    _uiState.update {
                        it.copy(
                            downloadProgress = progress,
                            isDownloaded = progress?.status == DownloadStatus.COMPLETED || it.isDownloaded
                        )
                    }
                    if (progress?.status == DownloadStatus.COMPLETED) {
                        val refreshedChapters = localStoryCache.getCachedChapters(storyId)
                        if (refreshedChapters.isNotEmpty()) {
                            _uiState.update { it.copy(chapters = refreshedChapters, isDownloaded = true) }
                        }
                    }
                }
            }

            // Check if already downloaded
            val isDownloaded = downloadManager.isStoryDownloaded(storyId)

            // Check cached story first
            val cachedStory = localStoryCache.getCachedStory(storyId)
            val cachedChapters = localStoryCache.getCachedChapters(storyId)

            if (cachedStory != null && cachedChapters.isNotEmpty()) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        story = cachedStory,
                        chapters = cachedChapters,
                        isDownloaded = isDownloaded,
                        isInLibrary = cachedStory.inLibrary
                    )
                }
            }

            // Fetch latest from remote source
            val source = storySourceRegistry.getSource(storyId.sourceId)
                ?: storySourceRegistry.getDefaultSource()

            if (source != null) {
                when (val result = source.getStoryDetail(storyId.rawId)) {
                    is AppResult.Success -> {
                        val detail = result.data
                        localStoryCache.cacheStory(detail.story)
                        if (cachedChapters.isEmpty()) {
                            localStoryCache.cacheChapters(detail.chapters)
                        }

                        val latestChapters = localStoryCache.getCachedChapters(storyId).ifEmpty { detail.chapters }

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                story = detail.story,
                                chapters = latestChapters,
                                isDownloaded = isDownloaded,
                                isInLibrary = cachedStory?.inLibrary ?: detail.story.inLibrary,
                                errorMessage = null
                            )
                        }
                    }
                    is AppResult.Error -> {
                        if (_uiState.value.story == null) {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = result.error.message
                                )
                            }
                        }
                    }
                }
            } else if (_uiState.value.story == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Không tìm thấy nguồn truyện"
                    )
                }
            }
        }
    }

    fun startDownload() {
        val story = _uiState.value.story ?: return
        val downloadUrl = _uiState.value.chapters.firstOrNull()?.url ?: return

        viewModelScope.launch {
            downloadManager.downloadStory(story, downloadUrl).collect { progress ->
                _uiState.update { it.copy(downloadProgress = progress) }
                if (progress.status == DownloadStatus.COMPLETED) {
                    val updatedChapters = localStoryCache.getCachedChapters(story.id)
                    _uiState.update {
                        it.copy(
                            chapters = updatedChapters,
                            isDownloaded = true,
                            isInLibrary = true
                        )
                    }
                }
            }
        }
    }

    fun toggleLibrary() {
        val story = _uiState.value.story ?: return
        val nextInLibrary = !_uiState.value.isInLibrary
        _uiState.update { it.copy(isInLibrary = nextInLibrary) }

        viewModelScope.launch {
            val updatedStory = story.copy(inLibrary = nextInLibrary)
            storyDao.insertOrUpdate(StoryEntity.fromDomain(updatedStory, lastAccessedAt = System.currentTimeMillis()))
        }
    }
}
