package com.morphos.app.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class DataBinding(
    val dataSourceId: String,
    val pluginId: String,
    val pluginConfig: Map<String, String> = emptyMap(),
    val refreshIntervalSeconds: Int = 1800,
    val cacheTtlSeconds: Int = 3600
)
