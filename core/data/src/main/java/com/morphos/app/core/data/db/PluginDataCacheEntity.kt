package com.morphos.app.core.data.db

import androidx.room.*

@Entity(
    tableName = "plugin_data_cache",
    indices = [Index("dataSourceId", unique = true)]
)
data class PluginDataCacheEntity(
    @PrimaryKey val dataSourceId: String,
    val pluginId: String,
    val rawValue: String,
    val fetchedAt: Long,
    val ttlSeconds: Int
)

@Dao
interface PluginDataCacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PluginDataCacheEntity)

    @Query("SELECT * FROM plugin_data_cache WHERE dataSourceId = :id")
    suspend fun getBySourceId(id: String): PluginDataCacheEntity?

    @Query("DELETE FROM plugin_data_cache WHERE dataSourceId = :id")
    suspend fun invalidate(id: String)

    @Query("DELETE FROM plugin_data_cache WHERE fetchedAt + (ttlSeconds * 1000) < :now")
    suspend fun pruneExpired(now: Long)
}
