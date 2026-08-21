package com.slowbuild.storyverse.storyverse.ui.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.slowbuild.storyverse.domain.i18n.AppStringKey
import com.slowbuild.storyverse.domain.model.Bookmark
import com.slowbuild.storyverse.domain.reader.ReaderFontFamily
import com.slowbuild.storyverse.domain.reader.ReaderPreferences
import com.slowbuild.storyverse.domain.reader.ReaderThemePreset
import com.slowbuild.storyverse.storyverse.theme.localizedString
import com.slowbuild.storyverse.storyverse.ui.common.EmptyView
import com.slowbuild.storyverse.storyverse.ui.common.ErrorView
import com.slowbuild.storyverse.storyverse.ui.common.LoadingView
import com.slowbuild.storyverse.storyverse.ui.common.StoryVerseTopBar
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

data class ReaderThemeColors(
    val background: Color,
    val text: Color
)

fun getReaderThemeColors(
    preset: ReaderThemePreset,
    defaultBg: Color,
    defaultText: Color
): ReaderThemeColors {
    return when (preset) {
        ReaderThemePreset.DEFAULT -> ReaderThemeColors(defaultBg, defaultText)
        ReaderThemePreset.LIGHT -> ReaderThemeColors(Color(0xFFFFFFFF), Color(0xFF1A1A1A))
        ReaderThemePreset.SEPIA -> ReaderThemeColors(Color(0xFFFBF0D9), Color(0xFF5F4B32))
        ReaderThemePreset.DARK -> ReaderThemeColors(Color(0xFF1E1E24), Color(0xFFE0E0E0))
        ReaderThemePreset.BLACK -> ReaderThemeColors(Color(0xFF000000), Color(0xFFA0A0A0))
    }
}

