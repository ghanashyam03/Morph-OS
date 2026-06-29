package com.morphos.app.core.data.repository

import com.morphos.app.core.domain.model.EmbeddingEntry
import com.morphos.app.core.domain.repository.EmbeddingRepository
import javax.inject.Inject

class EmbeddingRepositoryImpl @Inject constructor() : EmbeddingRepository {
    override suspend fun saveEmbedding(entry: EmbeddingEntry) {
        TODO("Not yet implemented")
    }

    override suspend fun findSimilar(embedding: FloatArray, limit: Int): List<EmbeddingEntry> {
        TODO("Not yet implemented")
    }

    override suspend fun clearEmbeddings() {
        TODO("Not yet implemented")
    }
}
