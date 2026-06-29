package com.morphos.app.core.data.plugin

import android.content.Context
import com.morphos.app.core.common.AppResult
import com.morphos.app.core.common.safeCall
import com.morphos.app.core.data.db.PluginDataCacheDao
import com.morphos.app.core.data.network.WeatherApiService
import com.morphos.app.core.domain.model.*
import com.morphos.app.core.domain.repository.DataPlugin
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject

class WeatherPlugin @Inject constructor(
    @ApplicationContext private val context: Context,
    private val weatherApiService: WeatherApiService,
    private val pluginDataCacheDao: PluginDataCacheDao
) : DataPlugin {

    override val pluginId: String = "weather"
    override val displayName: String = "Weather"
    override val requiredPermissions: List<String> = emptyList()

    override val configSchema: PluginConfigSchema = PluginConfigSchema(
        listOf(
            PluginConfigField("latitude", "Latitude", PluginFieldType.NUMBER, false, "52.52"),
            PluginConfigField("longitude", "Longitude", PluginFieldType.NUMBER, false, "13.41")
        )
    )

    override suspend fun fetch(config: Map<String, String>): AppResult<PluginData> = safeCall {
        val lat = config["latitude"]?.toDoubleOrNull() ?: 52.52
        val lon = config["longitude"]?.toDoubleOrNull() ?: 13.41

        var isStale = false
        val rawJson = try {
            val response = weatherApiService.getForecast(lat, lon)
            buildJsonObject {
                put("temperature", response.current_weather.temperature)
                put("weathercode", response.current_weather.weathercode)
                put("windspeed", response.current_weather.windspeed)
            }.toString()
        } catch (e: Exception) {
            val cached = pluginDataCacheDao.getBySourceId("${pluginId}_source")
            if (cached != null) {
                isStale = true
                cached.rawValue
            } else {
                buildJsonObject {
                    put("temperature", 20.0)
                    put("weathercode", 0)
                    put("windspeed", 5.0)
                }.toString()
            }
        }

        PluginData(
            pluginId = pluginId,
            dataSourceId = "${pluginId}_source",
            rawValue = rawJson,
            fetchedAt = System.currentTimeMillis(),
            isStale = isStale
        )
    }

    override fun canFetch(context: ContextSnapshot): Boolean = true
}
