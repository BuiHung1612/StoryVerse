package com.slowbuild.storyverse.storyverse.ui.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slowbuild.storyverse.core.result.AppResult
import com.slowbuild.storyverse.domain.model.Bookmark
import com.slowbuild.storyverse.domain.model.Chapter
import com.slowbuild.storyverse.domain.model.ChapterContent
import com.slowbuild.storyverse.domain.model.Story
import com.slowbuild.storyverse.domain.model.StoryId
import com.slowbuild.storyverse.domain.reader.ReaderFontFamily
import com.slowbuild.storyverse.domain.reader.ReaderPreferences
import com.slowbuild.storyverse.domain.reader.ReaderThemePreset
import com.slowbuild.storyverse.domain.usecase.ReaderUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    val bookmarks: List<Bookmark> = emptyList(),
    val preferences: ReaderPreferences = ReaderPreferences(),
    val showTocSheet: Boolean = false,
    val showSettingsSheet: Boolean = false,
    val showBookmarksSheet: Boolean = false,
    val errorMessage: String? = null,
    val showControls: Boolean = true
)

class ReaderViewModel(
    private val readerUseCase: ReaderUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReaderUiState())
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    private var currentStoryId: StoryId? = null
    private var progressSaveJob: Job? = null

    init {
        viewModelScope.launch {
            readerUseCase.preferences.collect { prefs ->
                _uiState.update { it.copy(preferences = prefs) }
            }
        }
    }

    fun loadChapter(rawOrCompositeStoryId: String, chapterId: String) {
        val storyId = StoryId.from(rawOrCompositeStoryId)
        currentStoryId = storyId

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // Observe bookmarks for this story
            launch {
                readerUseCase.observeBookmarks(storyId).collect { bookmarkList ->
                    val isBookmarked = bookmarkList.any { it.chapterId == chapterId }
                    _uiState.update {
                        it.copy(
                            bookmarks = bookmarkList,
                            isBookmarked = isBookmarked
                        )
                    }
                }
            }

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

    fun deleteBookmark(bookmarkId: String) {
        viewModelScope.launch {
            readerUseCase.deleteBookmark(bookmarkId)
        }
    }

    fun onScrollPositionChanged(scrollOffset: Int, progressPercentage: Float) {
        val storyId = currentStoryId ?: return
        val chapter = _uiState.value.currentChapter ?: return

        progressSaveJob?.cancel()
        progressSaveJob = viewModelScope.launch {
            delay(500) // 500ms debounce
            readerUseCase.saveScrollPosition(
                storyId = storyId,
                chapterId = chapter.id,
                chapterIndex = chapter.index,
                scrollOffset = scrollOffset,
                progressPercentage = progressPercentage
            )
        }
    }

    // Sheet Toggles
    fun toggleTocSheet() {
        _uiState.update { it.copy(showTocSheet = !it.showTocSheet, showSettingsSheet = false, showBookmarksSheet = false) }
    }

    fun hideTocSheet() {
        _uiState.update { it.copy(showTocSheet = false) }
    }

    fun toggleSettingsSheet() {
        _uiState.update { it.copy(showSettingsSheet = !it.showSettingsSheet, showTocSheet = false, showBookmarksSheet = false) }
    }

    fun hideSettingsSheet() {
        _uiState.update { it.copy(showSettingsSheet = false) }
    }

    fun toggleBookmarksSheet() {
        _uiState.update { it.copy(showBookmarksSheet = !it.showBookmarksSheet, showTocSheet = false, showSettingsSheet = false) }
    }

    fun hideBookmarksSheet() {
        _uiState.update { it.copy(showBookmarksSheet = false) }
    }

    fun toggleControls() {
        _uiState.update { it.copy(showControls = !it.showControls) }
    }

    // Preferences Setters
    fun setFontSize(size: Float) {
        readerUseCase.setFontSize(size)
    }

    fun increaseFontSize() {
        val current = _uiState.value.preferences.fontSize
        readerUseCase.setFontSize(current + 1.5f)
    }

    fun decreaseFontSize() {
        val current = _uiState.value.preferences.fontSize
        readerUseCase.setFontSize(current - 1.5f)
    }

    fun setFontFamily(fontFamily: ReaderFontFamily) {
        readerUseCase.setFontFamily(fontFamily)
    }

    fun setLineSpacing(multiplier: Float) {
        readerUseCase.setLineSpacing(multiplier)
    }

    fun setThemePreset(preset: ReaderThemePreset) {
        readerUseCase.setThemePreset(preset)
    }

    fun setHorizontalPadding(paddingDp: Float) {
        readerUseCase.setHorizontalPadding(paddingDp)
    }
}
