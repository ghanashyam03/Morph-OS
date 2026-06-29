package com.morphos.app.core.data.db

import androidx.room.*

@Entity(
    tableName = "agent_tasks",
    indices = [Index("status"), Index("scheduledAt")]
)
data class AgentTaskEntity(
    @PrimaryKey val id: String,
    val agentId: String,
    val taskType: String,
    val paramsJson: String,
    val status: String,    // PENDING, RUNNING, COMPLETED, FAILED
    val retryCount: Int = 0,
    val maxRetries: Int = 3,
    val scheduledAt: Long,
    val completedAt: Long? = null,
    val errorMessage: String? = null
)

@Dao
interface AgentTaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: AgentTaskEntity)

    @Update
    suspend fun updateTask(task: AgentTaskEntity)

    @Query("SELECT * FROM agent_tasks WHERE status = 'PENDING' ORDER BY scheduledAt ASC")
    suspend fun getPendingTasks(): List<AgentTaskEntity>

    @Query("SELECT * FROM agent_tasks WHERE status = 'FAILED' AND retryCount < maxRetries")
    suspend fun getRetryableTasks(): List<AgentTaskEntity>

    @Query("DELETE FROM agent_tasks WHERE status = 'COMPLETED' AND completedAt < :cutoff")
    suspend fun pruneCompleted(cutoff: Long)
}
