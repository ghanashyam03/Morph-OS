package com.morphos.app.core.domain.repository

import com.morphos.app.core.common.AppResult
import com.morphos.app.core.domain.model.PrioritizedNotification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getPrioritizedNotifications(): Flow<List<PrioritizedNotification>>
    suspend fun logNotification(notification: PrioritizedNotification): AppResult<Unit>
    suspend fun dismissNotification(id: String): AppResult<Unit>
}
