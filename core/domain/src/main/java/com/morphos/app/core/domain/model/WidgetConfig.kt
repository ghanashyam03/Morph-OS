package com.morphos.app.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PriorityWeights(
    val timeOfDay: Float,
    val location: Float,
    val battery: Float,
    val engagement: Float
)

@Serializable
data class ThemeConfig(
    val primaryColorHex: String,
    val secondaryColorHex: String,
    val useDarkTheme: Boolean
)

@Serializable
data class WidgetConfig(
    val id: String,
    val name: String,
    val templateId: String,
    val sizeClass: WidgetSizeClass,
    val slots: Map<String, SlotConfig>,
    val dataBindings: List<DataBinding>,
    val refreshPolicy: RefreshPolicy,
    val priorityWeights: PriorityWeights,
    val themeOverride: ThemeConfig? = null,
    val createdAtEpochMilli: Long,
    val lastModifiedEpochMilli: Long,
    val version: Int
)
