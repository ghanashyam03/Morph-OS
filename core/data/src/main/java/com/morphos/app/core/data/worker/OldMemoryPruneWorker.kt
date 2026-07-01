package com.morphos.app.core.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.morphos.app.core.data.db.*
import com.morphos.app.core.domain.repository.SettingsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.withTimeout

@HiltWorker
class OldMemoryPruneWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val settingsRepository: SettingsRepository,
    private val shortTermEventDao: ShortTermEventDao,
    private val longTermMemoryDao: LongTermMemoryDao,
    private val notificationLogDao: NotificationLogDao,
    private val agentTaskDao: AgentTaskDao
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            withTimeout(30_000L) {
                val retentionDays = settingsRepository.getRetentionDays()
                val cutoff = System.currentTimeMillis() - (retentionDays * 86400000L)

                shortTermEventDao.deleteOlderThan(cutoff)
                longTermMemoryDao.pruneOld(cutoff)
                notificationLogDao.pruneOld(cutoff)
                agentTaskDao.pruneCompleted(cutoff)

                Result.success()
            }
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
