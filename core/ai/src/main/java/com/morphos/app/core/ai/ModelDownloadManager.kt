package com.morphos.app.core.ai

import android.content.Context
import androidx.work.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModelDownloadManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun downloadModel(config: ModelConfig): Flow<Float> {
        val workManager = WorkManager.getInstance(context)

        val data = workDataOf(
            "model_filename" to config.filename,
            "model_url" to config.url,
            "sha256" to config.sha256
        )

        @Suppress("UNCHECKED_CAST")
        val workerClass = Class.forName("com.morphos.app.core.data.worker.ModelDownloadWorker") as Class<out ListenableWorker>
        
        val request = OneTimeWorkRequest.Builder(workerClass)
            .setInputData(data)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        workManager.enqueueUniqueWork(
            "Download_${config.name}",
            ExistingWorkPolicy.KEEP,
            request
        )

        return workManager.getWorkInfoByIdFlow(request.id).map { workInfo ->
            if (workInfo == null) return@map 0f
            when (workInfo.state) {
                WorkInfo.State.SUCCEEDED -> 1f
                WorkInfo.State.RUNNING -> {
                    val progress = workInfo.progress.getInt("progress", 0)
                    progress / 100f
                }
                WorkInfo.State.FAILED -> -1f
                else -> 0f
            }
        }
    }
}
