package com.slowbuild.storyverse.core.result

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AppResultTest {

    @Test
    fun success_result_contains_data_and_maps_properly() {
        val result: AppResult<Int> = AppResult.Success(42)

        assertTrue(result.isSuccess)
        assertFalse(result.isError)
        assertEquals(42, result.getOrNull())
        assertNull(result.errorOrNull())

        val mapped = result.map { it * 2 }
        assertEquals(84, mapped.getOrNull())

        var callbackTriggered = false
        result.onSuccess { callbackTriggered = true }
        assertTrue(callbackTriggered)
    }

    @Test
    fun error_result_contains_error_and_preserves_on_map() {
        val error = AppError.Network("Connection failed", 500)
        val result: AppResult<Int> = AppResult.Error(error)

        assertFalse(result.isSuccess)
        assertTrue(result.isError)
        assertNull(result.getOrNull())
        assertEquals(error, result.errorOrNull())

        val mapped = result.map { it * 2 }
        assertTrue(mapped.isError)

        var errorCallbackTriggered = false
        result.onError { errorCallbackTriggered = true }
        assertTrue(errorCallbackTriggered)
    }
}
