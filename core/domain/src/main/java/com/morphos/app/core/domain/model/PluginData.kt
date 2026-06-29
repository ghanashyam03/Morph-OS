package com.morphos.app.core.domain.model

data class PluginData(
    val pluginId: String,
    val dataSourceId: String,
    val rawValue: String,
    val fetchedAt: Long,
    val isStale: Boolean = false
)

data class PluginConfigSchema(
    val fields: List<PluginConfigField>
)

data class PluginConfigField(
    val key: String,
    val label: String,
    val type: PluginFieldType,
    val required: Boolean = false,
    val defaultValue: String? = null
)

enum class PluginFieldType { TEXT, URL, NUMBER, BOOLEAN, ENUM }
