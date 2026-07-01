package com.morphos.app.core.domain.model

data class EmbeddingEntry(
    val id: String,
    val sourceText: String,
    val embedding: FloatArray,
    val createdAtEpochMilli: Long
)
