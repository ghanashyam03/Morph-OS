package com.morphos.app.core.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.morphos.app.core.common.AppResult
import com.morphos.app.core.domain.agent.ContextAgent
import com.morphos.app.core.domain.repository.PluginRepository
import com.morphos.app.core.domain.repository.WidgetRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout

@HiltWorker
class WidgetDataPrefetchWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val widgetRepository: WidgetRepository,
    private val pluginRepository: PluginRepository,
    private val contextAgent: ContextAgent
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            withTimeout(30_000L) {
                val snapshot = contextAgent.contextFlow.value
                if (snapshot.batteryLevel < 10) {
                    // battery < 10%: skip all background work, return Result.success() immediately
                    return@withTimeout Result.success()
                }

                val widgets = widgetRepository.getAllWidgets().first()
                for (widget in widgets) {
                    // If battery < 20%: update only clock-type widgets, skip others
                    if (snapshot.batteryLevel < 20) {
                        val hasClockBinding = widget.dataBindings.any {
                            it.pluginId == "clock" || it.pluginId == "countdown"
                        }
                        if (!hasClockBinding) continue
                    }

                    for (binding in widget.dataBindings) {
                        // Skip network plugins when battery is low (< 20%)
                        if (snapshot.batteryLevel < 20 && (binding.pluginId == "weather" || binding.pluginId == "news_rss")) {
                            continue
                        }

                        val result = pluginRepository.fetchPluginData(binding.pluginId, binding.pluginConfig)
                        if (result is AppResult.Success) {
                            pluginRepository.cachePluginData(result.data, binding.cacheTtlSeconds)
                        }
                    }
                }
                Result.success()
            }
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
