package com.morphos.app.core.domain.agent

import com.morphos.app.core.domain.model.ContextSnapshot
import com.morphos.app.core.domain.model.PrioritizedNotification
import kotlinx.coroutines.flow.StateFlow

interface NotificationAgent {
    val activeNotifications: StateFlow<List<PrioritizedNotification>>
    suspend fun processNotificationEvent(event: String, context: ContextSnapshot)
}
