package com.morphos.app.core.domain.repository

import com.morphos.app.core.common.AppResult
import com.morphos.app.core.domain.model.EmbeddingEntry

interface EmbeddingRepository {
    suspend fun saveEmbedding(entry: EmbeddingEntry): AppResult<Unit>
    suspend fun findSimilar(queryEmbedding: FloatArray, topK: Int = 5): List<EmbeddingEntry>
    suspend fun deleteEmbedding(id: String): AppResult<Unit>
    suspend fun getAllEmbeddings(): List<EmbeddingEntry>
}
