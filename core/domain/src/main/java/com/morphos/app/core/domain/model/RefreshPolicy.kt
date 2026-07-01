package com.morphos.app.core.domain.model

import kotlinx.serialization.Serializable

enum class RefreshType { PERIODIC, EVENT_DRIVEN, MANUAL, HYBRID }

enum class TriggerEvent {
    USER_UNLOCK, CALENDAR_EVENT_START, BATTERY_LOW,
    CONNECTIVITY_CHANGED, LOCATION_CHANGED, TIME_THRESHOLD
}

@Serializable
data class RefreshPolicy(
    val type: RefreshType,
    val minIntervalSeconds: Int = 1800,
    val maxIntervalSeconds: Int = 86400,
    val triggerEvents: List<TriggerEvent> = emptyList()
)
