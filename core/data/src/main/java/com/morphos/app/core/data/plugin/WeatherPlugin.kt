package com.morphos.app.core.data.plugin

import com.morphos.app.core.domain.model.ContextSnapshot
import com.morphos.app.core.domain.model.PluginConfigSchema
import com.morphos.app.core.domain.model.PluginData
import javax.inject.Inject

class WeatherPlugin @Inject constructor() : DataPlugin {
    override val pluginId = "weather"
    override val displayName = "Weather"
    override val requiredPermissions = listOf("android.permission.ACCESS_FINE_LOCATION")
    override val configSchema = PluginConfigSchema(emptyMap())

    override suspend fun fetch(config: Map<String, String>): Result<PluginData> {
        TODO("Not yet implemented")
    }

    override fun canFetch(context: ContextSnapshot): Boolean {
        TODO("Not yet implemented")
    }
}
