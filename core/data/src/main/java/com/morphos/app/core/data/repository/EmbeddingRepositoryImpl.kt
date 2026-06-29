package com.morphos.app.core.data.repository

import com.morphos.app.core.common.AppDispatchers
import com.morphos.app.core.common.AppResult
import com.morphos.app.core.data.db.EmbeddingEntryDao
import com.morphos.app.core.data.mapper.toDomain
import com.morphos.app.core.data.mapper.toEntity
import com.morphos.app.core.domain.model.EmbeddingEntry
import com.morphos.app.core.domain.repository.EmbeddingRepository
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.sqrt

class EmbeddingRepositoryImpl @Inject constructor(
    private val embeddingEntryDao: EmbeddingEntryDao,
    private val dispatchers: AppDispatchers
) : EmbeddingRepository {

    override suspend fun saveEmbedding(entry: EmbeddingEntry): AppResult<Unit> = withContext(dispatchers.io) {
        try {
            embeddingEntryDao.insert(entry.toEntity())
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e)
        }
    }

    override suspend fun findSimilar(queryEmbedding: FloatArray, topK: Int): List<EmbeddingEntry> = withContext(dispatchers.io) {
        try {
            val allEntries = embeddingEntryDao.getAll().map { it.toDomain() }
            if (allEntries.isEmpty()) return@withContext emptyList()

            val queryMag = magnitude(queryEmbedding)
            if (queryMag == 0f) return@withContext emptyList()

            allEntries
                .map { entry ->
                    val entryMag = magnitude(entry.embedding)
                    val sim = if (entryMag > 0f) {
                        dot(queryEmbedding, entry.embedding) / (queryMag * entryMag)
                    } else {
                        0f
                    }
                    entry to sim
                }
                .sortedByDescending { it.second }
                .take(topK)
                .map { it.first }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun deleteEmbedding(id: String): AppResult<Unit> = withContext(dispatchers.io) {
        try {
            embeddingEntryDao.deleteById(id)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e)
        }
    }

    override suspend fun getAllEmbeddings(): List<EmbeddingEntry> = withContext(dispatchers.io) {
        try {
            embeddingEntryDao.getAll().map { it.toDomain() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun dot(a: FloatArray, b: FloatArray): Float {
        var sum = 0f
        val len = minOf(a.size, b.size)
        for (i in 0 until len) {
            sum += a[i] * b[i]
        }
        return sum
    }

    private fun magnitude(a: FloatArray): Float {
        var sum = 0f
        for (v in a) {
            sum += v * v
        }
        return sqrt(sum)
    }
}
