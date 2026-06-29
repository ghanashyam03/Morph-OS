package com.morphos.app.core.ai

sealed interface InferenceRequest {
    data class TextCompletion(val prompt: String, val temperature: Float) : InferenceRequest
    data class Embeddings(val text: String) : InferenceRequest
}
