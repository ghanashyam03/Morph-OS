package com.morphos.app.core.domain.usecase.widget

import com.morphos.app.core.common.AppResult
import com.morphos.app.core.domain.agent.IntentAgent
import com.morphos.app.core.domain.model.ContextSnapshot
import com.morphos.app.core.domain.model.IntentType
import com.morphos.app.core.domain.model.WidgetIntent
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockKExtension::class)
class ParseUserIntentUseCaseTest {

    @MockK
    lateinit var intentAgent: IntentAgent

    private lateinit var parseUserIntentUseCase: ParseUserIntentUseCase
    private val dummyContext = ContextSnapshot(0, 0L, 80, "WiFi", 0.0, 0.0, false)

    @BeforeEach
    fun setUp() {
        parseUserIntentUseCase = ParseUserIntentUseCase(intentAgent)
    }

    @Test
    fun givenBlankInput_whenParsed_thenReturnsError() = runTest {
        val params = ParseIntentParams("", dummyContext)
        val result = parseUserIntentUseCase(params)

        assertTrue(result is AppResult.Error)
        assertTrue((result as AppResult.Error).message?.contains("cannot be blank") == true)
    }

    @Test
    fun givenStudyKeyword_whenParsed_thenDelegatesToAgent() = runTest {
        val params = ParseIntentParams("study routine", dummyContext)
        val expectedIntent = WidgetIntent(IntentType.STUDY, 0.9f, true)

        coEvery { intentAgent.parseIntent(any(), any()) } returns AppResult.Success(expectedIntent)

        val result = parseUserIntentUseCase(params)

        assertTrue(result is AppResult.Success)
        assertEquals(expectedIntent, (result as AppResult.Success).data)
    }

    @Test
    fun givenAgentFailure_whenParsed_thenReturnsError() = runTest {
        val params = ParseIntentParams("study routine", dummyContext)
        val errorException = Exception("Inference Error")

        coEvery { intentAgent.parseIntent(any(), any()) } returns AppResult.Error(errorException)

        val result = parseUserIntentUseCase(params)

        assertTrue(result is AppResult.Error)
        assertEquals("Inference Error", (result as AppResult.Error).message)
    }
}
