package com.morphos.app.core.data.agent

import com.morphos.app.core.domain.agent.PlanningAgent
import com.morphos.app.core.domain.model.ContextSnapshot
import com.morphos.app.core.domain.model.MemoryProfile
import com.morphos.app.core.domain.model.WidgetIntent
import com.morphos.app.core.domain.model.WidgetPlan
import javax.inject.Inject

class PlanningAgentImpl @Inject constructor() : PlanningAgent {
    override suspend fun createPlan(
        intent: WidgetIntent,
        context: ContextSnapshot,
        memoryProfile: MemoryProfile,
        availablePlugins: List<String>
    ): WidgetPlan {
        TODO("Not yet implemented")
    }
}
