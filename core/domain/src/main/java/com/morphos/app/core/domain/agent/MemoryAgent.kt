package com.morphos.app.core.domain.agent

import com.morphos.app.core.domain.model.MemoryProfile
import com.morphos.app.core.domain.model.ShortTermEvent
import kotlinx.coroutines.flow.StateFlow

interface MemoryAgent {
    val memoryProfile: StateFlow<MemoryProfile?>
    suspend fun recordEvent(event: ShortTermEvent)
    suspend fun runConsolidation()
}
