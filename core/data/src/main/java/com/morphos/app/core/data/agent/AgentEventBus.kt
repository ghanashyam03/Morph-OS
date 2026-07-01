package com.morphos.app.core.data.agent

import com.morphos.app.core.domain.model.AgentEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AgentEventBus @Inject constructor() {
    private val _events = MutableSharedFlow<AgentEvent>()
    val events: SharedFlow<AgentEvent> = _events

    suspend fun postEvent(event: AgentEvent) {
        _events.emit(event)
    }
}
