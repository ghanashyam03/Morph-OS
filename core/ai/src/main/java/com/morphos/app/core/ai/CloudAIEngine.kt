package com.morphos.app.core.ai

import com.morphos.app.core.ai.BuildConfig
import com.morphos.app.core.ai.network.ChatMessage
import com.morphos.app.core.ai.network.OpenRouterApiService
import com.morphos.app.core.ai.network.OpenRouterRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudAIEngine @Inject constructor(
    private val openRouterApiService: OpenRouterApiService
) {
    suspend fun inferTier2(prompt: String): String {
        val apiKey = try {
            BuildConfig.OPENROUTER_API_KEY
        } catch (e: Exception) {
            "dummy_key"
        }
        val authHeader = "Bearer $apiKey"
        val request = OpenRouterRequest(
            model = "meta-llama/llama-3-8b-instruct:free",
            messages = listOf(ChatMessage(role = "user", content = prompt)),
            max_tokens = 512,
            temperature = 0.5f
        )
        return try {
            val response = openRouterApiService.completion(authHeader, request)
            response.choices.firstOrNull()?.message?.content ?: ""
        } catch (e: Exception) {
            ""
        }
    }
}
