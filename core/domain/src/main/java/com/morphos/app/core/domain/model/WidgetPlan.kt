package com.morphos.app.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class WidgetPlan(
    val widgetIntentRaw: String,
    val selectedTemplateId: String,
    val suggestedName: String,
    val suggestedDescription: String,
    val slotAssignments: Map<String, String>,  // slotId → pluginId
    val pluginConfigs: Map<String, Map<String, String>>,
    val suggestedRefreshPolicy: RefreshPolicy,
    val suggestedPriorityWeights: PriorityWeights,
    val planConfidence: Float,
    val planSource: PlanSource
)

enum class PlanSource { TIER_0_LOCAL, TIER_1_LOCAL, TIER_2_CLOUD, RULE_BASED_FALLBACK }
