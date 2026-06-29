package com.morphos.app.core.data.plugin

import com.morphos.app.core.domain.model.ContextSnapshot
import com.morphos.app.core.domain.model.PluginConfigSchema
import com.morphos.app.core.domain.model.PluginData

interface DataPlugin {
    val pluginId: String
    val displayName: String
    val requiredPermissions: List<String>
    val configSchema: PluginConfigSchema
    suspend fun fetch(config: Map<String, String>): Result<PluginData>
    fun canFetch(context: ContextSnapshot): Boolean
}
