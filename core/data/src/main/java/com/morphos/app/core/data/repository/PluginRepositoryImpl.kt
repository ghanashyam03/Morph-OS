package com.morphos.app.core.data.repository

import com.morphos.app.core.domain.model.PluginData
import com.morphos.app.core.domain.repository.PluginRepository
import javax.inject.Inject

class PluginRepositoryImpl @Inject constructor() : PluginRepository {
    override suspend fun getCachedPluginData(pluginId: String, widgetId: String): PluginData? {
        TODO("Not yet implemented")
    }

    override suspend fun cachePluginData(
        pluginId: String,
        widgetId: String,
        data: PluginData,
        ttlSeconds: Int
    ) {
        TODO("Not yet implemented")
    }
}
