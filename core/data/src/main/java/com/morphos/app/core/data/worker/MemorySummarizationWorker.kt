package com.morphos.app.core.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.morphos.app.core.common.AppResult
import com.morphos.app.core.domain.agent.AgentOrchestrator
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.withTimeout

@HiltWorker
class MemorySummarizationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val agentOrchestrator: AgentOrchestrator
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            withTimeout(30_000L) {
                val result = agentOrchestrator.triggerMemorySummarization()
                if (result is AppResult.Success) {
                    Result.success()
                } else {
                    Result.failure()
                }
            }
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
