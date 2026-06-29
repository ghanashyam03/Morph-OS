package com.morphos.app.core.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.OnConflictStrategy

@Entity(tableName = "long_term_memories")
data class LongTermMemoryEntity(
    @PrimaryKey val id: String,
    val content: String,
    val embeddingId: String,
    val timestamp: Long
)

@Dao
interface LongTermMemoryDao {
    @Query("SELECT * FROM long_term_memories ORDER BY timestamp DESC")
    suspend fun getMemories(): List<LongTermMemoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: LongTermMemoryEntity)

    @Query("DELETE FROM long_term_memories")
    suspend fun clearMemories()
}
