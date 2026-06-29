package com.morphos.app.core.domain.repository

import com.morphos.app.core.domain.model.PluginData
import kotlinx.coroutines.flow.Flow

interface PluginRepository {
    suspend fun getCachedPluginData(pluginId: String, widgetId: String): PluginData?
    suspend fun cachePluginData(pluginId: String, widgetId: String, data: PluginData, ttlSeconds: Int)
}
