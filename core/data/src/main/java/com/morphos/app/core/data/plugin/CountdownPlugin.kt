package com.morphos.app.core.data.plugin

import com.morphos.app.core.domain.model.ContextSnapshot
import com.morphos.app.core.domain.model.PluginConfigSchema
import com.morphos.app.core.domain.model.PluginData
import javax.inject.Inject

class CountdownPlugin @Inject constructor() : DataPlugin {
    override val pluginId = "countdown"
    override val displayName = "Countdown"
    override val requiredPermissions = emptyList<String>()
    override val configSchema = PluginConfigSchema(mapOf("targetDate" to "TEXT"))

    override suspend fun fetch(config: Map<String, String>): Result<PluginData> {
        TODO("Not yet implemented")
    }

    override fun canFetch(context: ContextSnapshot): Boolean {
        TODO("Not yet implemented")
    }
}
