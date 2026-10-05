package com.puzzle.game.analytics

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AnalyticsTest {
    @Test
    fun normalizesAndBoundsEventProperties() {
        val properties = linkedMapOf<String, Any?>(
            " story_id " to "little-red",
            "nullable" to null,
            "long_value" to "x".repeat(200)
        )

        val normalized = normalizeAnalyticsProperties(properties)

        assertEquals("little-red", normalized["story_id"])
        assertFalse("nullable" in normalized)
        assertEquals(128, normalized.getValue("long_value").length)
    }

    @Test
    fun traceIdsAreScopedAndUnique() {
        val first = Analytics.newTraceId("game")
        val second = Analytics.newTraceId("game")

        assertTrue(first.startsWith("game_"))
        assertTrue(second.startsWith("game_"))
        assertTrue(first != second)
    }
}
