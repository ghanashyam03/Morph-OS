package com.morphos.app.core.domain.agent

import com.morphos.app.core.domain.model.AgentEvent
import kotlinx.coroutines.flow.SharedFlow

interface AgentOrchestrator {
    val eventBus: SharedFlow<AgentEvent>
    fun dispatchEvent(event: AgentEvent)
    fun start()
    fun stop()
}
