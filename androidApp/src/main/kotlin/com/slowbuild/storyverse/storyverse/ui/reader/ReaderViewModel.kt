package com.slowbuild.storyverse.storyverse.ui.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slowbuild.storyverse.core.result.AppResult
import com.slowbuild.storyverse.data.local.LocalStoryCache
import com.slowbuild.storyverse.domain.model.Chapter
import com.slowbuild.storyverse.domain.model.ChapterContent
import com.slowbuild.storyverse.domain.model.HistoryEntry
import com.slowbuild.storyverse.domain.model.ReadingProgress
import com.slowbuild.storyverse.domain.model.Story
import com.slowbuild.storyverse.domain.model.StoryId
import com.slowbuild.storyverse.domain.repository.ReaderRepository
import com.slowbuild.storyverse.domain.source.StorySourceRegistry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReaderUiState(
    val isLoading: Boolean = true,
    val story: Story? = null,
    val currentChapter: Chapter? = null,
    val chapterContent: ChapterContent? = null,
    val chapters: List<Chapter> = emptyList(),
    val fontSizeSp: Float = 17f,
    val errorMessage: String? = null,
    val showControls: Boolean = true
)

class ReaderViewModel(
    private val storySourceRegistry: StorySourceRegistry,
    private val localStoryCache: LocalStoryCache,
    private val readerRepository: ReaderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReaderUiState())
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    private var currentStoryId: StoryId? = null

    fun loadChapter(rawOrCompositeStoryId: String, chapterId: String) {
        val storyId = StoryId.from(rawOrCompositeStoryId)
        currentStoryId = storyId

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val story = localStoryCache.getCachedStory(storyId)
            val chapters = localStoryCache.getCachedChapters(storyId)
            val currentChapter = chapters.find { it.id == chapterId }

            _uiState.update {
                it.copy(
                    story = story,
                    chapters = chapters,
                    currentChapter = currentChapter
                )
            }

            // Check cached content
            val cachedContent = localStoryCache.getCachedChapterContent(chapterId)
            if (cachedContent != null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        chapterContent = cachedContent,
                        errorMessage = null
                    )
                }
                saveProgressAndHistory(storyId, story, currentChapter, chapters)
                return@launch
            }

            // Fetch remote content
            val source = storySourceRegistry.getSource(storyId.sourceId)
                ?: storySourceRegistry.getDefaultSource()

            if (source != null) {
                when (val result = source.getChapterContent(chapterId)) {
                    is AppResult.Success -> {
                        val content = result.data
                        localStoryCache.cacheChapterContent(content)

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                chapterContent = content,
                                errorMessage = null
                            )
                        }
                        saveProgressAndHistory(storyId, story, currentChapter, chapters)
                    }
                    is AppResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = result.error.message
                            )
                        }
                    }
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Không tìm thấy nguồn truyện"
                    )
                }
            }
        }
    }

    private suspend fun saveProgressAndHistory(
        storyId: StoryId,
        story: Story?,
        chapter: Chapter?,
        chapters: List<Chapter>
    ) {
        val chapterIdx = chapter?.index ?: 1
        val total = chapters.size.coerceAtLeast(1)
        val progress = ReadingProgress(
            storyId = storyId,
            lastReadChapterId = chapter?.id ?: "",
            lastReadChapterIndex = chapterIdx,
            progressPercentage = chapterIdx.toFloat() / total,
            lastReadAt = System.currentTimeMillis()
        )
        readerRepository.saveReadingProgress(progress)

        if (story != null && chapter != null) {
            val history = HistoryEntry(
                story = story,
                lastReadChapterId = chapter.id,
                lastReadChapterTitle = chapter.title,
                lastReadChapterIndex = chapter.index,
                lastReadAt = System.currentTimeMillis()
            )
            readerRepository.recordHistory(history)
        }
    }

    fun toggleControls() {
        _uiState.update { it.copy(showControls = !it.showControls) }
    }

    fun increaseFontSize() {
        _uiState.update { it.copy(fontSizeSp = (it.fontSizeSp + 1.5f).coerceAtMost(28f)) }
    }

    fun decreaseFontSize() {
        _uiState.update { it.copy(fontSizeSp = (it.fontSizeSp - 1.5f).coerceAtLeast(13f)) }
    }

    fun getNextChapter(): Chapter? {
        val currentIdx = _uiState.value.currentChapter?.index ?: return null
        return _uiState.value.chapters.find { it.index == currentIdx + 1 }
    }

    fun getPrevChapter(): Chapter? {
        val currentIdx = _uiState.value.currentChapter?.index ?: return null
        return _uiState.value.chapters.find { it.index == currentIdx - 1 }
    }
}
