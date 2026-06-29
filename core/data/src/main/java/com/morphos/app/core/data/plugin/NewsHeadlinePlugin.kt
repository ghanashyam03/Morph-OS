package com.morphos.app.core.data.plugin

import com.morphos.app.core.domain.model.ContextSnapshot
import com.morphos.app.core.domain.model.PluginConfigSchema
import com.morphos.app.core.domain.model.PluginData
import javax.inject.Inject

class NewsHeadlinePlugin @Inject constructor() : DataPlugin {
    override val pluginId = "news"
    override val displayName = "News Headlines"
    override val requiredPermissions = emptyList<String>()
    override val configSchema = PluginConfigSchema(mapOf("feedUrl" to "TEXT"))

    override suspend fun fetch(config: Map<String, String>): Result<PluginData> {
        TODO("Not yet implemented")
    }

    override fun canFetch(context: ContextSnapshot): Boolean {
        TODO("Not yet implemented")
    }
}
