package com.morphos.app.core.ai

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIEngineManager @Inject constructor(
    private val llamaCppEngine: LlamaCppEngine,
    private val onnxEmbeddingEngine: OnnxEmbeddingEngine,
    private val cloudAIEngine: CloudAIEngine
) {
    suspend fun processInference(request: InferenceRequest): InferenceResult {
        TODO("Not yet implemented")
    }
}
