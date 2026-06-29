package com.morphos.app.core.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "short_term_events",
    indices = [Index("timestamp"), Index("widgetId")]
)
data class ShortTermEventEntity(
    @PrimaryKey val id: String,
    val eventType: String,
    val widgetId: String?,
    val metadataJson: String,
    val timestamp: Long
)

@Dao
interface ShortTermEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: ShortTermEventEntity)

    @Query("SELECT * FROM short_term_events ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentEvents(limit: Int): Flow<List<ShortTermEventEntity>>

    @Query("DELETE FROM short_term_events WHERE timestamp < :cutoff")
    suspend fun deleteOlderThan(cutoff: Long)

    @Query("SELECT COUNT(*) FROM short_term_events")
    suspend fun getCount(): Int
}
