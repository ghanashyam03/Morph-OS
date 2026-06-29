package com.morphos.app.core.ai

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LlamaCppEngine @Inject constructor() {
    suspend fun runInference(request: InferenceRequest.TextCompletion): InferenceResult {
        TODO("Not yet implemented")
    }
}
