package com.slowbuild.storyverse.data.i18n.dictionary

import com.slowbuild.storyverse.domain.i18n.AppStringKey

val EnglishDictionary: Map<AppStringKey, String> = mapOf(
    // App Shell & Navigation
    AppStringKey.APP_NAME to "StoryVerse",
    AppStringKey.TAB_DISCOVER to "Discover",
    AppStringKey.TAB_SEARCH to "Search",
    AppStringKey.TAB_LIBRARY to "Library",
    AppStringKey.TAB_SETTINGS to "Settings",
    AppStringKey.TAB_AI_STUDIO to "AI Studio",

    // Home & Discovery Sections
    AppStringKey.SECTION_FEATURED to "Featured Stories",
    AppStringKey.SECTION_POPULAR to "Most Popular",
    AppStringKey.SECTION_LATEST to "Latest Updates",
    AppStringKey.SECTION_COMPLETED to "Completed Stories",
    AppStringKey.SECTION_RECOMMENDED to "Recommended For You",
    AppStringKey.VIEW_MORE to "View more",
    AppStringKey.HOT_STORIES to "Trending",
    AppStringKey.DISCOVER_TITLE to "Explore The Universe of Stories",

    // Search & Filter
    AppStringKey.SEARCH_HINT to "Search stories, authors...",
    AppStringKey.SEARCH_NO_RESULTS to "No matching stories found",
    AppStringKey.SEARCH_RECENT_TITLE to "Recent Searches",
    AppStringKey.SEARCH_CLEAR_HISTORY to "Clear History",
    AppStringKey.FILTER_ALL to "All",
    AppStringKey.FILTER_STATUS to "Status",
    AppStringKey.FILTER_CATEGORY to "Genre",
    AppStringKey.FILTER_SORT_BY to "Sort By",
    AppStringKey.SORT_POPULAR to "Most Popular",
    AppStringKey.SORT_LATEST to "Latest",
    AppStringKey.SORT_CHAPTERS to "Chapters",
    AppStringKey.SORT_RATING to "Rating",

    // Story Statuses
    AppStringKey.STATUS_ONGOING to "Ongoing",
    AppStringKey.STATUS_COMPLETED to "Completed",
    AppStringKey.STATUS_HIATUS to "Hiatus",
    AppStringKey.STATUS_UNKNOWN to "Unknown",

    // Story Detail
    AppStringKey.DETAIL_AUTHOR to "Author",
    AppStringKey.DETAIL_CATEGORY to "Genre",
    AppStringKey.DETAIL_STATUS to "Status",
    AppStringKey.DETAIL_CHAPTERS to "chapters",
    AppStringKey.DETAIL_RATING to "Rating",
    AppStringKey.DETAIL_VIEWS to "views",
    AppStringKey.DETAIL_READ_NOW to "Read First",
    AppStringKey.DETAIL_CONTINUE_READING to "Continue Reading",
    AppStringKey.DETAIL_ADD_LIBRARY to "Add to Library",
    AppStringKey.DETAIL_IN_LIBRARY to "In Library",
    AppStringKey.DETAIL_DOWNLOAD to "Download",
    AppStringKey.DETAIL_DOWNLOADED to "Downloaded Offline",
    AppStringKey.DETAIL_DESCRIPTION_TITLE to "Description",
    AppStringKey.DETAIL_CHAPTER_LIST_TITLE to "Table of Contents",
    AppStringKey.DETAIL_LATEST_CHAPTER_FORMAT to "Latest chapter: {0}",

    // Reader
    AppStringKey.READER_CHAPTER_INDEX_FORMAT to "Chapter {0}: {1}",
    AppStringKey.READER_NEXT_CHAPTER to "Next Chapter",
    AppStringKey.READER_PREV_CHAPTER to "Previous Chapter",
    AppStringKey.READER_FONT_SIZE to "Font Size",
    AppStringKey.READER_LINE_SPACING to "Line Spacing",
    AppStringKey.READER_THEME to "Reading Theme",
    AppStringKey.READER_BOOKMARK_ADDED to "Bookmark added",
    AppStringKey.READER_BOOKMARK_REMOVED to "Bookmark removed",
    AppStringKey.READER_CHAPTER_LOADING to "Loading chapter content...",
    AppStringKey.READER_CHAPTER_LOADING_FAILED to "Failed to load chapter. Tap to retry.",
    AppStringKey.READER_TOC_TITLE to "Chapters",
    AppStringKey.READER_PROGRESS_FORMAT to "Reading {0}%",

    // Library & History
    AppStringKey.LIBRARY_TITLE to "My Library",
    AppStringKey.LIBRARY_EMPTY_TITLE to "Your library is empty",
    AppStringKey.LIBRARY_EMPTY_SUBTITLE to "Discover stories and save your favorites here!",
    AppStringKey.LIBRARY_TAB_FAVORITES to "Favorites",
    AppStringKey.LIBRARY_TAB_HISTORY to "History",
    AppStringKey.LIBRARY_TAB_DOWNLOADS to "Downloads",
    AppStringKey.LIBRARY_TAB_LOCAL_FILES to "EPUB Files",
    AppStringKey.LIBRARY_CLEAR_HISTORY_CONFIRM to "Are you sure you want to clear reading history?",

    // Settings
    AppStringKey.SETTINGS_TITLE to "Settings",
    AppStringKey.SETTINGS_SECTION_APPEARANCE to "Appearance & Display",
    AppStringKey.SETTINGS_THEME_TITLE to "Theme Mode",
    AppStringKey.SETTINGS_SECTION_LANGUAGE to "Language",
    AppStringKey.SETTINGS_LANGUAGE_TITLE to "App Language",
    AppStringKey.SETTINGS_SECTION_STORAGE to "Storage & Cache",
    AppStringKey.SETTINGS_STORAGE_USAGE to "Used Storage",
    AppStringKey.SETTINGS_ACTIVE_SOURCE to "Active Story Source: {0}",
    AppStringKey.SETTINGS_CLEAR_CACHE to "Clear Cache",
    AppStringKey.SETTINGS_CLEAR_CACHE_SUCCESS to "Cache cleared successfully",
    AppStringKey.SETTINGS_SECTION_ABOUT to "About",
    AppStringKey.SETTINGS_VERSION_FORMAT to "Version {0}",
    AppStringKey.SETTINGS_DEVELOPER to "Developed by SlowBuild Team",

    // Common Actions & Dialogs
    AppStringKey.ACTION_CONFIRM to "Confirm",
    AppStringKey.ACTION_CANCEL to "Cancel",
    AppStringKey.ACTION_RETRY to "Retry",
    AppStringKey.ACTION_DELETE to "Delete",
    AppStringKey.ACTION_SAVE to "Save",
    AppStringKey.ACTION_CLOSE to "Close",
    AppStringKey.ACTION_BACK to "Back",
    AppStringKey.ACTION_SHARE to "Share",
    AppStringKey.ACTION_REFRESH to "Refresh",

    // Error Messages
    AppStringKey.ERROR_NETWORK to "No internet connection. Please check your network.",
    AppStringKey.ERROR_UNKNOWN to "An unexpected error occurred. Please try again later.",
    AppStringKey.ERROR_NOT_FOUND to "The requested data was not found.",
    AppStringKey.ERROR_TIMEOUT to "Connection timed out. Please try again.",
    AppStringKey.ERROR_SOURCE_UNAVAILABLE to "Story source is currently unavailable."
)
