package com.morphos.app.core.data

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.morphos.app.core.common.AppDispatchers
import com.morphos.app.core.data.agent.AgentOrchestratorImpl
import com.morphos.app.core.domain.agent.AgentOrchestrator
import com.morphos.app.core.domain.model.AgentEvent
import com.morphos.app.core.domain.model.NotificationPriority
import com.morphos.app.core.domain.model.PrioritizedNotification
import com.morphos.app.core.domain.repository.NotificationRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MorphOsNotificationListenerService : NotificationListenerService() {

    @Inject
    lateinit var notificationRepository: NotificationRepository

    @Inject
    lateinit var agentOrchestrator: AgentOrchestrator

    @Inject
    lateinit var dispatchers: AppDispatchers

    private var scope: CoroutineScope? = null

    override fun onCreate() {
        super.onCreate()
        scope = CoroutineScope(SupervisorJob() + dispatchers.default)
    }

    override fun onDestroy() {
        super.onDestroy()
        scope?.cancel()
        scope = null
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return
        val currentScope = scope ?: return

        currentScope.launch {
            val appLabel = try {
                val pm = packageManager
                val ai = pm.getApplicationInfo(sbn.packageName, 0)
                pm.getApplicationLabel(ai).toString()
            } catch (e: Exception) {
                sbn.packageName
            }

            val extras = sbn.notification.extras
            val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
            val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()

            val prioritized = PrioritizedNotification(
                id = sbn.key,
                packageName = sbn.packageName,
                appName = appLabel,
                title = title,
                text = text,
                priority = NotificationPriority.MEDIUM,
                postedAt = sbn.postTime
            )

            notificationRepository.logNotification(prioritized)
            
            // Emit to Orchestrator event bus
            (agentOrchestrator as? AgentOrchestratorImpl)?.eventBus?.emit(
                AgentEvent.NotificationReceived(prioritized)
            )
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        if (sbn == null) return
        val currentScope = scope ?: return

        currentScope.launch {
            notificationRepository.dismissNotification(sbn.key)
        }
    }
}
