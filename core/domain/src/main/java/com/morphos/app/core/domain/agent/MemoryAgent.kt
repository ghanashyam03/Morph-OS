package com.morphos.app.core.domain.agent

import com.morphos.app.core.common.AppResult
import com.morphos.app.core.domain.model.LongTermMemory
import com.morphos.app.core.domain.model.MemoryProfile
import com.morphos.app.core.domain.model.ShortTermEvent

interface MemoryAgent {
    suspend fun recordEvent(event: ShortTermEvent): AppResult<Unit>
    suspend fun summarizeRecentMemory(): AppResult<MemoryProfile>
    suspend fun retrieveRelevantMemories(query: String): AppResult<List<LongTermMemory>>
    suspend fun updateMemoryProfile(profile: MemoryProfile): AppResult<Unit>
}
