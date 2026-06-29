package com.morphos.app.core.domain.repository

import com.morphos.app.core.domain.model.PrioritizedNotification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getNotifications(): Flow<List<PrioritizedNotification>>
    suspend fun saveNotification(notification: PrioritizedNotification)
    suspend fun clearNotifications()
}
