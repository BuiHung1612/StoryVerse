package com.slowbuild.storyverse.di

import com.slowbuild.storyverse.data.local.LocalStoryCache
import com.slowbuild.storyverse.domain.i18n.LocalizationRepository
import com.slowbuild.storyverse.domain.repository.ReaderRepository
import com.slowbuild.storyverse.domain.source.StorySourceRegistry
import com.slowbuild.storyverse.domain.theme.ThemeRepository
import com.slowbuild.storyverse.domain.usecase.ReaderUseCase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class KoinHelper : KoinComponent {
    val themeRepository: ThemeRepository by inject()
    val localizationRepository: LocalizationRepository by inject()
    val storySourceRegistry: StorySourceRegistry by inject()
    val readerRepository: ReaderRepository by inject()
    val localStoryCache: LocalStoryCache by inject()
    val readerUseCase: ReaderUseCase by inject()
}
