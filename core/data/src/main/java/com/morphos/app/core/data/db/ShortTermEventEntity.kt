package com.morphos.app.core.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "short_term_events")
data class ShortTermEventEntity(
    @PrimaryKey val id: String,
    val timestamp: Long,
    val type: String,
    val widgetId: String?,
    val metadataJson: String
)

@Dao
interface ShortTermEventDao {
    @Query("SELECT * FROM short_term_events ORDER BY timestamp DESC")
    fun getEvents(): Flow<List<ShortTermEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: ShortTermEventEntity)

    @Query("DELETE FROM short_term_events WHERE timestamp < :threshold")
    suspend fun pruneEvents(threshold: Long)
}
