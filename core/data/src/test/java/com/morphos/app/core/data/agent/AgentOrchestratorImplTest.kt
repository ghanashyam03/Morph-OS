package com.morphos.app.core.data.agent

import com.morphos.app.core.common.AppDispatchers
import com.morphos.app.core.common.AppResult
import com.morphos.app.core.domain.agent.*
import com.morphos.app.core.domain.model.*
import com.morphos.app.core.domain.repository.MemoryRepository
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockKExtension::class)
class AgentOrchestratorImplTest {

    @MockK
    lateinit var contextAgent: ContextAgent

    @MockK
    lateinit var intentAgent: IntentAgent

    @MockK
    lateinit var planningAgent: PlanningAgent

    @MockK
    lateinit var memoryAgent: MemoryAgent

    @MockK
    lateinit var rankingAgent: RankingAgent

    @MockK
    lateinit var notificationAgent: NotificationAgent

    @MockK
    lateinit var memoryRepository: MemoryRepository

    private lateinit var orchestrator: AgentOrchestratorImpl
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testDispatchers = AppDispatchers(
        main = testDispatcher,
        io = testDispatcher,
        default = testDispatcher,
        unconfined = testDispatcher
    )

    private val dummyContext = ContextSnapshot(0, 0L, 80, "WiFi", 0.0, 0.0, false)
    private val dummyProfile = MemoryProfile(emptyMap(), emptyList(), emptyMap())

    @BeforeEach
    fun setUp() {
        val contextFlow = MutableStateFlow(dummyContext)
        every { contextAgent.contextFlow } returns contextFlow
        every { contextAgent.start() } just Runs
        every { contextAgent.stop() } just Runs
        every { notificationAgent.start() } just Runs
        every { notificationAgent.stop() } just Runs

        orchestrator = AgentOrchestratorImpl(
            contextAgent,
            intentAgent,
            planningAgent,
            memoryAgent,
            rankingAgent,
            notificationAgent,
            memoryRepository,
            testDispatchers
        )
    }

    private fun createDummyPlan(): WidgetPlan {
        return WidgetPlan(
            selectedTemplateId = "TPL_CARD_SINGLE",
            suggestedName = "Study Helper",
            suggestedDescription = "Manage focus",
            planConfidence = 0.8f,
            slotAssignments = emptyMap(),
            pluginConfigs = emptyMap(),
            suggestedRefreshPolicy = RefreshPolicy(RefreshType.PERIODIC),
            suggestedPriorityWeights = PriorityWeights()
        )
    }

    @Test
    fun handleUserInput_validInput_returnsPlan() = runTest {
        val input = "some valid request"
        val intent = WidgetIntent(input, IntentType.CREATE_STUDY_WIDGET, emptyMap(), 0.9f, false)
        val plan = createDummyPlan()

        coEvery { intentAgent.parseIntent(input, any()) } returns AppResult.Success(intent)
        coEvery { memoryRepository.getMemoryProfile() } returns dummyProfile
        coEvery { planningAgent.generatePlan(intent, any(), dummyProfile) } returns AppResult.Success(plan)
        coEvery { memoryAgent.recordEvent(any()) } returns AppResult.Success(Unit)

        orchestrator.start()
        val result = orchestrator.handleUserInput(input)

        assertTrue(result is AppResult.Success)
        assertEquals(plan, (result as AppResult.Success).data)
        orchestrator.stop()
    }

    @Test
    fun handleUserInput_blankInput_returnsError() = runTest {
        // Orchestrator starts, passes to intent parser
        val input = ""
        val errorException = IllegalArgumentException("input blank")

        coEvery { intentAgent.parseIntent(input, any()) } returns AppResult.Error(errorException)

        orchestrator.start()
        val result = orchestrator.handleUserInput(input)

        assertTrue(result is AppResult.Error)
        assertEquals("input blank", (result as AppResult.Error).message)
        orchestrator.stop()
    }

    @Test
    fun handleUserInput_agentFailure_returnsError() = runTest {
        val input = "study gym"
        val intent = WidgetIntent(input, IntentType.CREATE_STUDY_WIDGET, emptyMap(), 0.9f, false)
        val errorException = Exception("Planning failed")

        coEvery { intentAgent.parseIntent(input, any()) } returns AppResult.Success(intent)
        coEvery { memoryRepository.getMemoryProfile() } returns dummyProfile
        coEvery { planningAgent.generatePlan(intent, any(), dummyProfile) } returns AppResult.Error(errorException)

        orchestrator.start()
        val result = orchestrator.handleUserInput(input)

        assertTrue(result is AppResult.Error)
        assertEquals("Planning failed", (result as AppResult.Error).message)
        orchestrator.stop()
    }

    @Test
    fun getContextFlow_delegatesToContextAgent() {
        val flow = orchestrator.getContextFlow()
        assertEquals(dummyContext, flow.value)
    }

    @Test
    fun getNotificationFlow_delegatesToNotificationAgent() {
        val notifs = listOf(
            PrioritizedNotification("1", "AppName", "Title", "Text", 0L, 0.8f, PriorityClass.HIGH)
        )
        every { notificationAgent.getPrioritizedNotifications() } returns flowOf(notifs)

        val flow = orchestrator.getNotificationFlow()
        // verify flow emits correctly
        runTest {
            flow.collect { list ->
                assertEquals(1, list.size)
                assertEquals("1", list[0].id)
            }
        }
    }
}
