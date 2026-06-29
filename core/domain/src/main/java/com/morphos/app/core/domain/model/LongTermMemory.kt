package com.morphos.app.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class LongTermMemory(
    val id: String,
    val content: String,
    val embeddingId: String,
    val timestamp: Long
)
