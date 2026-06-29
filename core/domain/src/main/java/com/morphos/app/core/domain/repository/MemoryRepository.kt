package com.morphos.app.core.domain.repository

import com.morphos.app.core.domain.model.ShortTermEvent
import com.morphos.app.core.domain.model.LongTermMemory
import com.morphos.app.core.domain.model.MemoryProfile
import kotlinx.coroutines.flow.Flow

interface MemoryRepository {
    fun getShortTermEvents(): Flow<List<ShortTermEvent>>
    suspend fun saveShortTermEvent(event: ShortTermEvent)
    suspend fun getLongTermMemories(): List<LongTermMemory>
    suspend fun saveLongTermMemory(memory: LongTermMemory)
    fun getMemoryProfile(): Flow<MemoryProfile>
    suspend fun saveMemoryProfile(profile: MemoryProfile)
    suspend fun clearAllMemory()
}
