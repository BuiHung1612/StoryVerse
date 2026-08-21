package com.slowbuild.storyverse.domain.i18n

object AppStrings {
    private var repository: LocalizationRepository? = null

    fun initialize(repo: LocalizationRepository) {
        repository = repo
    }

    fun get(key: AppStringKey, vararg args: Any): String {
        return repository?.getString(key, *args) ?: key.name
    }
}
