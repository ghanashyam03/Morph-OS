package com.morphos.app.core.domain.model

sealed class AgentEvent {
    data class ContextUpdated(val snapshot: ContextSnapshot) : AgentEvent()
    data class IntentParsed(val intent: WidgetIntent) : AgentEvent()
    data class PlanGenerated(val plan: WidgetPlan, val requestId: String) : AgentEvent()
    data class MemoryUpdated(val profile: MemoryProfile) : AgentEvent()
    data class NotificationReceived(val notification: PrioritizedNotification) : AgentEvent()
    data class RankingCompleted(val ranked: List<RankedContent>, val widgetId: String) : AgentEvent()
    data class AgentFailed(val agentId: String, val error: Throwable, val taskId: String) : AgentEvent()
}
