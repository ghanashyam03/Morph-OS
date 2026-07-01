package com.morphos.app.core.data.agent

import com.morphos.app.core.ai.AIEngineManager
import com.morphos.app.core.common.AppDispatchers
import com.morphos.app.core.common.AppResult
import com.morphos.app.core.domain.model.*
import com.morphos.app.core.domain.repository.EmbeddingRepository
import com.morphos.app.core.domain.repository.MemoryRepository
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockKExtension::class)
class MemoryAgentImplTest {

    @MockK
    lateinit var memoryRepository: MemoryRepository

    @MockK
    lateinit var embeddingRepository: EmbeddingRepository

    @MockK
    lateinit var aiEngineManager: AIEngineManager

    private lateinit var agent: MemoryAgentImpl
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testDispatchers = AppDispatchers(
        main = testDispatcher,
        io = testDispatcher,
        default = testDispatcher,
        unconfined = testDispatcher
    )

    @BeforeEach
    fun setUp() {
        agent = MemoryAgentImpl(memoryRepository, embeddingRepository, aiEngineManager, testDispatchers)
    }

    @Test
    fun recordEvent_savedToRepository() = runTest {
        val event = ShortTermEvent("id", ShortTermEventType.WIDGET_TAPPED, "widget_1", System.currentTimeMillis())

        coEvery { memoryRepository.recordShortTermEvent(any()) } returns AppResult.Success(Unit)

        val result = agent.recordEvent(event)

        assertTrue(result is AppResult.Success)
        coVerify(exactly = 1) { memoryRepository.recordShortTermEvent(event) }
    }

    @Test
    fun summarize_withNoEvents_returnsDefaultProfile() = runTest {
        val initialProfile = MemoryProfile(mapOf("test" to 1f), listOf("widget_test"), listOf(12))

        coEvery { memoryRepository.getRecentEvents(any()) } returns flowOf(emptyList())
        coEvery { memoryRepository.getMemoryProfile() } returns initialProfile

        val result = agent.summarizeRecentMemory()

        assertTrue(result is AppResult.Success)
        assertEquals(initialProfile, (result as AppResult.Success).data)
    }

    @Test
    fun summarize_withStudyEvents_updatesPreferences() = runTest {
        val initialProfile = MemoryProfile(emptyMap(), emptyList(), emptyList())
        val events = listOf(
            ShortTermEvent("id_1", ShortTermEventType.WIDGET_TAPPED, "study_widget", System.currentTimeMillis()),
            ShortTermEvent("id_2", ShortTermEventType.WIDGET_TAPPED, "study_widget", System.currentTimeMillis())
        )

        coEvery { memoryRepository.getRecentEvents(any()) } returns flowOf(events)
        coEvery { memoryRepository.getMemoryProfile() } returns initialProfile
        every { aiEngineManager.isTier1Available() } returns false
        every { aiEngineManager.isTier0Available() } returns false
        coEvery { memoryRepository.saveLongTermMemory(any()) } returns AppResult.Success(Unit)
        coEvery { memoryRepository.saveMemoryProfile(any()) } returns AppResult.Success(Unit)

        val result = agent.summarizeRecentMemory()

        assertTrue(result is AppResult.Success)
        val finalProfile = (result as AppResult.Success).data
        assertTrue(finalProfile.preferredWidgetTypes.contains("study_widget"))
    }

    @Test
    fun retrieveRelevant_delegatesToEmbeddingRepository() = runTest {
        val memories = listOf(
            LongTermMemory("id_1", "text pattern", "emb_1", MemoryType.BEHAVIORAL_PATTERN, 0.9f, 0L, 0L)
        )
        val queryEmbedding = floatArrayOf(1f, 0f, 0f)
        val similarEntries = listOf(
            EmbeddingEntry("emb_1", "text pattern", queryEmbedding, 0L)
        )

        coEvery { memoryRepository.getLongTermMemories(any()) } returns flowOf(memories)
        every { aiEngineManager.isTier0Available() } returns true
        coEvery { aiEngineManager.computeEmbedding(any()) } returns queryEmbedding
        coEvery { embeddingRepository.findSimilar(any(), any()) } returns similarEntries

        val result = agent.retrieveRelevantMemories("search query")

        assertTrue(result is AppResult.Success)
        val matched = (result as AppResult.Success).data
        assertEquals(1, matched.size)
        assertEquals("id_1", matched[0].id)
    }
}
