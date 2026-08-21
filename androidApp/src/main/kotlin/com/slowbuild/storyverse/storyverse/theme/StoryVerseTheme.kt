package com.slowbuild.storyverse.storyverse.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.slowbuild.storyverse.domain.theme.AppTheme
import com.slowbuild.storyverse.domain.theme.AppThemePreset
import com.slowbuild.storyverse.domain.theme.ThemeColors
import com.slowbuild.storyverse.domain.theme.ThemeRepository
import org.koin.core.context.GlobalContext

val LocalStoryVerseColors = staticCompositionLocalOf<ThemeColors> {
    AppTheme.getFallbackColors()
}

fun Long.toComposeColor(): Color = Color(this.toULong())

fun ThemeColors.toMaterialColorScheme(): ColorScheme {
    return if (isDark) {
        darkColorScheme(
            primary = primary.toComposeColor(),
            onPrimary = onPrimary.toComposeColor(),
            primaryContainer = primaryContainer.toComposeColor(),
            onPrimaryContainer = onPrimaryContainer.toComposeColor(),
            secondary = secondary.toComposeColor(),
            onSecondary = onSecondary.toComposeColor(),
            background = background.toComposeColor(),
            onBackground = onBackground.toComposeColor(),
            surface = surface.toComposeColor(),
            onSurface = onSurface.toComposeColor(),
            surfaceVariant = surfaceVariant.toComposeColor(),
            onSurfaceVariant = onSurfaceVariant.toComposeColor(),
            error = error.toComposeColor(),
            onError = onError.toComposeColor(),
            outline = border.toComposeColor()
        )
    } else {
        lightColorScheme(
            primary = primary.toComposeColor(),
            onPrimary = onPrimary.toComposeColor(),
            primaryContainer = primaryContainer.toComposeColor(),
            onPrimaryContainer = onPrimaryContainer.toComposeColor(),
            secondary = secondary.toComposeColor(),
            onSecondary = onSecondary.toComposeColor(),
            background = background.toComposeColor(),
            onBackground = onBackground.toComposeColor(),
            surface = surface.toComposeColor(),
            onSurface = onSurface.toComposeColor(),
            surfaceVariant = surfaceVariant.toComposeColor(),
            onSurfaceVariant = onSurfaceVariant.toComposeColor(),
            error = error.toComposeColor(),
            onError = onError.toComposeColor(),
            outline = border.toComposeColor()
        )
    }
}

@Composable
fun StoryVerseTheme(
    presetOverride: AppThemePreset? = null,
    content: @Composable () -> Unit
) {
    val themeRepository = remember {
        GlobalContext.getOrNull()?.get<ThemeRepository>()
    }

    val currentColors by (themeRepository?.currentColors ?: kotlinx.coroutines.flow.MutableStateFlow(AppTheme.getFallbackColors()))
        .collectAsState(initial = AppTheme.getFallbackColors())

    val activeColors = if (presetOverride != null && themeRepository != null) {
        themeRepository.getColorsForPreset(presetOverride)
    } else {
        currentColors
    }

    CompositionLocalProvider(
        LocalStoryVerseColors provides activeColors
    ) {
        MaterialTheme(
            colorScheme = activeColors.toMaterialColorScheme(),
            content = content
        )
    }
}
