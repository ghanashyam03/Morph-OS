package com.morphos.app.core.ai

import android.content.Context
import androidx.work.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModelDownloadManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun downloadModel(config: ModelConfig): Flow<Float> {
        val workManager = WorkManager.getInstance(context)

        val data = androidx.work.workDataOf(
            "model_filename" to config.filename,
            "model_url" to config.url,
            "sha256" to config.sha256,
            "size_bytes" to config.sizeBytes
        )

        return try {
            @Suppress("UNCHECKED_CAST")
            val workerClass = Class.forName("com.morphos.app.core.data.worker.ModelDownloadWorker") as Class<out ListenableWorker>
            
            val request = OneTimeWorkRequest.Builder(workerClass)
                .setInputData(data)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    15,  // 15 seconds initial backoff
                    TimeUnit.SECONDS
                )
                .build()

            workManager.enqueueUniqueWork(
                "Download_${config.name}",
                ExistingWorkPolicy.REPLACE,  // Changed from KEEP to REPLACE to allow retries
                request
            )

            Timber.d("ModelDownloadManager: Enqueued download for ${config.name}")

            workManager.getWorkInfoByIdFlow(request.id)
                .distinctUntilChanged()
                .map { workInfo ->
                    if (workInfo == null) {
                        Timber.d("ModelDownloadManager: WorkInfo is null")
                        return@map 0f
                    }
                    
                    Timber.d("ModelDownloadManager: State=${workInfo.state}, Progress=${workInfo.progress.getInt("progress", 0)}")
                    
                    when (workInfo.state) {
                        WorkInfo.State.SUCCEEDED -> {
                            Timber.d("ModelDownloadManager: Download succeeded")
                            1f
                        }
                        WorkInfo.State.RUNNING -> {
                            val progress = workInfo.progress.getInt("progress", 0)
                            (progress / 100f).coerceIn(0f, 1f)
                        }
                        WorkInfo.State.FAILED -> {
                            Timber.e("ModelDownloadManager: Download failed - ${workInfo.outputData.getString("error")}")
                            -1f
                        }
                        WorkInfo.State.ENQUEUED, WorkInfo.State.BLOCKED -> {
                            Timber.d("ModelDownloadManager: Work enqueued/blocked")
                            0.01f  // Signal that work is queued but not started
                        }
                        else -> 0f
                    }
                }
        } catch (e: Exception) {
            Timber.e(e, "ModelDownloadManager: Failed to initialize worker")
            kotlinx.coroutines.flow.flowOf(-1f)
        }
    }
}
