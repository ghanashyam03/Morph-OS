package com.morphos.app.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class WidgetIntent(
    val rawInput: String,
    val intentType: IntentType,
    val slots: Map<String, String> = emptyMap(),  // slot name → extracted value
    val confidence: Float = 0f,
    val requiresCloudFallback: Boolean = false
)

enum class IntentType {
    CREATE_STUDY_WIDGET, CREATE_TRAVEL_WIDGET, CREATE_FITNESS_WIDGET,
    CREATE_PRODUCTIVITY_WIDGET, CREATE_WEATHER_WIDGET, CREATE_FINANCE_WIDGET,
    CREATE_CUSTOM_WIDGET, EDIT_EXISTING_WIDGET, UNKNOWN
}
