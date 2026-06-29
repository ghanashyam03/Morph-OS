package com.morphos.app.core.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.OnConflictStrategy

@Entity(tableName = "agent_task_queue")
data class AgentTaskEntity(
    @PrimaryKey val taskId: String,
    val agentName: String,
    val description: String,
    val status: String,
    val retryCount: Int,
    val createdAt: Long
)

@Dao
interface AgentTaskDao {
    @Query("SELECT * FROM agent_task_queue WHERE status = :status")
    suspend fun getTasksByStatus(status: String): List<AgentTaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: AgentTaskEntity)

    @Query("DELETE FROM agent_task_queue WHERE taskId = :taskId")
    suspend fun deleteTask(taskId: String)
}
