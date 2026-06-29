package com.morphos.app.core.domain.repository

import com.morphos.app.core.common.AppResult
import com.morphos.app.core.domain.model.ShortTermEvent
import com.morphos.app.core.domain.model.LongTermMemory
import com.morphos.app.core.domain.model.MemoryProfile
import com.morphos.app.core.domain.model.MemoryType
import kotlinx.coroutines.flow.Flow

interface MemoryRepository {
    suspend fun recordShortTermEvent(event: ShortTermEvent): AppResult<Unit>
    fun getRecentEvents(limit: Int): Flow<List<ShortTermEvent>>
    suspend fun saveMemoryProfile(profile: MemoryProfile): AppResult<Unit>
    suspend fun getMemoryProfile(): MemoryProfile
    suspend fun saveLongTermMemory(memory: LongTermMemory): AppResult<Unit>
    fun getLongTermMemories(type: MemoryType? = null): Flow<List<LongTermMemory>>
    suspend fun deleteMemoriesOlderThan(epochMillis: Long): AppResult<Int>
    suspend fun clearAllMemory(): AppResult<Unit>
}
