package com.morphos.app.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class WidgetPlan(
    val intentId: String,
    val templateId: String,
    val sizeClass: WidgetSizeClass,
    val slots: Map<String, SlotConfig>,
    val dataBindings: List<DataBinding>,
    val refreshPolicy: RefreshPolicy,
    val priorityWeights: PriorityWeights
)
