package com.morphos.app.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class LongTermMemory(
    val id: String,
    val content: String,                        // prose summary
    val embeddingId: String?,                   // FK to embedding_store
    val memoryType: MemoryType,
    val confidence: Float = 1.0f,
    val createdAt: Long,
    val expiresAt: Long? = null
)

enum class MemoryType { BEHAVIORAL_PATTERN, EXPLICIT_PREFERENCE, WIDGET_PERFORMANCE, INFERRED_PREFERENCE }
