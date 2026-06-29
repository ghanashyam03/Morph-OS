package com.morphos.app.core.domain.agent

import com.morphos.app.core.common.AppResult
import com.morphos.app.core.domain.model.ContentCandidate
import com.morphos.app.core.domain.model.ContextSnapshot
import com.morphos.app.core.domain.model.PrioritizedNotification
import com.morphos.app.core.domain.model.RankedContent
import com.morphos.app.core.domain.model.WidgetPlan
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface AgentOrchestrator {
    fun start()
    fun stop()
    suspend fun handleUserInput(input: String): AppResult<WidgetPlan>
    fun getContextFlow(): StateFlow<ContextSnapshot>
    fun getNotificationFlow(): Flow<List<PrioritizedNotification>>
    suspend fun triggerMemorySummarization(): AppResult<Unit>
    suspend fun triggerWidgetRanking(widgetId: String, candidates: List<ContentCandidate>): AppResult<List<RankedContent>>
}
