package com.morphos.app.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class WidgetIntent(
    val intentId: String,
    val type: String,
    val slots: Map<String, String>,
    val confidenceScore: Float,
    val rawText: String
)
