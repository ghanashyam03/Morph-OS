package com.morphos.app.core.ai

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnnxEmbeddingEngine @Inject constructor() {
    suspend fun getEmbedding(request: InferenceRequest.Embeddings): InferenceResult {
        TODO("Not yet implemented")
    }
}
