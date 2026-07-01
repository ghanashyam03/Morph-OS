package com.morphos.app.core.data.plugin

import android.content.Context
import com.morphos.app.core.common.AppResult
import com.morphos.app.core.common.safeCall
import com.morphos.app.core.domain.model.*
import com.morphos.app.core.domain.repository.DataPlugin
import com.morphos.app.core.domain.repository.NotificationRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject

class NotificationPlugin @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationRepository: NotificationRepository
) : DataPlugin {

    override val pluginId: String = "notifications"
    override val displayName: String = "Notifications Feed"
    override val requiredPermissions: List<String> = listOf("android.permission.BIND_NOTIFICATION_LISTENER_SERVICE")

    override val configSchema: PluginConfigSchema = PluginConfigSchema(emptyList())

    override suspend fun fetch(config: Map<String, String>): AppResult<PluginData> = safeCall {
        val list = notificationRepository.getPrioritizedNotifications().first().take(3)
        val rawJson = buildJsonArray {
            list.forEach { notif ->
                add(buildJsonObject {
                    put("id", notif.id)
                    put("appName", notif.appName)
                    put("title", notif.title ?: "")
                    put("text", notif.text ?: "")
                    put("priority", notif.priority.name)
                })
            }
        }.toString()

        PluginData(
            pluginId = pluginId,
            dataSourceId = "${pluginId}_source",
            rawValue = rawJson,
            fetchedAt = System.currentTimeMillis()
        )
    }

    override fun canFetch(context: ContextSnapshot): Boolean {
        val list = try {
            runBlocking {
                notificationRepository.getPrioritizedNotifications().first()
            }
        } catch (e: Exception) {
            emptyList()
        }
        return list.isNotEmpty()
    }
}
