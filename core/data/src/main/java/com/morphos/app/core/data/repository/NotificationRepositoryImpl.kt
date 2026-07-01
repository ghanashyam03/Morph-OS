package com.morphos.app.core.data.repository

import com.morphos.app.core.common.AppDispatchers
import com.morphos.app.core.common.AppResult
import com.morphos.app.core.data.db.NotificationLogDao
import com.morphos.app.core.data.mapper.toDomain
import com.morphos.app.core.data.mapper.toEntity
import com.morphos.app.core.domain.model.PrioritizedNotification
import com.morphos.app.core.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val notificationLogDao: NotificationLogDao,
    private val dispatchers: AppDispatchers
) : NotificationRepository {

    override fun getPrioritizedNotifications(): Flow<List<PrioritizedNotification>> {
        return notificationLogDao.getActivePrioritized().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun logNotification(notification: PrioritizedNotification): AppResult<Unit> = withContext(dispatchers.io) {
        try {
            notificationLogDao.insert(notification.toEntity())
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e)
        }
    }

    override suspend fun dismissNotification(id: String): AppResult<Unit> = withContext(dispatchers.io) {
        try {
            notificationLogDao.dismiss(id)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e)
        }
    }
}
