package com.morphos.app.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class EmbeddingEntry(
    val id: String,
    val sourceText: String,
    val embedding: FloatArray,
    val createdAt: Long
) {
    override fun equals(other: Any?) = other is EmbeddingEntry && id == other.id
    override fun hashCode() = id.hashCode()
}
