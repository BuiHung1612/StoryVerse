package com.slowbuild.storyverse.core.platform

interface Platform {
    val name: String
    val isAndroid: Boolean
    val isIos: Boolean
}

expect fun getPlatform(): Platform
