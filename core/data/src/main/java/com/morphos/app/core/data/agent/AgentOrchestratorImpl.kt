package com.morphos.app.core.data.agent

import com.morphos.app.core.common.AppDispatchers
import com.morphos.app.core.common.AppResult
import com.morphos.app.core.common.safeCall
import com.morphos.app.core.domain.agent.*
import com.morphos.app.core.domain.model.*
import com.morphos.app.core.domain.repository.MemoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AgentOrchestratorImpl @Inject constructor(
    private val contextAgent: ContextAgent,
    private val intentAgent: IntentAgent,
    private val planningAgent: PlanningAgent,
    private val memoryAgent: MemoryAgent,
    private val rankingAgent: RankingAgent,
    private val notificationAgent: NotificationAgent,
    private val memoryRepository: MemoryRepository,
    private val dispatchers: AppDispatchers
) : AgentOrchestrator {

    private var orchestratorScope: CoroutineScope? = null
    val eventBus = MutableSharedFlow<AgentEvent>(replay = 0, extraBufferCapacity = 64)

    override fun start() {
        if (orchestratorScope != null) return
        val scope = CoroutineScope(SupervisorJob() + dispatchers.default)
        orchestratorScope = scope

        contextAgent.start()
        notificationAgent.start()

        // Collect contextFlow -> emit ContextUpdated
        scope.launch {
            contextAgent.contextFlow.collect { snapshot ->
                eventBus.emit(AgentEvent.ContextUpdated(snapshot))
            }
        }

        // Collect eventBus -> handle/log each event
        scope.launch {
            eventBus.collect { event ->
                logEvent(event)
            }
        }
    }

    override fun stop() {
        contextAgent.stop()
        notificationAgent.stop()
        orchestratorScope?.cancel()
        orchestratorScope = null
    }

    override suspend fun handleUserInput(input: String): AppResult<WidgetPlan> {
        val scope = orchestratorScope ?: return AppResult.Error(IllegalStateException("Orchestrator not started"))
        return try {
            val context = contextAgent.contextFlow.value
            val intentResult = intentAgent.parseIntent(input, context)
            if (intentResult is AppResult.Error) return AppResult.Error(intentResult.exception)
            val intent = (intentResult as AppResult.Success).data

            scope.launch {
                eventBus.emit(AgentEvent.IntentParsed(intent))
            }

            val profile = memoryRepository.getMemoryProfile()
            val planResult = planningAgent.generatePlan(intent, context, profile)
            if (planResult is AppResult.Error) return AppResult.Error(planResult.exception)
            val plan = (planResult as AppResult.Success).data

            val requestId = UUID.randomUUID().toString()
            scope.launch {
                eventBus.emit(AgentEvent.PlanGenerated(plan, requestId))
            }

            // Record submit event to memory
            val shortTermEvent = ShortTermEvent(
                id = UUID.randomUUID().toString(),
                eventType = ShortTermEventType.NL_INPUT_SUBMITTED,
                widgetId = null,
                metadata = mapOf("input" to input),
                timestamp = System.currentTimeMillis()
            )
            memoryAgent.recordEvent(shortTermEvent)

            AppResult.Success(plan)
        } catch (e: Exception) {
            AppResult.Error(e)
        }
    }

    override fun getContextFlow(): StateFlow<ContextSnapshot> {
        return contextAgent.contextFlow
    }

    override fun getNotificationFlow(): Flow<List<PrioritizedNotification>> {
        return notificationAgent.getPrioritizedNotifications()
    }

    override suspend fun triggerMemorySummarization(): AppResult<Unit> {
        val scope = orchestratorScope ?: return AppResult.Error(IllegalStateException("Orchestrator not started"))
        return try {
            val result = memoryAgent.summarizeRecentMemory()
            if (result is AppResult.Error) return AppResult.Error(result.exception)
            val profile = (result as AppResult.Success).data
            scope.launch {
                eventBus.emit(AgentEvent.MemoryUpdated(profile))
            }
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e)
        }
    }

    override suspend fun triggerWidgetRanking(
        widgetId: String,
        candidates: List<ContentCandidate>
    ): AppResult<List<RankedContent>> {
        val scope = orchestratorScope ?: return AppResult.Error(IllegalStateException("Orchestrator not started"))
        return try {
            val context = contextAgent.contextFlow.value
            val profile = memoryRepository.getMemoryProfile()
            val ranked = rankingAgent.rankContent(candidates, context, profile)
            scope.launch {
                eventBus.emit(AgentEvent.RankingCompleted(ranked, widgetId))
            }
            AppResult.Success(ranked)
        } catch (e: Exception) {
            AppResult.Error(e)
        }
    }

    private fun logEvent(event: AgentEvent) {
        when (event) {
            is AgentEvent.ContextUpdated -> Timber.d("AgentOrchestrator: Context updated.")
            is AgentEvent.IntentParsed -> Timber.d("AgentOrchestrator: Intent parsed: ${event.intent.intentType}")
            is AgentEvent.PlanGenerated -> Timber.d("AgentOrchestrator: Plan generated: ${event.plan.suggestedName}")
            is AgentEvent.MemoryUpdated -> Timber.d("AgentOrchestrator: Memory Profile updated.")
            is AgentEvent.NotificationReceived -> Timber.d("AgentOrchestrator: Notification received from ${event.notification.appName}")
            is AgentEvent.RankingCompleted -> Timber.d("AgentOrchestrator: Ranking completed for widget ${event.widgetId}")
            is AgentEvent.AgentFailed -> Timber.e(event.error, "AgentOrchestrator: Agent ${event.agentId} failed on task ${event.taskId}")
        }
    }
}
