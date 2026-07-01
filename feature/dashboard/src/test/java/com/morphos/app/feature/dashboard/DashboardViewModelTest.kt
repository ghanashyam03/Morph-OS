package com.morphos.app.feature.dashboard

import app.cash.turbine.test
import com.morphos.app.core.common.AppDispatchers
import com.morphos.app.core.common.AppResult
import com.morphos.app.core.common.NoParams
import com.morphos.app.core.domain.agent.AgentOrchestrator
import com.morphos.app.core.domain.model.*
import com.morphos.app.core.domain.usecase.widget.DeleteWidgetUseCase
import com.morphos.app.core.domain.usecase.widget.GetAllWidgetsUseCase
import com.morphos.app.core.domain.usecase.widget.RecordUserEventUseCase
import com.morphos.app.core.widget.GlanceWidgetRenderer
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
class DashboardViewModelTest {

    @MockK
    lateinit var getAllWidgetsUseCase: GetAllWidgetsUseCase

    @MockK
    lateinit var deleteWidgetUseCase: DeleteWidgetUseCase

    @MockK
    lateinit var recordUserEventUseCase: RecordUserEventUseCase

    @MockK
    lateinit var agentOrchestrator: AgentOrchestrator

    @MockK
    lateinit var glanceWidgetRenderer: GlanceWidgetRenderer

    @MockK
    lateinit var connectivityObserver: com.morphos.app.core.common.ConnectivityObserver

    private lateinit var viewModel: DashboardViewModel
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testDispatchers = AppDispatchers(
        main = testDispatcher,
        io = testDispatcher,
        default = testDispatcher,
        unconfined = testDispatcher
    )

    private val dummyContext = ContextSnapshot(0, 0L, 80, "WiFi", 0.0, 0.0, false)
    private val contextFlow = MutableStateFlow(dummyContext)

    @BeforeEach
    fun setUp() {
        every { agentOrchestrator.getContextFlow() } returns contextFlow
        every { agentOrchestrator.getNotificationFlow() } returns flowOf(emptyList())
        every { connectivityObserver.observe() } returns flowOf(com.morphos.app.core.common.ConnectivityObserver.Status.Available)
    }

    private fun initViewModel() {
        viewModel = DashboardViewModel(
            getAllWidgetsUseCase,
            deleteWidgetUseCase,
            recordUserEventUseCase,
            agentOrchestrator,
            glanceWidgetRenderer,
            connectivityObserver,
            testDispatchers
        )
    }

    private fun createDummyWidget(id: String, name: String): WidgetConfig {
        return WidgetConfig(
            id = id,
            name = name,
            description = "Desc",
            templateId = "TPL_CARD_SINGLE",
            sizeClass = WidgetSizeClass.MEDIUM,
            slots = emptyMap(),
            dataBindings = emptyList(),
            refreshPolicy = RefreshPolicy(emptyList()),
            priorityWeights = PriorityWeights(),
            createdAt = System.currentTimeMillis(),
            lastModified = System.currentTimeMillis()
        )
    }

    @Test
    fun init_loadWidgets_stateUpdated() = runTest {
        val widgets = listOf(createDummyWidget("1", "Widget 1"))
        every { getAllWidgetsUseCase(NoParams) } returns flowOf(AppResult.Success(widgets))

        initViewModel()

        assertEquals(widgets, viewModel.state.value.widgets)
        assertEquals(false, viewModel.state.value.isLoading)
        assertEquals(false, viewModel.state.value.showEmptyState)
    }

    @Test
    fun deleteWidget_callsUseCase() = runTest {
        every { getAllWidgetsUseCase(NoParams) } returns flowOf(AppResult.Success(emptyList()))
        coEvery { deleteWidgetUseCase(any()) } returns AppResult.Success(Unit)

        initViewModel()
        viewModel.processIntent(DashboardIntent.DeleteWidget("1"))

        coVerify(exactly = 1) { deleteWidgetUseCase("1") }
    }

    @Test
    fun recordWidgetTap_callsRecordEvent() = runTest {
        every { getAllWidgetsUseCase(NoParams) } returns flowOf(AppResult.Success(emptyList()))
        coEvery { recordUserEventUseCase(any()) } returns AppResult.Success(Unit)

        initViewModel()
        viewModel.processIntent(DashboardIntent.RecordWidgetTap("widget_1"))

        coVerify(exactly = 1) { recordUserEventUseCase(any()) }
    }

    @Test
    fun navigateToCreator_emitsEffect() = runTest {
        every { getAllWidgetsUseCase(NoParams) } returns flowOf(AppResult.Success(emptyList()))

        initViewModel()

        viewModel.effects.test {
            viewModel.processIntent(DashboardIntent.NavigateToWidgetCreator)
            val effect = awaitItem()
            assertTrue(effect is DashboardEffect.NavigateToWidgetCreator)
        }
    }

    @Test
    fun contextUpdate_stateReflectsContext() = runTest {
        every { getAllWidgetsUseCase(NoParams) } returns flowOf(AppResult.Success(emptyList()))

        initViewModel()

        val updatedContext = dummyContext.copy(batteryLevel = 45, isCharging = true)
        contextFlow.value = updatedContext

        assertEquals(updatedContext, viewModel.state.value.contextSnapshot)
    }

    @Test
    fun error_stateHasErrorMessage() = runTest {
        every { getAllWidgetsUseCase(NoParams) } returns flowOf(AppResult.Error(Exception("Load error")))

        initViewModel()

        assertEquals("Load error", viewModel.state.value.error)
        assertEquals(false, viewModel.state.value.isLoading)
    }
}
