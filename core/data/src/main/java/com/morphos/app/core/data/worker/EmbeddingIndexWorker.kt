package com.morphos.app.core.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.morphos.app.core.ai.AIEngineManager
import com.morphos.app.core.common.AppResult
import com.morphos.app.core.domain.model.EmbeddingEntry
import com.morphos.app.core.domain.repository.EmbeddingRepository
import com.morphos.app.core.domain.repository.MemoryRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.UUID

@HiltWorker
class EmbeddingIndexWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val memoryRepository: MemoryRepository,
    private val embeddingRepository: EmbeddingRepository,
    private val aiEngineManager: AIEngineManager
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            if (!aiEngineManager.isTier0Available()) {
                return Result.retry()
            }

            val memories = memoryRepository.getLongTermMemories().first()
            val unembedded = memories.filter { it.embeddingId == null }

            for (memory in unembedded) {
                val embeddingArray = aiEngineManager.computeEmbedding(memory.content)
                val embeddingEntry = EmbeddingEntry(
                    id = UUID.randomUUID().toString(),
                    sourceText = memory.content,
                    embedding = embeddingArray,
                    createdAtEpochMilli = System.currentTimeMillis()
                )

                embeddingRepository.saveEmbedding(embeddingEntry)
                memoryRepository.saveLongTermMemory(memory.copy(embeddingId = embeddingEntry.id))
            }

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
