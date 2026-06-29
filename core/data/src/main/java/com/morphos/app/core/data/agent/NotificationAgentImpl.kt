package com.morphos.app.core.data.agent

import com.morphos.app.core.common.AppDispatchers
import com.morphos.app.core.domain.agent.NotificationAgent
import com.morphos.app.core.domain.model.NotificationPriority
import com.morphos.app.core.domain.model.PrioritizedNotification
import com.morphos.app.core.domain.repository.NotificationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationAgentImpl @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val dispatchers: AppDispatchers
) : NotificationAgent {

    private val scope = CoroutineScope(SupervisorJob() + dispatchers.default)
    private val _notifications = MutableStateFlow<List<PrioritizedNotification>>(emptyList())
    private var job: Job? = null

    override fun start() {
        if (job != null) return

        job = scope.launch {
            notificationRepository.getPrioritizedNotifications().collectLatest { list ->
                val calendar = Calendar.getInstance()
                val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
                val isActiveHour = currentHour in 8..22

                val processed = list.map { notif ->
                    val titleText = notif.title?.lowercase() ?: ""
                    val bodyText = notif.text?.lowercase() ?: ""

                    val newPriority = when {
                        // Spam heuristic
                        titleText.contains("promo") || titleText.contains("offer") || titleText.contains("deal") || titleText.contains("% off") ||
                                bodyText.contains("promo") || bodyText.contains("offer") || bodyText.contains("deal") || bodyText.contains("% off") -> {
                            NotificationPriority.SUPPRESSED
                        }
                        // System notification
                        notif.packageName.startsWith("com.android") || notif.packageName.startsWith("com.google.android.gms") -> {
                            NotificationPriority.LOW
                        }
                        // Social apps
                        isSocialApp(notif.packageName) -> {
                            if (isActiveHour) NotificationPriority.HIGH else NotificationPriority.MEDIUM
                        }
                        else -> notif.priority
                    }
                    notif.copy(priority = newPriority)
                }
                _notifications.value = processed.filter { it.priority != NotificationPriority.SUPPRESSED }
            }
        }
    }

    override fun stop() {
        job?.cancel()
        job = null
    }

    override fun getPrioritizedNotifications(): Flow<List<PrioritizedNotification>> {
        return _notifications.asStateFlow()
    }

    private fun isSocialApp(packageName: String): Boolean {
        val lower = packageName.lowercase()
        return lower.contains("whatsapp") || lower.contains("telegram") ||
                lower.contains("signal") || lower.contains("gmail") ||
                lower.contains("twitter") || lower.contains("instagram") ||
                lower.contains("facebook") || lower.contains("messenger")
    }
}
