package com.morphos.app.core.data.network

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenRouterApiService {
    @POST("api/v1/chat/completions")
    suspend fun getCompletion(
        @Header("Authorization") authorization: String,
        @Body requestBody: String
    ): String
}
