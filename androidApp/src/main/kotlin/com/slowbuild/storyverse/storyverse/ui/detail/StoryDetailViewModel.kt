package com.slowbuild.storyverse.storyverse.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slowbuild.storyverse.core.result.AppResult
import com.slowbuild.storyverse.data.local.LocalStoryCache
import com.slowbuild.storyverse.data.local.room.dao.StoryDao
import com.slowbuild.storyverse.data.local.room.entity.StoryEntity
import com.slowbuild.storyverse.domain.model.Chapter
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
    val isInLibrary: Boolean = false,
    val errorMessage: String? = null
)

class StoryDetailViewModel(
    private val storySourceRegistry: StorySourceRegistry,
    private val localStoryCache: LocalStoryCache,
    private val storyDao: StoryDao,
    private val readerRepository: ReaderRepository
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

            // Check cached story first
            val cachedStory = localStoryCache.getCachedStory(storyId)
            val cachedChapters = localStoryCache.getCachedChapters(storyId)

            if (cachedStory != null && cachedChapters.isNotEmpty()) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        story = cachedStory,
                        chapters = cachedChapters,
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
                        localStoryCache.cacheChapters(detail.chapters)

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                story = detail.story,
                                chapters = detail.chapters,
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
