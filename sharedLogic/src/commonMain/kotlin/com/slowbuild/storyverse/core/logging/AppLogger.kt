package com.slowbuild.storyverse.core.logging

import co.touchlab.kermit.Logger
import co.touchlab.kermit.StaticConfig
import co.touchlab.kermit.platformLogWriter

object AppLogger {
    private val logger = Logger(
        config = StaticConfig(logWriterList = listOf(platformLogWriter())),
        tag = "StoryVerse"
    )

    fun d(tag: String? = null, message: () -> String) {
        tag?.let { logger.withTag(it).d(message = message) } ?: logger.d(message = message)
    }

    fun i(tag: String? = null, message: () -> String) {
        tag?.let { logger.withTag(it).i(message = message) } ?: logger.i(message = message)
    }

    fun w(tag: String? = null, throwable: Throwable? = null, message: () -> String) {
        val log = tag?.let { logger.withTag(it) } ?: logger
        if (throwable != null) log.w(throwable = throwable, message = message) else log.w(message = message)
    }

    fun e(tag: String? = null, throwable: Throwable? = null, message: () -> String) {
        val log = tag?.let { logger.withTag(it) } ?: logger
        if (throwable != null) log.e(throwable = throwable, message = message) else log.e(message = message)
    }
}
