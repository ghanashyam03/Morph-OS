package com.morphos.app.core.data.plugin

import android.content.Context
import com.morphos.app.core.common.AppResult
import com.morphos.app.core.common.safeCall
import com.morphos.app.core.domain.model.*
import com.morphos.app.core.domain.repository.DataPlugin
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class ClockPlugin @Inject constructor(
    @ApplicationContext private val context: Context
) : DataPlugin {

    override val pluginId: String = "clock"
    override val displayName: String = "Clock & Date"
    override val requiredPermissions: List<String> = emptyList()

    override val configSchema: PluginConfigSchema = PluginConfigSchema(emptyList())

    override suspend fun fetch(config: Map<String, String>): AppResult<PluginData> = safeCall {
        val now = Date()
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEE, dd MMM", Locale.getDefault())

        val timeStr = timeFormat.format(now)
        val dateStr = dateFormat.format(now)
        val timezoneStr = TimeZone.getDefault().id

        val rawJson = buildJsonObject {
            put("time", timeStr)
            put("date", dateStr)
            put("timezone", timezoneStr)
        }.toString()

        PluginData(
            pluginId = pluginId,
            dataSourceId = "${pluginId}_source",
            rawValue = rawJson,
            fetchedAt = System.currentTimeMillis()
        )
    }

    override fun canFetch(context: ContextSnapshot): Boolean = true
}
