package com.morphos.app.core.data.plugin

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Process
import com.morphos.app.core.common.AppResult
import com.morphos.app.core.common.safeCall
import com.morphos.app.core.domain.model.*
import com.morphos.app.core.domain.repository.DataPlugin
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.*
import javax.inject.Inject

class UsageStatsPlugin @Inject constructor(
    @ApplicationContext private val context: Context
) : DataPlugin {

    override val pluginId: String = "usage_stats"
    override val displayName: String = "App Usage Stats"
    override val requiredPermissions: List<String> = emptyList()

    override val configSchema: PluginConfigSchema = PluginConfigSchema(emptyList())

    override suspend fun fetch(config: Map<String, String>): AppResult<PluginData> = safeCall {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        val hasPermission = mode == AppOpsManager.MODE_ALLOWED

        val rawJson = if (hasPermission) {
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startTime = calendar.timeInMillis
            val endTime = System.currentTimeMillis()

            val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
            val topApps = stats
                .filter { it.totalTimeInForeground > 0 }
                .sortedByDescending { it.totalTimeInForeground }
                .take(3)

            buildJsonArray {
                topApps.forEach { stat ->
                    add(buildJsonObject {
                        put("package", stat.packageName)
                        put("minutes", (stat.totalTimeInForeground / (1000 * 60)).toInt())
                    })
                }
            }.toString()
        } else {
            "[]"
        }

        PluginData(
            pluginId = pluginId,
            dataSourceId = "${pluginId}_source",
            rawValue = rawJson,
            fetchedAt = System.currentTimeMillis()
        )
    }

    override fun canFetch(context: ContextSnapshot): Boolean = true
}
