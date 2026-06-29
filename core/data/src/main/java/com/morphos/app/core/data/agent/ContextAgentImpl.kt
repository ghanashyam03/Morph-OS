package com.morphos.app.core.data.agent

import com.morphos.app.core.domain.agent.ContextAgent
import com.morphos.app.core.domain.model.ContextSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContextAgentImpl @Inject constructor() : ContextAgent {
    private val _contextState = MutableStateFlow<ContextSnapshot?>(null)
    override val contextState: StateFlow<ContextSnapshot?> = _contextState

    override suspend fun refreshContext() {
        TODO("Not yet implemented")
    }
}
