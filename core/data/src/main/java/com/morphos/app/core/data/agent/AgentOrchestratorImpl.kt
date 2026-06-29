package com.morphos.app.core.data.agent

import com.morphos.app.core.domain.agent.AgentOrchestrator
import com.morphos.app.core.domain.model.AgentEvent
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AgentOrchestratorImpl @Inject constructor(
    private val agentEventBus: AgentEventBus
) : AgentOrchestrator {
    override val eventBus: SharedFlow<AgentEvent> = agentEventBus.events

    override fun dispatchEvent(event: AgentEvent) {
        TODO("Not yet implemented")
    }

    override fun start() {
        TODO("Not yet implemented")
    }

    override fun stop() {
        TODO("Not yet implemented")
    }
}
