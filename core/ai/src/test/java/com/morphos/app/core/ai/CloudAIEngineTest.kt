package com.morphos.app.core.ai

import com.morphos.app.core.data.network.ChatMessage
import com.morphos.app.core.data.network.Choice
import com.morphos.app.core.data.network.OpenRouterApiService
import com.morphos.app.core.data.network.OpenRouterResponse
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
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockKExtension::class)
class CloudAIEngineTest {

    @MockK
    lateinit var openRouterApiService: OpenRouterApiService

    private lateinit var cloudAIEngine: CloudAIEngine

    @BeforeEach
    fun setUp() {
        cloudAIEngine = CloudAIEngine(openRouterApiService)
    }

    @Test
    fun cloudDisabled_complete_returnsError() = runTest {
        // If API key is empty or service throws
        coEvery { openRouterApiService.completion(any(), any()) } throws IllegalStateException("API key missing")

        val result = cloudAIEngine.inferTier2("prompt")

        assertEquals("", result)
    }

    @Test
    fun rateLimitExceeded_complete_returnsError() = runTest {
        coEvery { openRouterApiService.completion(any(), any()) } throws IOException("HTTP 429 Rate Limit Exceeded")

        val result = cloudAIEngine.inferTier2("prompt")

        assertEquals("", result)
    }

    @Test
    fun validRequest_returnsContent() = runTest {
        val mockResponse = OpenRouterResponse(
            choices = listOf(Choice(message = ChatMessage("assistant", "AI generated content")))
        )
        coEvery { openRouterApiService.completion(any(), any()) } returns mockResponse

        val result = cloudAIEngine.inferTier2("prompt")

        assertEquals("AI generated content", result)
    }

    @Test
    fun networkError_returnsError() = runTest {
        coEvery { openRouterApiService.completion(any(), any()) } throws IOException("No internet connection")

        val result = cloudAIEngine.inferTier2("prompt")

        assertEquals("", result)
    }
}
