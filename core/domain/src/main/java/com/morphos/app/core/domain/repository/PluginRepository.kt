package com.morphos.app.core.domain.repository

import com.morphos.app.core.common.AppResult
import com.morphos.app.core.domain.model.PluginData

interface PluginRepository {
    fun getAvailablePlugins(): List<DataPlugin>
    suspend fun fetchPluginData(pluginId: String, config: Map<String, String>): AppResult<PluginData>
    suspend fun getCachedPluginData(dataSourceId: String): PluginData?
    suspend fun cachePluginData(data: PluginData, ttlSeconds: Int): AppResult<Unit>
    suspend fun invalidateCache(dataSourceId: String): AppResult<Unit>
}
