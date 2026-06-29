package com.morphos.app.core.domain.model

import kotlinx.serialization.Serializable

enum class RefreshType {
    PERIODIC, EVENT_DRIVEN, MANUAL, HYBRID
}

@Serializable
data class TriggerEvent(
    val eventType: String,
    val parameters: Map<String, String>
)

@Serializable
data class RefreshPolicy(
    val type: RefreshType,
    val minIntervalSeconds: Int,
    val maxIntervalSeconds: Int,
    val triggerEvents: List<TriggerEvent>
)
