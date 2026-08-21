package com.slowbuild.storyverse.storyverse.ui.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slowbuild.storyverse.core.result.AppResult
import com.slowbuild.storyverse.domain.model.Chapter
import com.slowbuild.storyverse.domain.model.ChapterContent
import com.slowbuild.storyverse.domain.model.Story
import com.slowbuild.storyverse.domain.model.StoryId
import com.slowbuild.storyverse.domain.usecase.ReaderUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReaderUiState(
    val isLoading: Boolean = true,
    val story: Story? = null,
    val currentChapter: Chapter? = null,
    val chapterContent: ChapterContent? = null,
    val chapters: List<Chapter> = emptyList(),
    val prevChapter: Chapter? = null,
    val nextChapter: Chapter? = null,
    val wordCount: Int = 0,
    val estimatedReadMinutes: Int = 1,
    val isBookmarked: Boolean = false,
    val showTocSheet: Boolean = false,
    val fontSizeSp: Float = 17f,
    val errorMessage: String? = null,
    val showControls: Boolean = true
)

class ReaderViewModel(
    private val readerUseCase: ReaderUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReaderUiState())
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    private var currentStoryId: StoryId? = null

    fun loadChapter(rawOrCompositeStoryId: String, chapterId: String) {
        val storyId = StoryId.from(rawOrCompositeStoryId)
        currentStoryId = storyId

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val sessionResult = readerUseCase.loadChapterSession(storyId, chapterId, viewModelScope)) {
                is AppResult.Success -> {
                    val session = sessionResult.data
                    val bookmarks = readerUseCase.observeBookmarks(storyId).firstOrNull().orEmpty()
                    val isBookmarked = bookmarks.any { it.chapterId == chapterId }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            story = session.story,
                            currentChapter = session.currentChapter,
                            chapterContent = session.content,
                            chapters = session.chapters,
                            prevChapter = session.prevChapter,
                            nextChapter = session.nextChapter,
                            wordCount = session.wordCount,
                            estimatedReadMinutes = session.estimatedReadMinutes,
                            isBookmarked = isBookmarked,
                            errorMessage = null
                        )
                    }
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = sessionResult.error.message
                        )
                    }
                }
            }
        }
    }

    fun toggleBookmark() {
        val story = _uiState.value.story ?: return
        val chapter = _uiState.value.currentChapter ?: return

        viewModelScope.launch {
            when (val res = readerUseCase.toggleBookmark(story, chapter)) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isBookmarked = res.data) }
                }
                is AppResult.Error -> Unit
            }
        }
    }

    fun toggleTocSheet() {
        _uiState.update { it.copy(showTocSheet = !it.showTocSheet) }
    }

    fun hideTocSheet() {
        _uiState.update { it.copy(showTocSheet = false) }
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
}
