package com.morphos.app.core.data.repository

import com.morphos.app.core.domain.model.ShortTermEvent
import com.morphos.app.core.domain.model.LongTermMemory
import com.morphos.app.core.domain.model.MemoryProfile
import com.morphos.app.core.domain.repository.MemoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MemoryRepositoryImpl @Inject constructor() : MemoryRepository {
    override fun getShortTermEvents(): Flow<List<ShortTermEvent>> {
        TODO("Not yet implemented")
    }

    override suspend fun saveShortTermEvent(event: ShortTermEvent) {
        TODO("Not yet implemented")
    }

    override suspend fun getLongTermMemories(): List<LongTermMemory> {
        TODO("Not yet implemented")
    }

    override suspend fun saveLongTermMemory(memory: LongTermMemory) {
        TODO("Not yet implemented")
    }

    override fun getMemoryProfile(): Flow<MemoryProfile> {
        TODO("Not yet implemented")
    }

    override suspend fun saveMemoryProfile(profile: MemoryProfile) {
        TODO("Not yet implemented")
    }

    override suspend fun clearAllMemory() {
        TODO("Not yet implemented")
    }
}
