package com.morphos.app.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ShortTermEvent(
    val id: String,
    val eventType: ShortTermEventType,
    val widgetId: String?,
    val metadata: Map<String, String> = emptyMap(),
    val timestamp: Long
)

enum class ShortTermEventType {
    WIDGET_TAPPED, WIDGET_DISMISSED, SLOT_ENGAGED, WIDGET_EDITED,
    NL_INPUT_SUBMITTED, NOTIFICATION_DISMISSED, NOTIFICATION_ACTED_UPON,
    WIDGET_CREATED, WIDGET_DELETED
}
