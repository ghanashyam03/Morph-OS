package com.morphos.app.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ShortTermEvent(
    val id: String,
    val timestamp: Long,
    val type: String,
    val widgetId: String?,
    val metadata: Map<String, String>
)
