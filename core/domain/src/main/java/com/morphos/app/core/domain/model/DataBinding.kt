package com.morphos.app.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class DataBinding(
    val dataSourceId: String,
    val pluginId: String,
    val pluginConfig: Map<String, String>,
    val refreshIntervalSeconds: Int,
    val cacheTtlSeconds: Int
)
