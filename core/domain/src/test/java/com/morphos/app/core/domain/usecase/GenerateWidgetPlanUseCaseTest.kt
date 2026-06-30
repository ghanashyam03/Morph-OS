package com.morphos.app.core.domain.usecase.widget

import com.morphos.app.core.common.AppResult
import com.morphos.app.core.domain.agent.PlanningAgent
import com.morphos.app.core.domain.model.*
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockKExtension::class)
class GenerateWidgetPlanUseCaseTest {

    @MockK
    lateinit var planningAgent: PlanningAgent

    private lateinit var generateWidgetPlanUseCase: GenerateWidgetPlanUseCase
    private val dummyContext = ContextSnapshot(0, 0L, 80, "WiFi", 0.0, 0.0, false)
    private val dummyProfile = MemoryProfile(emptyMap(), emptyList(), emptyMap())

    @BeforeEach
    fun setUp() {
        generateWidgetPlanUseCase = GenerateWidgetPlanUseCase(planningAgent)
    }

    private fun createDummyPlan(templateId: String): WidgetPlan {
        return WidgetPlan(
            selectedTemplateId = templateId,
            suggestedName = "Suggestion",
            suggestedDescription = "Desc",
            planConfidence = 0.8f,
            slotAssignments = emptyMap(),
            pluginConfigs = emptyMap(),
            suggestedRefreshPolicy = RefreshPolicy(emptyList()),
            suggestedPriorityWeights = PriorityWeights()
        )
    }

    @Test
    fun givenKnownIntent_whenGenerated_thenReturnsRuleBasedPlan() = runTest {
        val intent = WidgetIntent(IntentType.WEATHER, 0.9f, false)
        val params = GeneratePlanParams(intent, dummyContext, dummyProfile)
        val expectedPlan = createDummyPlan("TPL_WEATHER_FOCUS")

        coEvery { planningAgent.generatePlan(any(), any(), any()) } returns AppResult.Success(expectedPlan)

        val result = generateWidgetPlanUseCase(params)

        assertTrue(result is AppResult.Success)
        assertEquals(expectedPlan, (result as AppResult.Success).data)
    }

    @Test
    fun givenUnknownIntent_whenGenerated_thenDelegatesToAI() = runTest {
        val intent = WidgetIntent(IntentType.UNKNOWN, 0.5f, true)
        val params = GeneratePlanParams(intent, dummyContext, dummyProfile)
        val expectedPlan = createDummyPlan("TPL_CARD_SINGLE")

        coEvery { planningAgent.generatePlan(any(), any(), any()) } returns AppResult.Success(expectedPlan)

        val result = generateWidgetPlanUseCase(params)

        assertTrue(result is AppResult.Success)
        assertEquals(expectedPlan, (result as AppResult.Success).data)
    }
}
