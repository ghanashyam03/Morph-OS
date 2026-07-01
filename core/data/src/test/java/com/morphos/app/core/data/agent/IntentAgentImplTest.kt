package com.morphos.app.core.data.agent

import com.morphos.app.core.ai.AIEngineManager
import com.morphos.app.core.common.AppDispatchers
import com.morphos.app.core.common.AppResult
import com.morphos.app.core.domain.model.ContextSnapshot
import com.morphos.app.core.domain.model.IntentType
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockKExtension::class)
class IntentAgentImplTest {

    @MockK
    lateinit var aiEngineManager: AIEngineManager

    private lateinit var agent: IntentAgentImpl
    private val dummyContext = ContextSnapshot(0, 0L, 80, "WiFi", 0.0, 0.0, false)
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testDispatchers = AppDispatchers(
        main = testDispatcher,
        io = testDispatcher,
        default = testDispatcher,
        unconfined = testDispatcher
    )

    @BeforeEach
    fun setUp() {
        agent = IntentAgentImpl(aiEngineManager, testDispatchers)
    }

    @Test
    fun studyKeyword_returnsStudyIntent() = runTest {
        val result = agent.parseIntent("study routine details", dummyContext)

        assertTrue(result is AppResult.Success)
        val data = (result as AppResult.Success).data
        assertEquals(IntentType.CREATE_STUDY_WIDGET, data.intentType)
    }

    @Test
    fun travelKeyword_returnsTravelIntent() = runTest {
        val result = agent.parseIntent("my new travel plans", dummyContext)

        assertTrue(result is AppResult.Success)
        val data = (result as AppResult.Success).data
        assertEquals(IntentType.CREATE_TRAVEL_WIDGET, data.intentType)
    }

    @Test
    fun unknownInput_returnsUnknownWithCloudFallbackTrue() = runTest {
        every { aiEngineManager.isTier0Available() } returns false

        val result = agent.parseIntent("arbitrary weird text input", dummyContext)

        assertTrue(result is AppResult.Success)
        val data = (result as AppResult.Success).data
        assertEquals(IntentType.UNKNOWN, data.intentType)
        assertTrue(data.requiresCloudFallback)
    }

    @Test
    fun blankInput_isHandledGracefully() = runTest {
        every { aiEngineManager.isTier0Available() } returns false

        val result = agent.parseIntent("", dummyContext)

        assertTrue(result is AppResult.Success)
        val data = (result as AppResult.Success).data
        assertEquals(IntentType.UNKNOWN, data.intentType)
    }

    @Test
    fun aiAvailable_usesAIClassification() = runTest {
        every { aiEngineManager.isTier0Available() } returns true
        coEvery { aiEngineManager.inferTier0(any()) } returns "CREATE_FINANCE_WIDGET"

        // Input doesn't contain simple finance keyword matching
        val result = agent.parseIntent("arbitrary input that AI resolves", dummyContext)

        assertTrue(result is AppResult.Success)
        val data = (result as AppResult.Success).data
        assertEquals(IntentType.CREATE_FINANCE_WIDGET, data.intentType)
    }

    @Test
    fun aiUnavailable_usesKeywordFallback() = runTest {
        every { aiEngineManager.isTier0Available() } returns false

        val result = agent.parseIntent("help me gym tomorrow", dummyContext)

        assertTrue(result is AppResult.Success)
        val data = (result as AppResult.Success).data
        assertEquals(IntentType.CREATE_FITNESS_WIDGET, data.intentType)
    }
}