fun getComposeFontFamily(family: ReaderFontFamily): FontFamily {
    return when (family) {
        ReaderFontFamily.SYSTEM -> FontFamily.Default
        ReaderFontFamily.SERIF -> FontFamily.Serif
        ReaderFontFamily.SANS_SERIF -> FontFamily.SansSerif
        ReaderFontFamily.MONOSPACE -> FontFamily.Monospace
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    storyId: String,
    chapterId: String,
    onNavigateBack: () -> Unit,
    onNavigateChapter: (chapterId: String) -> Unit,
    viewModel: ReaderViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val colors = getReaderThemeColors(
        preset = uiState.preferences.themePreset,
        defaultBg = MaterialTheme.colorScheme.background,
        defaultText = MaterialTheme.colorScheme.onBackground
    )

    LaunchedEffect(storyId, chapterId) {
        viewModel.loadChapter(storyId, chapterId)
        listState.scrollToItem(0)
    }

    // Debounced scroll progress tracking
    LaunchedEffect(listState) {
        snapshotFlow {
            val firstVisible = listState.firstVisibleItemIndex
            val totalItems = listState.layoutInfo.totalItemsCount.coerceAtLeast(1)
            firstVisible to (firstVisible.toFloat() / totalItems)
        }
            .distinctUntilChanged()
            .collect { (firstVisible, progress) ->
                viewModel.onScrollPositionChanged(firstVisible, progress)
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        when {
            uiState.isLoading -> {
                LoadingView(modifier = Modifier.fillMaxSize())
            }
            uiState.errorMessage != null -> {
                ErrorView(
                    modifier = Modifier.fillMaxSize(),
                    message = uiState.errorMessage ?: localizedString(AppStringKey.ERROR_UNKNOWN),
                    onRetry = { viewModel.loadChapter(storyId, chapterId) }
                )
            }
            uiState.chapterContent != null -> {
                val content = uiState.chapterContent!!
                val prefs = uiState.preferences

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { viewModel.toggleControls() }
                        )
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            horizontal = prefs.horizontalPaddingDp.dp,
                            vertical = 24.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(prefs.paragraphSpacingDp.dp)
                    ) {
                        // Chapter Title & Meta Header
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp, bottom = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = content.title,
                                    fontSize = (prefs.fontSize + 5).sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = getComposeFontFamily(prefs.fontFamily),
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (uiState.wordCount > 0) {
                                        Text(
                                            text = "${uiState.wordCount} từ",
                                            fontSize = 12.sp,
                                            color = colors.text.copy(alpha = 0.7f)
                                        )
                                        Text(
                                            text = "•",
                                            fontSize = 12.sp,
                                            color = colors.text.copy(alpha = 0.4f)
                                        )
                                    }
                                    Text(
                                        text = "~${uiState.estimatedReadMinutes} phút đọc",
                                        fontSize = 12.sp,
                                        color = colors.text.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }

                        // Paragraphs
                        items(content.paragraphs) { paragraph ->
                            Text(
                                text = paragraph,
                                fontSize = prefs.fontSize.sp,
                                fontFamily = getComposeFontFamily(prefs.fontFamily),
                                lineHeight = (prefs.fontSize * prefs.lineSpacingMultiplier).sp,
                                color = colors.text
                            )
                        }

                        // End of chapter navigation buttons
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val prev = uiState.prevChapter
                                val next = uiState.nextChapter

                                if (prev != null) {
                                    OutlinedButton(
                                        onClick = { onNavigateChapter(prev.id) },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = null
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = localizedString(AppStringKey.READER_PREV_CHAPTER), maxLines = 1)
                                    }
                                }

                                if (next != null) {
                                    Button(
                                        onClick = { onNavigateChapter(next.id) },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(text = localizedString(AppStringKey.READER_NEXT_CHAPTER), maxLines = 1)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(48.dp))
                        }
                    }
                }
            }
        }

        // Floating TopBar overlay
        AnimatedVisibility(
            visible = uiState.showControls,
            enter = slideInVertically(initialOffsetY = { -it }),
            exit = slideOutVertically(targetOffsetY = { -it })
        ) {
            StoryVerseTopBar(
                title = uiState.currentChapter?.title ?: localizedString(AppStringKey.APP_NAME),
                canNavigateBack = true,
                onNavigateBack = onNavigateBack,
                actions = {
                    IconButton(onClick = { viewModel.toggleBookmark() }) {
                        Icon(
                            imageVector = if (uiState.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (uiState.isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { viewModel.toggleBookmarksSheet() }) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = "Saved Bookmarks",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { viewModel.toggleTocSheet() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.FormatListBulleted,
                            contentDescription = "Table of Contents",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }

        // Floating BottomBar overlay
        AnimatedVisibility(
            modifier = Modifier.align(Alignment.BottomCenter),
            visible = uiState.showControls,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            ReaderBottomControls(
                onPrevChapter = {
                    uiState.prevChapter?.let { onNavigateChapter(it.id) }
                },
                onNextChapter = {
                    uiState.nextChapter?.let { onNavigateChapter(it.id) }
                },
                onOpenToc = { viewModel.toggleTocSheet() },
                onOpenSettings = { viewModel.toggleSettingsSheet() },
                hasPrev = uiState.prevChapter != null,
                hasNext = uiState.nextChapter != null,
                fontSizeSp = uiState.preferences.fontSize,
                onIncreaseFont = { viewModel.increaseFontSize() },
                onDecreaseFont = { viewModel.decreaseFontSize() }
            )
        }

        // Table of Contents Modal Bottom Sheet
        if (uiState.showTocSheet) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.hideTocSheet() },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Mục Lục",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${uiState.chapters.size} chương",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(uiState.chapters) { chapter ->
                            val isCurrent = chapter.id == uiState.currentChapter?.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isCurrent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else Color.Transparent)
                                    .clickable {
                                        scope.launch {
                                            viewModel.hideTocSheet()
                                            onNavigateChapter(chapter.id)
                                        }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = chapter.title,
                                    fontSize = 14.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isCurrent) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Reader Preferences & Settings Bottom Sheet
        if (uiState.showSettingsSheet) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.hideSettingsSheet() },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                ReaderSettingsBottomSheet(
                    preferences = uiState.preferences,
                    onUpdatePreferences = { viewModel.setFontSize(it.fontSize) },
                    onSetFontSize = { viewModel.setFontSize(it) },
                    onSetFontFamily = { viewModel.setFontFamily(it) },
                    onSetLineSpacing = { viewModel.setLineSpacing(it) },
                    onSetThemePreset = { viewModel.setThemePreset(it) },
                    onSetHorizontalPadding = { viewModel.setHorizontalPadding(it) }
                )
            }
        }

        // Bookmarks Bottom Sheet
        if (uiState.showBookmarksSheet) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.hideBookmarksSheet() },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Đánh Dấu Trang (${uiState.bookmarks.size})",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    if (uiState.bookmarks.isEmpty()) {
                        EmptyView(
                            title = "Chưa có đánh dấu",
                            message = "Nhấn biểu tượng đánh dấu ở thanh tiêu đề để lưu lại trang đang đọc"
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(350.dp),
                            contentPadding = PaddingValues(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.bookmarks) { bookmark ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                        .clickable {
                                            viewModel.hideBookmarksSheet()
                                            onNavigateChapter(bookmark.chapterId)
                                        }
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = bookmark.chapterTitle,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (!bookmark.note.isNullOrBlank()) {
                                            Text(
                                                text = bookmark.note!!,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 2
                                            )
                                        }
                                    }
                                    IconButton(onClick = { viewModel.deleteBookmark(bookmark.id) }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Bookmark",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReaderBottomControls(
    onPrevChapter: () -> Unit,
    onNextChapter: () -> Unit,
    onOpenToc: () -> Unit,
    onOpenSettings: () -> Unit,
    hasPrev: Boolean,
    hasNext: Boolean,
    fontSizeSp: Float,
    onIncreaseFont: () -> Unit,
    onDecreaseFont: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Prev Chapter Button
                IconButton(
                    onClick = onPrevChapter,
                    enabled = hasPrev
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous Chapter",
                        tint = if (hasPrev) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                    )
                }

                // Table of Contents Button
                IconButton(onClick = onOpenToc) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.FormatListBulleted,
                        contentDescription = "Table of Contents",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Reader Preferences & Settings
                IconButton(onClick = onOpenSettings) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Reader Settings",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // Next Chapter Button
                IconButton(
                    onClick = onNextChapter,
                    enabled = hasNext
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next Chapter",
                        tint = if (hasNext) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ReaderSettingsBottomSheet(
    preferences: ReaderPreferences,
    onUpdatePreferences: (ReaderPreferences) -> Unit,
    onSetFontSize: (Float) -> Unit,
    onSetFontFamily: (ReaderFontFamily) -> Unit,
    onSetLineSpacing: (Float) -> Unit,
    onSetThemePreset: (ReaderThemePreset) -> Unit,
    onSetHorizontalPadding: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Tùy Chỉnh Đọc",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Theme Presets
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "Màu nền đọc", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(ReaderThemePreset.entries) { preset ->
                    val isSelected = preferences.themePreset == preset
                    val bg = when (preset) {
                        ReaderThemePreset.DEFAULT -> MaterialTheme.colorScheme.background
                        ReaderThemePreset.LIGHT -> Color(0xFFFFFFFF)
                        ReaderThemePreset.SEPIA -> Color(0xFFFBF0D9)
                        ReaderThemePreset.DARK -> Color(0xFF1E1E24)
                        ReaderThemePreset.BLACK -> Color(0xFF000000)
                    }
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(bg)
                            .border(
                                width = if (isSelected) 2.5.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                shape = CircleShape
                            )
                            .clickable { onSetThemePreset(preset) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                }
            }
        }

        // Font Size
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Cỡ chữ", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "${preferences.fontSize.toInt()} pt", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onSetFontSize(preferences.fontSize - 1.5f) },
                    modifier = Modifier
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                ) {
                    Text(text = "A-", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                IconButton(
                    onClick = { onSetFontSize(preferences.fontSize + 1.5f) },
                    modifier = Modifier
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                ) {
                    Text(text = "A+", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Font Family
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "Phông chữ", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ReaderFontFamily.entries) { family ->
                    val isSelected = preferences.fontFamily == family
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSetFontFamily(family) },
                        label = { Text(family.displayName, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }

        // Line Spacing
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "Giãn dòng", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(1.3f to "Gọn", 1.55f to "Vừa", 1.85f to "Thoáng").forEach { (spacing, label) ->
                    val isSelected = kotlin.math.abs(preferences.lineSpacingMultiplier - spacing) < 0.1f
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSetLineSpacing(spacing) },
                        label = { Text(label, fontSize = 12.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
