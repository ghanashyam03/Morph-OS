package com.morphos.app.core.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "long_term_memory",
    indices = [Index("memoryType"), Index("createdAt")]
)
data class LongTermMemoryEntity(
    @PrimaryKey val id: String,
    val content: String,
    val embeddingId: String?,
    val memoryType: String,
    val confidence: Float,
    val createdAt: Long,
    val expiresAt: Long?
)

@Dao
interface LongTermMemoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: LongTermMemoryEntity)

    @Query("SELECT * FROM long_term_memory WHERE (expiresAt IS NULL OR expiresAt > :now) ORDER BY createdAt DESC")
    fun getLiveMemories(now: Long): Flow<List<LongTermMemoryEntity>>

    @Query("SELECT * FROM long_term_memory WHERE memoryType = :type AND (expiresAt IS NULL OR expiresAt > :now)")
    fun getByType(type: String, now: Long): Flow<List<LongTermMemoryEntity>>

    @Query("DELETE FROM long_term_memory WHERE createdAt < :cutoff")
    suspend fun pruneOld(cutoff: Long): Int

    @Query("DELETE FROM long_term_memory")
    suspend fun clearAll()
}
