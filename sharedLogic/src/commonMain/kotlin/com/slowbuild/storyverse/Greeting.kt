package com.slowbuild.storyverse

import com.slowbuild.storyverse.core.platform.getPlatform

class Greeting {
    private val platform = getPlatform()

    fun greet(): String {
        return "Hello from StoryVerse SharedLogic on ${platform.name}!"
    }
}
