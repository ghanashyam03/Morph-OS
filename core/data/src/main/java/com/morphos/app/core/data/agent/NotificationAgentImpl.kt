package com.morphos.app.core.data.agent

import com.morphos.app.core.domain.agent.NotificationAgent
import com.morphos.app.core.domain.model.ContextSnapshot
import com.morphos.app.core.domain.model.PrioritizedNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationAgentImpl @Inject constructor() : NotificationAgent {
    private val _activeNotifications = MutableStateFlow<List<PrioritizedNotification>>(emptyList())
    override val activeNotifications: StateFlow<List<PrioritizedNotification>> = _activeNotifications

    override suspend fun processNotificationEvent(event: String, context: ContextSnapshot) {
        TODO("Not yet implemented")
    }
}
