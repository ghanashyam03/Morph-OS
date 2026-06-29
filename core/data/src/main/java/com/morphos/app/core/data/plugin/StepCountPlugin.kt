package com.morphos.app.core.data.plugin

import com.morphos.app.core.domain.model.ContextSnapshot
import com.morphos.app.core.domain.model.PluginConfigSchema
import com.morphos.app.core.domain.model.PluginData
import javax.inject.Inject

class StepCountPlugin @Inject constructor() : DataPlugin {
    override val pluginId = "step_count"
    override val displayName = "Step Count"
    override val requiredPermissions = listOf("android.permission.ACTIVITY_RECOGNITION")
    override val configSchema = PluginConfigSchema(emptyMap())

    override suspend fun fetch(config: Map<String, String>): Result<PluginData> {
        TODO("Not yet implemented")
    }

    override fun canFetch(context: ContextSnapshot): Boolean {
        TODO("Not yet implemented")
    }
}
