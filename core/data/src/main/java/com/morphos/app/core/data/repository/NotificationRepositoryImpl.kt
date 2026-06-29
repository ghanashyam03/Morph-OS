package com.morphos.app.core.data.repository

import com.morphos.app.core.domain.model.PrioritizedNotification
import com.morphos.app.core.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor() : NotificationRepository {
    override fun getNotifications(): Flow<List<PrioritizedNotification>> {
        TODO("Not yet implemented")
    }

    override suspend fun saveNotification(notification: PrioritizedNotification) {
        TODO("Not yet implemented")
    }

    override suspend fun clearNotifications() {
        TODO("Not yet implemented")
    }
}
