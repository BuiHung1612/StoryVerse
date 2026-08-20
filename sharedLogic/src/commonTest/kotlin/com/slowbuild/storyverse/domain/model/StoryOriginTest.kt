package com.slowbuild.storyverse.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StoryOriginTest {

    @Test
    fun story_origins_are_distinguishable_without_reader_coupling() {
        val remoteStory = Story(
            id = StoryId.create("truyenfull", "pham-nhan"),
            title = "Phàm Nhân Tu Tiên",
            origin = StoryOrigin.REMOTE
        )

        val epubStory = Story(
            id = StoryId.create("local_epub", "hash_abc_123"),
            title = "Tây Du Ký",
            origin = StoryOrigin.LOCAL_EPUB
        )

        val aiStory = Story(
            id = StoryId.create("ai_generated", "project_xyz_789"),
            title = "Vũ Trụ Nguyên Thủy",
            origin = StoryOrigin.AI_GENERATED
        )

        assertEquals(StoryOrigin.REMOTE, remoteStory.origin)
        assertEquals(StoryOrigin.LOCAL_EPUB, epubStory.origin)
        assertEquals(StoryOrigin.AI_GENERATED, aiStory.origin)

        // All stories share identical domain model structure and can be read by the same reader pipeline
        val storyList = listOf(remoteStory, epubStory, aiStory)
        assertEquals(3, storyList.size)
        assertTrue(storyList.all { it.title.isNotEmpty() })
    }
}
