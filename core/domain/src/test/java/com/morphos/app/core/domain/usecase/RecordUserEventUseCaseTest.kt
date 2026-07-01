package com.morphos.app.core.domain.usecase.widget

import com.morphos.app.core.common.AppResult
import com.morphos.app.core.domain.agent.MemoryAgent
import com.morphos.app.core.domain.model.ShortTermEvent
import com.morphos.app.core.domain.model.ShortTermEventType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockKExtension::class)
class RecordUserEventUseCaseTest {

    @MockK
    lateinit var memoryAgent: MemoryAgent

    private lateinit var recordUserEventUseCase: RecordUserEventUseCase

    @BeforeEach
    fun setUp() {
        recordUserEventUseCase = RecordUserEventUseCase(memoryAgent)
    }

    @Test
    fun givenEvent_whenRecorded_thenDelegatesToMemoryAgent() = runTest {
        val event = ShortTermEvent("id", ShortTermEventType.WIDGET_TAPPED, "widget_1", System.currentTimeMillis())

        coEvery { memoryAgent.recordEvent(any()) } returns AppResult.Success(Unit)

        val result = recordUserEventUseCase(event)

        assertTrue(result is AppResult.Success)
        coVerify(exactly = 1) { memoryAgent.recordEvent(event) }
    }
}
