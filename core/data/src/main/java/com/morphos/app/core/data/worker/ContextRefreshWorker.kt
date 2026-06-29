package com.morphos.app.core.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.morphos.app.core.common.AppResult
import com.morphos.app.core.domain.agent.ContextAgent
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ContextRefreshWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val contextAgent: ContextAgent
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val result = contextAgent.refresh()
            if (result is AppResult.Error) {
                if (runAttemptCount < 2) Result.retry() else Result.failure()
            } else {
                Result.success()
            }
        } catch (e: Exception) {
            if (runAttemptCount < 2) Result.retry() else Result.failure()
        }
    }
}
