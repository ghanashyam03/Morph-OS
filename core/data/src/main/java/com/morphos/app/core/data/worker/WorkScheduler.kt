package com.morphos.app.core.data.worker

import android.content.Context
import android.os.PowerManager
import androidx.work.*
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun scheduleAll() {
        val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val powerSaveMode = pm?.isPowerSaveMode ?: false
        scheduleAll(powerSaveMode)
    }

    fun scheduleAll(powerSaveMode: Boolean) {
        val workManager = WorkManager.getInstance(context)

        // Adjust refresh intervals based on power save mode
        val contextRefreshInterval = if (powerSaveMode) 60L else 30L
        val prefetchInterval = if (powerSaveMode) 120L else 60L

        // 1. Context Refresh Worker
        val contextRefreshRequest = PeriodicWorkRequestBuilder<ContextRefreshWorker>(
            contextRefreshInterval, TimeUnit.MINUTES
        )
        .setConstraints(Constraints.Builder().build())
        .build()

        workManager.enqueueUniquePeriodicWork(
            "ContextRefreshWork",
            ExistingPeriodicWorkPolicy.UPDATE,
            contextRefreshRequest
        )

        // 2. Memory Summarization Worker (Every 24 hours, charging only)
        val summarizationRequest = PeriodicWorkRequestBuilder<MemorySummarizationWorker>(
            24, TimeUnit.HOURS
        )
        .setConstraints(
            Constraints.Builder()
                .setRequiresCharging(true)
                .build()
        )
        .build()

        workManager.enqueueUniquePeriodicWork(
            "MemorySummarizationWork",
            ExistingPeriodicWorkPolicy.KEEP,
            summarizationRequest
        )

        // 3. Embedding Index Worker (Every 24 hours, charging only)
        val embeddingIndexRequest = PeriodicWorkRequestBuilder<EmbeddingIndexWorker>(
            24, TimeUnit.HOURS
        )
        .setConstraints(
            Constraints.Builder()
                .setRequiresCharging(true)
                .build()
        )
        .build()

        workManager.enqueueUniquePeriodicWork(
            "EmbeddingIndexWork",
            ExistingPeriodicWorkPolicy.KEEP,
            embeddingIndexRequest
        )

        // 4. Widget Data Prefetch Worker
        val prefetchRequest = PeriodicWorkRequestBuilder<WidgetDataPrefetchWorker>(
            prefetchInterval, TimeUnit.MINUTES
        )
        .setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        )
        .build()

        workManager.enqueueUniquePeriodicWork(
            "WidgetDataPrefetchWork",
            ExistingPeriodicWorkPolicy.UPDATE,
            prefetchRequest
        )

        // 5. Old Memory Prune Worker (Every 24 hours, charging only)
        val pruneRequest = PeriodicWorkRequestBuilder<OldMemoryPruneWorker>(
            24, TimeUnit.HOURS
        )
        .setConstraints(
            Constraints.Builder()
                .setRequiresCharging(true)
                .build()
        )
        .build()

        workManager.enqueueUniquePeriodicWork(
            "OldMemoryPruneWork",
            ExistingPeriodicWorkPolicy.KEEP,
            pruneRequest
        )
    }
}
