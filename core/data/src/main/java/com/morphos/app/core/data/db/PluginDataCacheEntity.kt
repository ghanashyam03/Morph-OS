package com.morphos.app.core.data.db

import androidx.room.Entity
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.OnConflictStrategy

@Entity(tableName = "plugin_data_cache", primaryKeys = ["pluginId", "widgetId"])
data class PluginDataCacheEntity(
    val pluginId: String,
    val widgetId: String,
    val dataJson: String,
    val cachedAt: Long,
    val ttlSeconds: Int
)

@Dao
interface PluginDataCacheDao {
    @Query("SELECT * FROM plugin_data_cache WHERE pluginId = :pluginId AND widgetId = :widgetId")
    suspend fun getCache(pluginId: String, widgetId: String): PluginDataCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun cacheData(cache: PluginDataCacheEntity)

    @Query("DELETE FROM plugin_data_cache WHERE pluginId = :pluginId AND widgetId = :widgetId")
    suspend fun clearCache(pluginId: String, widgetId: String)
}
