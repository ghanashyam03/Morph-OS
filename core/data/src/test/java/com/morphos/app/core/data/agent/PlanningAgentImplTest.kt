package com.morphos.app.core.data.agent

import com.morphos.app.core.ai.AIEngineManager
import com.morphos.app.core.common.AppDispatchers
import com.morphos.app.core.common.AppResult
import com.morphos.app.core.domain.model.*
import com.morphos.app.core.domain.repository.PluginRepository
import com.morphos.app.core.domain.repository.SettingsRepository
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockKExtension::class)
class PlanningAgentImplTest {

    @MockK
    lateinit var aiEngineManager: AIEngineManager

    @MockK
    lateinit var pluginRepository: PluginRepository

    @MockK
    lateinit var settingsRepository: SettingsRepository

    private lateinit var agent: PlanningAgentImpl
    private val dummyContext = ContextSnapshot(0, 0L, 80, "WiFi", 0.0, 0.0, false)
    private val dummyProfile = MemoryProfile(emptyMap(), emptyList(), emptyMap())
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testDispatchers = AppDispatchers(
        main = testDispatcher,
        io = testDispatcher,
        default = testDispatcher,
        unconfined = testDispatcher
    )

    @BeforeEach
    fun setUp() {
        agent = PlanningAgentImpl(aiEngineManager, pluginRepository, settingsRepository, testDispatchers)
        coEvery { settingsRepository.isCloudAiEnabled() } returns false
    }

    @Test
    fun studyIntent_returnsTimelineTemplate() = runTest {
        val intent = WidgetIntent("raw", IntentType.CREATE_STUDY_WIDGET, emptyMap(), 0.9f, false)

        val result = agent.generatePlan(intent, dummyContext, dummyProfile)

        assertTrue(result is AppResult.Success)
        val plan = (result as AppResult.Success).data
        assertEquals("TPL_TIMELINE", plan.selectedTemplateId)
        assertEquals(PlanSource.RULE_BASED_FALLBACK, plan.planSource)
    }

    @Test
    fun weatherIntent_returnsWeatherFocusTemplate() = runTest {
        val intent = WidgetIntent("raw", IntentType.CREATE_WEATHER_WIDGET, emptyMap(), 0.9f, false)

        val result = agent.generatePlan(intent, dummyContext, dummyProfile)

        assertTrue(result is AppResult.Success)
        val plan = (result as AppResult.Success).data
        assertEquals("TPL_WEATHER_FOCUS", plan.selectedTemplateId)
        assertEquals(PlanSource.RULE_BASED_FALLBACK, plan.planSource)
    }

    @Test
    fun customIntent_withTier1Available_usesAIPlanner() = runTest {
        val intent = WidgetIntent("raw", IntentType.CREATE_CUSTOM_WIDGET, emptyMap(), 0.9f, false)
        val jsonMockResponse = """
            {
              "widgetIntentRaw": "raw",
              "selectedTemplateId": "TPL_GRID_2X2",
              "suggestedName": "AI Custom Widget",
              "suggestedDescription": "Description",
              "slotAssignments": {},
              "pluginConfigs": {},
              "suggestedRefreshPolicy": { "type": "PERIODIC", "minIntervalSeconds": 1800 },
              "suggestedPriorityWeights": { "recencyWeight": 0.3, "engagementWeight": 0.4, "contextRelevanceWeight": 0.3 },
              "planConfidence": 0.9,
              "planSource": "TIER_1_LOCAL"
            }
        """.trimIndent()

        every { pluginRepository.getAvailablePlugins() } returns emptyList()
        every { aiEngineManager.isTier1Available() } returns true
        coEvery { aiEngineManager.inferTier1(any()) } returns jsonMockResponse

        val result = agent.generatePlan(intent, dummyContext, dummyProfile)

        assertTrue(result is AppResult.Success)
        val plan = (result as AppResult.Success).data
        assertEquals("TPL_GRID_2X2", plan.selectedTemplateId)
        assertEquals(PlanSource.TIER_1_LOCAL, plan.planSource)
    }

    @Test
    fun customIntent_withNoAI_usesRuleBasedFallback() = runTest {
        val intent = WidgetIntent("raw", IntentType.CREATE_CUSTOM_WIDGET, emptyMap(), 0.9f, false)

        every { pluginRepository.getAvailablePlugins() } returns emptyList()
        every { aiEngineManager.isTier1Available() } returns false

        val result = agent.generatePlan(intent, dummyContext, dummyProfile)

        assertTrue(result is AppResult.Success)
        val plan = (result as AppResult.Success).data
        assertEquals(PlanSource.RULE_BASED_FALLBACK, plan.planSource)
    }

    @Test
    fun malformedAIResponse_fallsBackToRuleBased() = runTest {
        val intent = WidgetIntent("raw", IntentType.CREATE_CUSTOM_WIDGET, emptyMap(), 0.9f, false)

        every { pluginRepository.getAvailablePlugins() } returns emptyList()
        every { aiEngineManager.isTier1Available() } returns true
        coEvery { aiEngineManager.inferTier1(any()) } returns "malformed non-json garbage response"

        val result = agent.generatePlan(intent, dummyContext, dummyProfile)

        assertTrue(result is AppResult.Success)
        val plan = (result as AppResult.Success).data
        assertEquals(PlanSource.RULE_BASED_FALLBACK, plan.planSource)
    }
}
