package com.morphos.app.core.data.plugin

import android.content.Context
import com.morphos.app.core.common.AppResult
import com.morphos.app.core.common.safeCall
import com.morphos.app.core.domain.model.*
import com.morphos.app.core.domain.repository.DataPlugin
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject

class CountdownPlugin @Inject constructor(
    @ApplicationContext private val context: Context
) : DataPlugin {

    override val pluginId: String = "countdown"
    override val displayName: String = "Countdown Timer"
    override val requiredPermissions: List<String> = emptyList()

    override val configSchema: PluginConfigSchema = PluginConfigSchema(
        listOf(
            PluginConfigField("targetDateEpochMillis", "Target Date (Epoch Millis)", PluginFieldType.NUMBER, true),
            PluginConfigField("label", "Event Label", PluginFieldType.TEXT, true)
        )
    )

    override suspend fun fetch(config: Map<String, String>): AppResult<PluginData> = safeCall {
        val targetMillisStr = config["targetDateEpochMillis"] ?: "0"
        val targetMillis = targetMillisStr.toLongOrNull() ?: 0L
        val label = config["label"] ?: "Event"

        val now = System.currentTimeMillis()
        val diffMillis = maxOf(0L, targetMillis - now)

        val hoursLeft = (diffMillis / (1000 * 60 * 60)).toInt()
        val daysLeft = hoursLeft / 24

        val rawJson = buildJsonObject {
            put("label", label)
            put("daysLeft", daysLeft)
            put("hoursLeft", hoursLeft)
        }.toString()

        PluginData(
            pluginId = pluginId,
            dataSourceId = "${pluginId}_source",
            rawValue = rawJson,
            fetchedAt = now
        )
    }

    override fun canFetch(context: ContextSnapshot): Boolean = true
}
