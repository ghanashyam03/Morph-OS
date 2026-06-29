package com.morphos.app.core.data.plugin

import com.morphos.app.core.domain.model.ContextSnapshot
import com.morphos.app.core.domain.model.PluginConfigSchema
import com.morphos.app.core.domain.model.PluginData
import javax.inject.Inject

class ClockPlugin @Inject constructor() : DataPlugin {
    override val pluginId = "clock"
    override val displayName = "Clock"
    override val requiredPermissions = emptyList<String>()
    override val configSchema = PluginConfigSchema(emptyMap())

    override suspend fun fetch(config: Map<String, String>): Result<PluginData> {
        TODO("Not yet implemented")
    }

    override fun canFetch(context: ContextSnapshot): Boolean {
        TODO("Not yet implemented")
    }
}
