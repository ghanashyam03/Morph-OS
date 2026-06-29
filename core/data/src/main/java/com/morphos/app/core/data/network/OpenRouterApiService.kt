package com.morphos.app.core.data.network

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenRouterApiService {
    @POST("api/v1/chat/completions")
    suspend fun completion(
        @Header("Authorization") auth: String,
        @Body request: OpenRouterRequest
    ): OpenRouterResponse
}

@Serializable
data class OpenRouterRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val max_tokens: Int,
    val temperature: Float,
    val stream: Boolean = false
)

@Serializable
data class ChatMessage(
    val role: String,
    val content: String
)

@Serializable
data class OpenRouterResponse(
    val choices: List<Choice>
)

@Serializable
data class Choice(
    val message: ChatMessage
)
