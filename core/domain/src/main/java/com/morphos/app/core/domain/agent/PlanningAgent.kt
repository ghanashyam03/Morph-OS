package com.morphos.app.core.domain.agent

import com.morphos.app.core.domain.model.ContextSnapshot
import com.morphos.app.core.domain.model.MemoryProfile
import com.morphos.app.core.domain.model.WidgetIntent
import com.morphos.app.core.domain.model.WidgetPlan

interface PlanningAgent {
    suspend fun createPlan(
        intent: WidgetIntent,
        context: ContextSnapshot,
        memoryProfile: MemoryProfile,
        availablePlugins: List<String>
    ): WidgetPlan
}
