package com.morphos.app.core.ai

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIEngineManager @Inject constructor(
    private val llamaCppEngine: LlamaCppEngine,
    private val onnxEmbeddingEngine: OnnxEmbeddingEngine,
    private val cloudAIEngine: CloudAIEngine
) {

    fun isTier0Available(): Boolean {
        return llamaCppEngine.isTier0Loaded()
    }

    fun isTier1Available(): Boolean {
        return llamaCppEngine.isTier1Loaded()
    }

    suspend fun inferTier0(prompt: String): String {
        return llamaCppEngine.inferTier0(prompt)
    }

    suspend fun inferTier1(prompt: String): String {
        return llamaCppEngine.inferTier1(prompt)
    }

    suspend fun inferLocalPlan(prompt: String): String = llamaCppEngine.inferPlanning(prompt)

    suspend fun inferTier2(prompt: String): String {
        return cloudAIEngine.inferTier2(prompt)
    }

    fun computeEmbedding(text: String): FloatArray {
        return onnxEmbeddingEngine.computeEmbedding(text)
    }
}
