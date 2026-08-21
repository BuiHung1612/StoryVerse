package com.slowbuild.storyverse.domain.i18n

fun getLocalizedString(key: AppStringKey): String = AppStrings.get(key)
fun getLocalizedString(key: AppStringKey, arg: String): String = AppStrings.get(key, arg)
