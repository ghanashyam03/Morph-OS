package com.morphos.app.core.ai

sealed interface InferenceResult {
    data class Success(val text: String) : InferenceResult
    data class EmbeddingSuccess(val vector: FloatArray) : InferenceResult
    data class Error(val error: Throwable) : InferenceResult
}
