package com.morphos.app.core.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.morphos.app.core.common.AppResult
import com.morphos.app.core.domain.repository.PluginRepository
import com.morphos.app.core.domain.repository.WidgetRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class WidgetDataPrefetchWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val widgetRepository: WidgetRepository,
    private val pluginRepository: PluginRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val widgets = widgetRepository.getAllWidgets().first()
            for (widget in widgets) {
                for (binding in widget.dataBindings) {
                    val result = pluginRepository.fetchPluginData(binding.pluginId, binding.pluginConfig)
                    if (result is AppResult.Success) {
                        pluginRepository.cachePluginData(result.data, binding.cacheTtlSeconds)
                    }
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
