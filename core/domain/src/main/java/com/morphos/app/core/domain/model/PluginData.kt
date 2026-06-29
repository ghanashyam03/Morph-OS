package com.morphos.app.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PluginConfigSchema(
    val fields: Map<String, String>
)

@Serializable
data class PluginData(
    val pluginId: String,
    val timestamp: Long,
    val data: Map<String, String>
)
