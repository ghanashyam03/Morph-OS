package com.morphos.app.core.data.repository

import com.morphos.app.core.common.AppDispatchers
import com.morphos.app.core.common.AppResult
import com.morphos.app.core.data.db.PluginDataCacheDao
import com.morphos.app.core.data.db.PluginDataCacheEntity
import com.morphos.app.core.domain.model.PluginData
import com.morphos.app.core.domain.repository.DataPlugin
import com.morphos.app.core.domain.repository.PluginRepository
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PluginRepositoryImpl @Inject constructor(
    private val plugins: Set<@JvmSuppressWildcards DataPlugin>,
    private val pluginDataCacheDao: PluginDataCacheDao,
    private val dispatchers: AppDispatchers
) : PluginRepository {

    override fun getAvailablePlugins(): List<DataPlugin> {
        return plugins.toList()
    }

    override suspend fun fetchPluginData(pluginId: String, config: Map<String, String>): AppResult<PluginData> = withContext(dispatchers.io) {
        val plugin = plugins.find { it.pluginId == pluginId }
            ?: return@withContext AppResult.Error(IllegalArgumentException("Plugin $pluginId not found"))
        plugin.fetch(config)
    }

    override suspend fun getCachedPluginData(dataSourceId: String): PluginData? = withContext(dispatchers.io) {
        try {
            val entity = pluginDataCacheDao.getBySourceId(dataSourceId) ?: return@withContext null
            val now = System.currentTimeMillis()
            val isStale = entity.fetchedAt + (entity.ttlSeconds * 1000L) < now
            PluginData(
                pluginId = entity.pluginId,
                dataSourceId = entity.dataSourceId,
                rawValue = entity.rawValue,
                fetchedAt = entity.fetchedAt,
                isStale = isStale
            )
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun cachePluginData(data: PluginData, ttlSeconds: Int): AppResult<Unit> = withContext(dispatchers.io) {
        try {
            val entity = PluginDataCacheEntity(
                dataSourceId = data.dataSourceId,
                pluginId = data.pluginId,
                rawValue = data.rawValue,
                fetchedAt = data.fetchedAt,
                ttlSeconds = ttlSeconds
            )
            pluginDataCacheDao.insert(entity)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e)
        }
    }

    override suspend fun invalidateCache(dataSourceId: String): AppResult<Unit> = withContext(dispatchers.io) {
        try {
            pluginDataCacheDao.invalidate(dataSourceId)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e)
        }
    }
}
