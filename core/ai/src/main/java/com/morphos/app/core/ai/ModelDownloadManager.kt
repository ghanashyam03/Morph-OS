package com.morphos.app.core.ai

import android.content.Context
import androidx.work.*
import com.morphos.app.core.data.worker.ModelDownloadWorker
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

        val request = OneTimeWorkRequestBuilder<ModelDownloadWorker>()
            .setInputData(data)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.UNMETERED)
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
