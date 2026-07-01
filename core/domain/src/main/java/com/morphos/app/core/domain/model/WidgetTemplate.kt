package com.morphos.app.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class WidgetTemplate(
    val templateId: String,
    val displayName: String,
    val description: String,
    val supportedSizeClasses: List<WidgetSizeClass>,
    val slots: List<String>
)
