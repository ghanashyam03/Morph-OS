package com.morphos.app.core.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "notification_log")
data class NotificationLogEntity(
    @PrimaryKey val key: String,
    val packageName: String,
    val title: String,
    val text: String,
    val postTime: Long,
    val priorityScore: Float,
    val isSilent: Boolean
)

@Dao
interface NotificationLogDao {
    @Query("SELECT * FROM notification_log ORDER BY postTime DESC")
    fun getNotifications(): Flow<List<NotificationLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationLogEntity)

    @Query("DELETE FROM notification_log")
    suspend fun clearNotifications()
}
