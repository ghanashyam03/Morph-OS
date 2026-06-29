package com.morphos.app.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
sealed interface AgentEvent {
    data class ContextUpdated(val snapshot: ContextSnapshot) : AgentEvent
    data class IntentDetected(val intent: WidgetIntent) : AgentEvent
    data class PlanCreated(val plan: WidgetPlan) : AgentEvent
    data class MemorySummarized(val summary: String) : AgentEvent
    data class WidgetTapped(val widgetId: String, val slotId: String) : AgentEvent
}
