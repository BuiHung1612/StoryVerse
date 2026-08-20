package com.slowbuild.storyverse

import kotlin.test.Test
import kotlin.test.assertTrue

class GreetingTest {

    @Test
    fun greeting_returns_platform_name() {
        val greeting = Greeting().greet()
        assertTrue(greeting.startsWith("Hello from StoryVerse SharedLogic on"))
    }
}
