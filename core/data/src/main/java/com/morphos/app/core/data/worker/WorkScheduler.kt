package com.morphos.app.core.data.worker

import android.content.Context
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
        val workManager = WorkManager.getInstance(context)

        // 1. Context Refresh Worker (Every 30 minutes)
        val contextRefreshRequest = PeriodicWorkRequestBuilder<ContextRefreshWorker>(
            30, TimeUnit.MINUTES
        )
        .setConstraints(Constraints.Builder().build())
        .build()

        workManager.enqueueUniquePeriodicWork(
            "ContextRefreshWork",
            ExistingPeriodicWorkPolicy.KEEP,
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

        // 4. Widget Data Prefetch Worker (Every 1 hour, connected only)
        val prefetchRequest = PeriodicWorkRequestBuilder<WidgetDataPrefetchWorker>(
            1, TimeUnit.HOURS
        )
        .setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        )
        .build()

        workManager.enqueueUniquePeriodicWork(
            "WidgetDataPrefetchWork",
            ExistingPeriodicWorkPolicy.KEEP,
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
