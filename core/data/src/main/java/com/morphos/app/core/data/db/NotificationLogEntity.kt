package com.morphos.app.core.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "notification_log",
    indices = [Index("postedAt")]
)
data class NotificationLogEntity(
    @PrimaryKey val id: String,
    val packageName: String,
    val appName: String,
    val title: String?,
    val text: String?,
    val priority: String,
    val postedAt: Long,
    val isDismissed: Boolean = false
)

@Dao
interface NotificationLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: NotificationLogEntity)

    @Query("SELECT * FROM notification_log WHERE isDismissed = 0 ORDER BY postedAt DESC LIMIT 20")
    fun getActivePrioritized(): Flow<List<NotificationLogEntity>>

    @Query("UPDATE notification_log SET isDismissed = 1 WHERE id = :id")
    suspend fun dismiss(id: String)

    @Query("DELETE FROM notification_log WHERE postedAt < :cutoff")
    suspend fun pruneOld(cutoff: Long)
}
