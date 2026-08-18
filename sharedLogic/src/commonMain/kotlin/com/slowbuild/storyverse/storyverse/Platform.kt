package com.slowbuild.storyverse.storyverse

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform