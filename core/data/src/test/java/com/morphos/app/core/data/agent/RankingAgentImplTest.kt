package com.morphos.app.core.data.agent

import com.morphos.app.core.domain.model.ContentCandidate
import com.morphos.app.core.domain.model.ContextSnapshot
import com.morphos.app.core.domain.model.MemoryProfile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RankingAgentImplTest {

    private lateinit var agent: RankingAgentImpl
    private val dummyContext = ContextSnapshot(0, 0L, 80, "WiFi", 0.0, 0.0, false)
    private val dummyProfile = MemoryProfile(emptyMap(), emptyList(), emptyMap())

    @BeforeEach
    fun setUp() {
        agent = RankingAgentImpl()
    }

    private fun createCandidate(id: String, recency: Long, engagement: Int, matchScore: Float): ContentCandidate {
        return ContentCandidate(
            contentId = id,
            recency = recency,
            engagementCount = engagement,
            contextMatchScore = matchScore,
            metadata = emptyMap()
        )
    }

    @Test
    fun emptyList_returnsEmptyList() {
        val result = agent.rankContent(emptyList(), dummyContext, dummyProfile)
        assertTrue(result.isEmpty())
    }

    @Test
    fun singleCandidate_returnsSingleRanked() {
        val candidate = createCandidate("1", System.currentTimeMillis(), 5, 0.8f)
        val result = agent.rankContent(listOf(candidate), dummyContext, dummyProfile)

        assertEquals(1, result.size)
        assertEquals("1", result[0].contentId)
    }

    @Test
    fun highEngagement_ranksHigher_thanLow() {
        val now = System.currentTimeMillis()
        val candLow = createCandidate("low", now, 1, 0.5f)
        val candHigh = createCandidate("high", now, 100, 0.5f)

        val result = agent.rankContent(listOf(candLow, candHigh), dummyContext, dummyProfile)

        assertEquals(2, result.size)
        assertEquals("high", result[0].contentId)
        assertTrue(result[0].score > result[1].score)
    }

    @Test
    fun recentItem_ranksHigher_thanOld() {
        val now = System.currentTimeMillis()
        val oldTime = now - 3600000L * 24 // 24 hours ago
        val candOld = createCandidate("old", oldTime, 5, 0.5f)
        val candRecent = createCandidate("recent", now, 5, 0.5f)

        val result = agent.rankContent(listOf(candOld, candRecent), dummyContext, dummyProfile)

        assertEquals(2, result.size)
        assertEquals("recent", result[0].contentId)
        assertTrue(result[0].score > result[1].score)
    }

    @Test
    fun contextMatch_ranksHigher() {
        val now = System.currentTimeMillis()
        val candLowMatch = createCandidate("low", now, 5, 0.1f)
        val candHighMatch = createCandidate("high", now, 5, 0.9f)

        val result = agent.rankContent(listOf(candLowMatch, candHighMatch), dummyContext, dummyProfile)

        assertEquals(2, result.size)
        assertEquals("high", result[0].contentId)
        assertTrue(result[0].score > result[1].score)
    }

    @Test
    fun scoreNeverExceedsOne() {
        // Even with max engagement and perfect context match
        val candidate = createCandidate("perfect", System.currentTimeMillis(), 1000, 1.0f)
        val result = agent.rankContent(listOf(candidate), dummyContext, dummyProfile)

        assertTrue(result[0].score <= 1.0f)
    }

    @Test
    fun scoreNeverBelowZero() {
        val candidate = createCandidate("terrible", System.currentTimeMillis() - 3600000L * 1000, 0, 0.0f)
        val result = agent.rankContent(listOf(candidate), dummyContext, dummyProfile)

        assertTrue(result[0].score >= 0.0f)
    }
}
