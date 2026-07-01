package com.morphos.app.core.data.repository

import com.morphos.app.core.common.AppDispatchers
import com.morphos.app.core.common.AppResult
import com.morphos.app.core.data.datastore.MemoryProfileDataSource
import com.morphos.app.core.data.db.LongTermMemoryDao
import com.morphos.app.core.data.db.ShortTermEventDao
import com.morphos.app.core.data.mapper.toDomain
import com.morphos.app.core.data.mapper.toEntity
import com.morphos.app.core.domain.model.LongTermMemory
import com.morphos.app.core.domain.model.MemoryProfile
import com.morphos.app.core.domain.model.MemoryType
import com.morphos.app.core.domain.model.ShortTermEvent
import com.morphos.app.core.domain.repository.MemoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MemoryRepositoryImpl @Inject constructor(
    private val shortTermEventDao: ShortTermEventDao,
    private val longTermMemoryDao: LongTermMemoryDao,
    private val memoryProfileDataSource: MemoryProfileDataSource,
    private val dispatchers: AppDispatchers
) : MemoryRepository {

    override suspend fun recordShortTermEvent(event: ShortTermEvent): AppResult<Unit> = withContext(dispatchers.io) {
        try {
            shortTermEventDao.insertEvent(event.toEntity())
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e)
        }
    }

    override fun getRecentEvents(limit: Int): Flow<List<ShortTermEvent>> {
        return shortTermEventDao.getRecentEvents(limit).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveMemoryProfile(profile: MemoryProfile): AppResult<Unit> = withContext(dispatchers.io) {
        try {
            memoryProfileDataSource.saveMemoryProfile(profile)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e)
        }
    }

    override suspend fun getMemoryProfile(): MemoryProfile = withContext(dispatchers.io) {
        try {
            memoryProfileDataSource.getMemoryProfile()
        } catch (e: Exception) {
            MemoryProfile()
        }
    }

    override suspend fun saveLongTermMemory(memory: LongTermMemory): AppResult<Unit> = withContext(dispatchers.io) {
        try {
            longTermMemoryDao.insertMemory(memory.toEntity())
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e)
        }
    }

    override fun getLongTermMemories(type: MemoryType?): Flow<List<LongTermMemory>> {
        val now = System.currentTimeMillis()
        val flow = if (type == null) {
            longTermMemoryDao.getLiveMemories(now)
        } else {
            longTermMemoryDao.getByType(type.name, now)
        }
        return flow.map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun deleteMemoriesOlderThan(epochMillis: Long): AppResult<Int> = withContext(dispatchers.io) {
        try {
            val count = longTermMemoryDao.pruneOld(epochMillis)
            AppResult.Success(count)
        } catch (e: Exception) {
            AppResult.Error(e)
        }
    }

    override suspend fun clearAllMemory(): AppResult<Unit> = withContext(dispatchers.io) {
        try {
            longTermMemoryDao.clearAll()
            shortTermEventDao.deleteOlderThan(Long.MAX_VALUE)
            memoryProfileDataSource.saveMemoryProfile(MemoryProfile())
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e)
        }
    }
}
