package com.slowbuild.storyverse.storyverse.di

import com.slowbuild.storyverse.storyverse.ui.detail.StoryDetailViewModel
import com.slowbuild.storyverse.storyverse.ui.home.HomeViewModel
import com.slowbuild.storyverse.storyverse.ui.library.LibraryViewModel
import com.slowbuild.storyverse.storyverse.ui.reader.ReaderViewModel
import com.slowbuild.storyverse.storyverse.ui.search.SearchViewModel
import com.slowbuild.storyverse.storyverse.ui.settings.SettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { HomeViewModel(get()) }
    viewModel { SearchViewModel(get()) }
    viewModel { LibraryViewModel(get(), get()) }
    viewModel { StoryDetailViewModel(get(), get(), get(), get()) }
    viewModel { ReaderViewModel(get(), get(), get()) }
    viewModel { SettingsViewModel(get(), get(), get()) }
}
