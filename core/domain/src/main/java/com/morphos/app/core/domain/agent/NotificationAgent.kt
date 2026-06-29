package com.morphos.app.core.domain.agent

import com.morphos.app.core.domain.model.PrioritizedNotification
import kotlinx.coroutines.flow.Flow

interface NotificationAgent {
    fun start()
    fun stop()
    fun getPrioritizedNotifications(): Flow<List<PrioritizedNotification>>
}
