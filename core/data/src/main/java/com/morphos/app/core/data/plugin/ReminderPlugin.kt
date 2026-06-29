package com.morphos.app.core.data.plugin

import com.morphos.app.core.domain.model.ContextSnapshot
import com.morphos.app.core.domain.model.PluginConfigSchema
import com.morphos.app.core.domain.model.PluginData
import javax.inject.Inject

class ReminderPlugin @Inject constructor() : DataPlugin {
    override val pluginId = "reminders"
    override val displayName = "Reminders"
    override val requiredPermissions = listOf("android.permission.READ_CALENDAR")
    override val configSchema = PluginConfigSchema(emptyMap())

    override suspend fun fetch(config: Map<String, String>): Result<PluginData> {
        TODO("Not yet implemented")
    }

    override fun canFetch(context: ContextSnapshot): Boolean {
        TODO("Not yet implemented")
    }
}
