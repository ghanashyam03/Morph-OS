package com.morphos.app.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class SlotConfig(
    val slotId: String,
    val contentType: ContentType,
    val dataSourceId: String,
    val transformExpression: String? = null,
    val fallbackValue: String = "",
    val maxLength: Int = 100,
    val priority: Int = 0
)
