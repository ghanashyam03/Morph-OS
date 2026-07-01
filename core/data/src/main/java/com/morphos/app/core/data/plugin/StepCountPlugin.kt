package com.morphos.app.core.data.plugin

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.morphos.app.core.common.AppResult
import com.morphos.app.core.common.safeCall
import com.morphos.app.core.domain.model.*
import com.morphos.app.core.domain.repository.DataPlugin
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject

class StepCountPlugin @Inject constructor(
    @ApplicationContext private val context: Context
) : DataPlugin {

    override val pluginId: String = "steps"
    override val displayName: String = "Step Count"
    override val requiredPermissions: List<String> = listOf("android.permission.health.READ_STEPS")

    override val configSchema: PluginConfigSchema = PluginConfigSchema(emptyList())

    override suspend fun fetch(config: Map<String, String>): AppResult<PluginData> = safeCall {
        val nowTime = System.currentTimeMillis()
        var rawJson = "{\"steps\":0}"

        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            "android.permission.health.READ_STEPS"
        ) == PackageManager.PERMISSION_GRANTED

        val isAvailable = try {
            HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE
        } catch (e: Exception) {
            false
        }

        if (hasPermission && isAvailable) {
            try {
                val client = HealthConnectClient.getOrCreate(context)
                val nowInstant = Instant.now()
                val startInstant = ZonedDateTime.now(ZoneId.systemDefault()).toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant()

                val response = client.aggregate(
                    AggregateRequest(
                        metrics = setOf(StepsRecord.COUNT_TOTAL),
                        timeRangeFilter = TimeRangeFilter.between(startInstant, nowInstant)
                    )
                )
                val steps = response[StepsRecord.COUNT_TOTAL] ?: 0L
                rawJson = "{\"steps\":$steps,\"goal\":10000}"
            } catch (e: Exception) {
                // fall back to default
            }
        }

        PluginData(
            pluginId = pluginId,
            dataSourceId = "${pluginId}_source",
            rawValue = rawJson,
            fetchedAt = nowTime
        )
    }

    override fun canFetch(context: ContextSnapshot): Boolean = true
}
