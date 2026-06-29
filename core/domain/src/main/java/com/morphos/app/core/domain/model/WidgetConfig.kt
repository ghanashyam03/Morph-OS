package com.morphos.app.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PriorityWeights(
    val recencyWeight: Float = 0.3f,
    val engagementWeight: Float = 0.4f,
    val contextRelevanceWeight: Float = 0.3f
)

@Serializable
data class ThemeConfig(
    val backgroundColor: String? = null,   // hex string "#RRGGBB"
    val textColor: String? = null,
    val accentColor: String? = null,
    val useSystemTheme: Boolean = true
)

@Serializable
data class WidgetConfig(
    val id: String,                          // UUID string
    val name: String,
    val description: String,
    val templateId: String,
    val sizeClass: WidgetSizeClass,
    val slots: Map<String, SlotConfig>,
    val dataBindings: List<DataBinding>,
    val refreshPolicy: RefreshPolicy,
    val priorityWeights: PriorityWeights,
    val themeOverride: ThemeConfig? = null,
    val isPinned: Boolean = false,
    val createdAt: Long,                     // epochMillis
    val lastModified: Long,                  // epochMillis
    val lastRefreshed: Long = 0L,
    val version: Int = 1
)
