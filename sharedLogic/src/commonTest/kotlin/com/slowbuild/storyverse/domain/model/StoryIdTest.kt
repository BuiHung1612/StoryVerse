package com.slowbuild.storyverse.domain.model

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class StoryIdTest {

    @Test
    fun storyId_creation_and_composite_value() {
        val storyId = StoryId.create("tangthuvien", "pham-nhan-tu-tien")
        assertEquals("tangthuvien", storyId.sourceId)
        assertEquals("pham-nhan-tu-tien", storyId.rawId)
        assertEquals("tangthuvien::pham-nhan-tu-tien", storyId.value)
        assertEquals("tangthuvien::pham-nhan-tu-tien", storyId.toString())
    }

    @Test
    fun storyId_parses_from_composite_string() {
        val composite = "local_epub::my_novel_123"
        val parsed = StoryId.from(composite)
        assertEquals("local_epub", parsed.sourceId)
        assertEquals("my_novel_123", parsed.rawId)
        assertEquals(composite, parsed.value)
    }

    @Test
    fun storyId_collision_safety_with_same_raw_id_different_sources() {
        val sourceA = StoryId.create("sourceA", "novel-100")
        val sourceB = StoryId.create("sourceB", "novel-100")

        assertNotEquals(sourceA, sourceB)
        assertNotEquals(sourceA.value, sourceB.value)
    }

    @Test
    fun storyId_validation_rejects_blank_inputs() {
        assertFailsWith<IllegalArgumentException> {
            StoryId.create("", "rawId")
        }
        assertFailsWith<IllegalArgumentException> {
            StoryId.create("sourceId", "   ")
        }
    }

    @Test
    fun storyId_serialization_and_deserialization() {
        val original = StoryId.create("ai_generated", "project-uuid-999")
        val json = Json.encodeToString(StoryId.serializer(), original)
        val deserialized = Json.decodeFromString(StoryId.serializer(), json)

        assertEquals(original, deserialized)
        assertEquals(original.value, deserialized.value)
    }
}
