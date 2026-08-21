package com.slowbuild.storyverse.storyverse.ui.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.slowbuild.storyverse.domain.i18n.AppStringKey
import com.slowbuild.storyverse.domain.i18n.AppStrings
import com.slowbuild.storyverse.storyverse.ui.common.ErrorView
import com.slowbuild.storyverse.storyverse.ui.common.LoadingView
import com.slowbuild.storyverse.storyverse.ui.common.StoryVerseTopBar
import org.koin.androidx.compose.koinViewModel

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

    LaunchedEffect(storyId, chapterId) {
        viewModel.loadChapter(storyId, chapterId)
        listState.scrollToItem(0)
    }

    Scaffold(
        topBar = {
            AnimatedVisibility(
                visible = uiState.showControls,
                enter = slideInVertically(initialOffsetY = { -it }),
                exit = slideOutVertically(targetOffsetY = { -it })
            ) {
                StoryVerseTopBar(
                    title = uiState.currentChapter?.title ?: com.slowbuild.storyverse.storyverse.theme.localizedString(AppStringKey.APP_NAME),
                    canNavigateBack = true,
                    onNavigateBack = onNavigateBack
                )
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = uiState.showControls,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                ReaderBottomControls(
                    onPrevChapter = {
                        viewModel.getPrevChapter()?.let { onNavigateChapter(it.id) }
                    },
                    onNextChapter = {
                        viewModel.getNextChapter()?.let { onNavigateChapter(it.id) }
                    },
                    hasPrev = viewModel.getPrevChapter() != null,
                    hasNext = viewModel.getNextChapter() != null,
                    fontSizeSp = uiState.fontSizeSp,
                    onIncreaseFont = { viewModel.increaseFontSize() },
                    onDecreaseFont = { viewModel.decreaseFontSize() }
                )
            }
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                LoadingView(modifier = Modifier.padding(innerPadding))
            }
            uiState.errorMessage != null -> {
                ErrorView(
                    modifier = Modifier.padding(innerPadding),
                    message = uiState.errorMessage ?: com.slowbuild.storyverse.storyverse.theme.localizedString(AppStringKey.ERROR_UNKNOWN),
                    onRetry = { viewModel.loadChapter(storyId, chapterId) }
                )
            }
            uiState.chapterContent != null -> {
                val content = uiState.chapterContent!!
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { viewModel.toggleControls() }
                        )
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Chapter Title Header
                        item {
                            Text(
                                text = content.title,
                                fontSize = (uiState.fontSizeSp + 5).sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                            )
                        }

                        // Paragraphs
                        items(content.paragraphs) { paragraph ->
                            Text(
                                text = paragraph,
                                fontSize = uiState.fontSizeSp.sp,
                                lineHeight = (uiState.fontSizeSp * 1.55f).sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        // End of chapter navigation buttons
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val prevChapter = viewModel.getPrevChapter()
                                val nextChapter = viewModel.getNextChapter()

                                if (prevChapter != null) {
                                    OutlinedButton(
                                        onClick = { onNavigateChapter(prevChapter.id) },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = null
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "Chương trước", maxLines = 1)
                                    }
                                }

                                if (nextChapter != null) {
                                    Button(
                                        onClick = { onNavigateChapter(nextChapter.id) },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(text = "Chương sau", maxLines = 1)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(36.dp))
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
                .padding(horizontal = 20.dp, vertical = 12.dp)
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

                // Font Size Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onDecreaseFont,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Text(
                            text = "A-",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "${fontSizeSp.toInt()} pt",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    IconButton(
                        onClick = onIncreaseFont,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Text(
                            text = "A+",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
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
