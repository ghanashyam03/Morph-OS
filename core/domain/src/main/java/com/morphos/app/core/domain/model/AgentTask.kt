package com.morphos.app.core.domain.model

enum class AgentTaskStatus {
    PENDING, RUNNING, COMPLETED, FAILED
}

data class AgentTask(
    val taskId: String,
    val agentName: String,
    val description: String,
    val status: AgentTaskStatus,
    val retryCount: Int,
    val createdAtEpochMilli: Long
)
