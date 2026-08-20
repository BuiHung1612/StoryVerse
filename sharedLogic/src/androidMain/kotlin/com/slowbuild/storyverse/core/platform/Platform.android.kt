package com.slowbuild.storyverse.core.platform

import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android API ${Build.VERSION.SDK_INT}"
    override val isAndroid: Boolean = true
    override val isIos: Boolean = false
}

actual fun getPlatform(): Platform = AndroidPlatform()
