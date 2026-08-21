package com.slowbuild.storyverse.storyverse.theme

import android.app.Activity
import android.graphics.drawable.ColorDrawable
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.slowbuild.storyverse.domain.i18n.AppLanguage
import com.slowbuild.storyverse.domain.i18n.AppStringKey
import com.slowbuild.storyverse.domain.i18n.AppStrings
import com.slowbuild.storyverse.domain.i18n.LocalizationRepository
import com.slowbuild.storyverse.domain.theme.AppTheme
import com.slowbuild.storyverse.domain.theme.AppThemePreset
import com.slowbuild.storyverse.domain.theme.ThemeColors
import com.slowbuild.storyverse.domain.theme.ThemeRepository
import org.koin.core.context.GlobalContext

val LocalStoryVerseColors = staticCompositionLocalOf<ThemeColors> {
    AppTheme.getFallbackColors()
}

val LocalAppLanguage = staticCompositionLocalOf<AppLanguage> {
    AppLanguage.DEFAULT
}

fun Long.toComposeColor(): Color = Color(this.toInt())

/**
 * Builds a complete MaterialTheme ColorScheme directly from [ThemeColors].
 * Updates immediately and synchronously across all components (TopBar, Scaffold, Content, NavigationBar)
 * without animation lag.
 */
fun ThemeColors.toMaterialColorScheme(): ColorScheme {
    return if (isDark) {
        darkColorScheme(
            primary                = primary.toComposeColor(),
            onPrimary              = onPrimary.toComposeColor(),
            primaryContainer       = primaryContainer.toComposeColor(),
            onPrimaryContainer     = onPrimaryContainer.toComposeColor(),
            inversePrimary         = onPrimaryContainer.toComposeColor(),
            secondary              = secondary.toComposeColor(),
            onSecondary            = onSecondary.toComposeColor(),
            secondaryContainer     = primaryContainer.toComposeColor(),
            onSecondaryContainer   = onPrimaryContainer.toComposeColor(),
            tertiary               = accent.toComposeColor(),
            onTertiary             = onPrimary.toComposeColor(),
            tertiaryContainer      = primaryContainer.toComposeColor(),
            onTertiaryContainer    = onPrimaryContainer.toComposeColor(),
            background             = background.toComposeColor(),
            onBackground           = onBackground.toComposeColor(),
            surface                = surface.toComposeColor(),
            onSurface              = onSurface.toComposeColor(),
            surfaceVariant         = surfaceVariant.toComposeColor(),
            onSurfaceVariant       = onSurfaceVariant.toComposeColor(),
            surfaceTint            = primary.toComposeColor(),
            inverseSurface         = onSurface.toComposeColor(),
            inverseOnSurface       = surface.toComposeColor(),
            error                  = error.toComposeColor(),
            onError                = onError.toComposeColor(),
            errorContainer         = error.toComposeColor().copy(alpha = 0.2f),
            onErrorContainer       = error.toComposeColor(),
            outline                = border.toComposeColor(),
            outlineVariant         = border.toComposeColor().copy(alpha = 0.5f),
            scrim                  = Color(0x99000000),
            surfaceBright          = surface.toComposeColor(),
            surfaceDim             = background.toComposeColor(),
            surfaceContainer       = surface.toComposeColor(),
            surfaceContainerHigh   = surfaceVariant.toComposeColor(),
            surfaceContainerHighest= card.toComposeColor(),
            surfaceContainerLow    = background.toComposeColor(),
            surfaceContainerLowest = background.toComposeColor()
        )
    } else {
        lightColorScheme(
            primary                = primary.toComposeColor(),
            onPrimary              = onPrimary.toComposeColor(),
            primaryContainer       = primaryContainer.toComposeColor(),
            onPrimaryContainer     = onPrimaryContainer.toComposeColor(),
            inversePrimary         = onPrimaryContainer.toComposeColor(),
            secondary              = secondary.toComposeColor(),
            onSecondary            = onSecondary.toComposeColor(),
            secondaryContainer     = primaryContainer.toComposeColor(),
            onSecondaryContainer   = onPrimaryContainer.toComposeColor(),
            tertiary               = accent.toComposeColor(),
            onTertiary             = onPrimary.toComposeColor(),
            tertiaryContainer      = primaryContainer.toComposeColor(),
            onTertiaryContainer    = onPrimaryContainer.toComposeColor(),
            background             = background.toComposeColor(),
            onBackground           = onBackground.toComposeColor(),
            surface                = surface.toComposeColor(),
            onSurface              = onSurface.toComposeColor(),
            surfaceVariant         = surfaceVariant.toComposeColor(),
            onSurfaceVariant       = onSurfaceVariant.toComposeColor(),
            surfaceTint            = primary.toComposeColor(),
            inverseSurface         = onSurface.toComposeColor(),
            inverseOnSurface       = surface.toComposeColor(),
            error                  = error.toComposeColor(),
            onError                = onError.toComposeColor(),
            errorContainer         = error.toComposeColor().copy(alpha = 0.1f),
            onErrorContainer       = error.toComposeColor(),
            outline                = border.toComposeColor(),
            outlineVariant         = border.toComposeColor().copy(alpha = 0.6f),
            scrim                  = Color(0x66000000),
            surfaceBright          = surface.toComposeColor(),
            surfaceDim             = background.toComposeColor(),
            surfaceContainer       = surface.toComposeColor(),
            surfaceContainerHigh   = surfaceVariant.toComposeColor(),
            surfaceContainerHighest= card.toComposeColor(),
            surfaceContainerLow    = background.toComposeColor(),
            surfaceContainerLowest = background.toComposeColor()
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
    val localizationRepository = remember {
        GlobalContext.getOrNull()?.get<LocalizationRepository>()
    }

    val currentColors by (themeRepository?.currentColors
        ?: kotlinx.coroutines.flow.MutableStateFlow(AppTheme.getFallbackColors()))
        .collectAsState(initial = AppTheme.getFallbackColors())

    val currentLanguage by (localizationRepository?.currentLanguage
        ?: kotlinx.coroutines.flow.MutableStateFlow(AppLanguage.DEFAULT))
        .collectAsState(initial = AppLanguage.DEFAULT)

    val activeColors = if (presetOverride != null && themeRepository != null) {
        themeRepository.getColorsForPreset(presetOverride)
    } else {
        currentColors
    }

    val colorScheme = activeColors.toMaterialColorScheme()

    // Sync the native window background and system bar icon colors immediately
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.setBackgroundDrawable(
                    ColorDrawable(Color(activeColors.background.toInt()).toArgb())
                )
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !activeColors.isDark
                insetsController.isAppearanceLightNavigationBars = !activeColors.isDark
            }
        }
    }

    CompositionLocalProvider(
        LocalStoryVerseColors provides activeColors,
        LocalAppLanguage provides currentLanguage
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}

/**
 * Reactive localized string helper for Compose.
 * Automatically recomposes when the language changes.
 */
@Composable
fun localizedString(key: AppStringKey, vararg args: Any): String {
    LocalAppLanguage.current // triggers recomposition on language change
    return AppStrings.get(key, *args)
}
