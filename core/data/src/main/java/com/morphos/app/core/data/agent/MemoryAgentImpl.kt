package com.morphos.app.core.data.agent

import com.morphos.app.core.domain.agent.MemoryAgent
import com.morphos.app.core.domain.model.MemoryProfile
import com.morphos.app.core.domain.model.ShortTermEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoryAgentImpl @Inject constructor() : MemoryAgent {
    private val _memoryProfile = MutableStateFlow<MemoryProfile?>(null)
    override val memoryProfile: StateFlow<MemoryProfile?> = _memoryProfile

    override suspend fun recordEvent(event: ShortTermEvent) {
        TODO("Not yet implemented")
    }

    override suspend fun runConsolidation() {
        TODO("Not yet implemented")
    }
}
