package com.morphos.app.core.domain.repository

import com.morphos.app.core.domain.model.EmbeddingEntry

interface EmbeddingRepository {
    suspend fun saveEmbedding(entry: EmbeddingEntry)
    suspend fun findSimilar(embedding: FloatArray, limit: Int): List<EmbeddingEntry>
    suspend fun clearEmbeddings()
}
