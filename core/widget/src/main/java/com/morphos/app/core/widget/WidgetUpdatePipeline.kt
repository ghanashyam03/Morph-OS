package com.morphos.app.core.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import com.morphos.app.core.common.AppDispatchers
import com.morphos.app.core.domain.model.PluginData
import com.morphos.app.core.domain.model.SlotConfig
import com.morphos.app.core.domain.repository.PluginRepository
import com.morphos.app.core.domain.repository.WidgetRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetUpdatePipeline @Inject constructor(
    private val widgetRepository: WidgetRepository,
    private val pluginRepository: PluginRepository,
    @ApplicationContext private val context: Context,
    private val dispatchers: AppDispatchers
) {
    // Call this to update ONE widget's Glance state
    suspend fun updateWidget(widgetId: String) {
        withContext(dispatchers.io) {
            val config = widgetRepository.getWidgetById(widgetId) ?: return@withContext
            
            // 1. Fetch data for each slot
            val resolvedSlots = mutableMapOf<String, String>()
            config.dataBindings.forEach { binding ->
                val cachedData = pluginRepository.getCachedPluginData(binding.dataSourceId)
                if (cachedData != null && !cachedData.isStale) {
                    resolveSlots(cachedData, config.slots, resolvedSlots)
                } else {
                    val freshData = pluginRepository.fetchPluginData(binding.pluginId, binding.pluginConfig)
                    freshData.onSuccess { data ->
                        pluginRepository.cachePluginData(data, binding.cacheTtlSeconds)
                        resolveSlots(data, config.slots, resolvedSlots)
                    }
                }
            }
            
            // 2. Apply fallback values for any unresolved slots
            config.slots.forEach { (slotId, slotConfig) ->
                if (!resolvedSlots.containsKey(slotId)) {
                    resolvedSlots[slotId] = slotConfig.fallbackValue
                }
            }
            
            // 3. Build new GlanceState
            val newState = MorphOsWidgetState(
                widgetId = widgetId,
                widgetName = config.name,
                templateId = config.templateId,
                resolvedSlots = resolvedSlots,
                isLoading = false,
                lastUpdated = System.currentTimeMillis()
            )
            
            // 4. Update Glance state for this widget
            val glanceId = GlanceAppWidgetManager(context)
                .getGlanceIds(MorphOsGlanceWidget::class.java)
                .firstOrNull { it.hashCode().toString() == widgetId }
            
            if (glanceId != null) {
                updateAppWidgetState(context, MorphOsWidgetStateDefinition, glanceId) { newState }
                MorphOsGlanceWidget().update(context, glanceId)
            }
        }
    }

    // Call this to update ALL pinned widgets
    suspend fun updateAllWidgets() {
        withContext(dispatchers.io) {
            widgetRepository.getAllWidgets().first().forEach { config ->
                updateWidget(config.id)
            }
        }
    }

    private fun resolveSlots(
        data: PluginData,
        slotConfigs: Map<String, SlotConfig>,
        out: MutableMap<String, String>
    ) {
        try {
            val jsonElement = Json.parseToJsonElement(data.rawValue)
            val jsonObject = jsonElement.jsonObject
            slotConfigs.forEach { (slotId, slotConfig) ->
                if (slotConfig.dataSourceId == data.dataSourceId) {
                    val expr = slotConfig.transformExpression ?: return@forEach
                    val value = jsonObject[expr]?.jsonPrimitive?.content ?: slotConfig.fallbackValue
                    val truncated = value.take(slotConfig.maxLength)
                    out[slotId] = truncated
                }
            }
        } catch (e: Exception) {
            // Leave slots unresolved - fallback values will be applied
        }
    }
}
