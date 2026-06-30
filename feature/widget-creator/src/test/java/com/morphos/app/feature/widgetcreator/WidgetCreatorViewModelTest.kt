package com.morphos.app.feature.widgetcreator

import app.cash.turbine.test
import com.morphos.app.core.common.AppDispatchers
import com.morphos.app.core.common.AppResult
import com.morphos.app.core.domain.agent.AgentOrchestrator
import com.morphos.app.core.domain.model.*
import com.morphos.app.core.domain.usecase.widget.CreateWidgetUseCase
import com.morphos.app.core.domain.usecase.widget.GenerateWidgetPlanUseCase
import com.morphos.app.core.domain.usecase.widget.ParseUserIntentUseCase
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
class WidgetCreatorViewModelTest {

    @MockK
    lateinit var parseUserIntentUseCase: ParseUserIntentUseCase

    @MockK
    lateinit var generateWidgetPlanUseCase: GenerateWidgetPlanUseCase

    @MockK
    lateinit var createWidgetUseCase: CreateWidgetUseCase

    @MockK
    lateinit var agentOrchestrator: AgentOrchestrator

    private lateinit var viewModel: WidgetCreatorViewModel
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testDispatchers = AppDispatchers(
        main = testDispatcher,
        io = testDispatcher,
        default = testDispatcher,
        unconfined = testDispatcher
    )

    @BeforeEach
    fun setUp() {
        viewModel = WidgetCreatorViewModel(
            parseUserIntentUseCase,
            generateWidgetPlanUseCase,
            createWidgetUseCase,
            agentOrchestrator,
            testDispatchers
        )
    }

    private fun createDummyPlan(confidence: Float): WidgetPlan {
        return WidgetPlan(
            selectedTemplateId = "TPL_CARD_SINGLE",
            suggestedName = "Study Desk",
            suggestedDescription = "Description",
            planConfidence = confidence,
            slotAssignments = emptyMap(),
            pluginConfigs = emptyMap(),
            suggestedRefreshPolicy = RefreshPolicy(emptyList()),
            suggestedPriorityWeights = PriorityWeights()
        )
    }

    @Test
    fun blankInput_submitInput_setsError() = runTest {
        viewModel.processIntent(WidgetCreatorIntent.InputChanged(""))
        viewModel.processIntent(WidgetCreatorIntent.SubmitInput)

        assertEquals("Please describe the widget you want", viewModel.state.value.error)
        assertEquals(CreatorStep.NL_INPUT, viewModel.state.value.step)
    }

    @Test
    fun validInput_submitInput_callsOrchestrator() = runTest {
        viewModel.processIntent(WidgetCreatorIntent.InputChanged("study dashboard"))
        val plan = createDummyPlan(0.9f)
        coEvery { agentOrchestrator.handleUserInput("study dashboard") } returns AppResult.Success(plan)

        viewModel.processIntent(WidgetCreatorIntent.SubmitInput)

        coVerify(exactly = 1) { agentOrchestrator.handleUserInput("study dashboard") }
    }

    @Test
    fun orchestratorSuccess_highConfidence_goesToPreview() = runTest {
        viewModel.processIntent(WidgetCreatorIntent.InputChanged("study dashboard"))
        val plan = createDummyPlan(0.9f) // High confidence (>0.75)
        coEvery { agentOrchestrator.handleUserInput("study dashboard") } returns AppResult.Success(plan)

        viewModel.processIntent(WidgetCreatorIntent.SubmitInput)

        assertEquals(CreatorStep.PREVIEW, viewModel.state.value.step)
        assertEquals("Study Desk", viewModel.state.value.widgetName)
    }

    @Test
    fun orchestratorSuccess_lowConfidence_goesToTemplateSelection() = runTest {
        viewModel.processIntent(WidgetCreatorIntent.InputChanged("some vague plan"))
        val plan = createDummyPlan(0.5f) // Low confidence
        coEvery { agentOrchestrator.handleUserInput("some vague plan") } returns AppResult.Success(plan)

        viewModel.processIntent(WidgetCreatorIntent.SubmitInput)

        assertEquals(CreatorStep.TEMPLATE_SELECTION, viewModel.state.value.step)
    }

    @Test
    fun orchestratorFailure_showsError() = runTest {
        viewModel.processIntent(WidgetCreatorIntent.InputChanged("vague"))
        coEvery { agentOrchestrator.handleUserInput("vague") } returns AppResult.Error(Exception("Failed parsing"))

        viewModel.processIntent(WidgetCreatorIntent.SubmitInput)

        assertEquals(CreatorStep.NL_INPUT, viewModel.state.value.step)
        assertEquals("Failed parsing", viewModel.state.value.error)
    }

    @Test
    fun confirmWidget_callsCreateUseCase() = runTest {
        val plan = createDummyPlan(0.9f)
        viewModel.processIntent(WidgetCreatorIntent.InputChanged("study dashboard"))
        coEvery { agentOrchestrator.handleUserInput("study dashboard") } returns AppResult.Success(plan)
        viewModel.processIntent(WidgetCreatorIntent.SubmitInput) // Sets plan

        val dummyWidget = WidgetConfig(
            "1", "Study Desk", "Desc", "TPL_CARD_SINGLE", WidgetSizeClass.MEDIUM,
            emptyMap(), emptyList(), RefreshPolicy(emptyList()), PriorityWeights(), 0L, 0L
        )
        coEvery { createWidgetUseCase(any()) } returns AppResult.Success(dummyWidget)

        viewModel.processIntent(WidgetCreatorIntent.ConfirmWidget)

        coVerify(exactly = 1) { createWidgetUseCase(any()) }
        assertEquals(CreatorStep.DONE, viewModel.state.value.step)
    }

    @Test
    fun back_fromPreview_goesToTemplateSelection() = runTest {
        // Mock low confidence so back from PREVIEW returns to TEMPLATE_SELECTION
        viewModel.processIntent(WidgetCreatorIntent.InputChanged(" vague "))
        val plan = createDummyPlan(0.5f)
        coEvery { agentOrchestrator.handleUserInput(any()) } returns AppResult.Success(plan)
        viewModel.processIntent(WidgetCreatorIntent.SubmitInput) // Step moves to TEMPLATE_SELECTION

        // Select template -> goes to PREVIEW
        viewModel.processIntent(WidgetCreatorIntent.SelectTemplate("TPL_CARD_SINGLE"))
        assertEquals(CreatorStep.PREVIEW, viewModel.state.value.step)

        // Go Back
        viewModel.processIntent(WidgetCreatorIntent.Back)
        assertEquals(CreatorStep.TEMPLATE_SELECTION, viewModel.state.value.step)
    }

    @Test
    fun back_fromTemplateSelection_goesToNLInput() = runTest {
        viewModel.processIntent(WidgetCreatorIntent.InputChanged("vague"))
        val plan = createDummyPlan(0.5f)
        coEvery { agentOrchestrator.handleUserInput(any()) } returns AppResult.Success(plan)
        viewModel.processIntent(WidgetCreatorIntent.SubmitInput) // goes to TEMPLATE_SELECTION

        assertEquals(CreatorStep.TEMPLATE_SELECTION, viewModel.state.value.step)

        // Go Back
        viewModel.processIntent(WidgetCreatorIntent.Back)
        assertEquals(CreatorStep.NL_INPUT, viewModel.state.value.step)
    }
}
