package com.morphos.app.core.ai

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudAIEngine @Inject constructor() {
    suspend fun runCompletion(request: InferenceRequest.TextCompletion): InferenceResult {
        TODO("Not yet implemented")
    }
}
