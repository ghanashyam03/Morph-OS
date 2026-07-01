package com.morphos.app.core.domain.usecase.widget

import com.morphos.app.core.common.AppResult
import com.morphos.app.core.domain.model.PriorityWeights
import com.morphos.app.core.domain.model.RefreshPolicy
import com.morphos.app.core.domain.model.WidgetConfig
import com.morphos.app.core.domain.model.WidgetPlan
import com.morphos.app.core.domain.model.WidgetSizeClass
import com.morphos.app.core.domain.repository.WidgetRepository
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockKExtension::class)
class CreateWidgetUseCaseTest {

    @MockK
    lateinit var widgetRepository: WidgetRepository

    private lateinit var createWidgetUseCase: CreateWidgetUseCase

    @BeforeEach
    fun setUp() {
        createWidgetUseCase = CreateWidgetUseCase(widgetRepository)
    }

    private fun createDummyPlan(name: String = "Test Widget"): WidgetPlan {
        return WidgetPlan(
            selectedTemplateId = "TPL_CARD_SINGLE",
            suggestedName = name,
            suggestedDescription = "Description",
            planConfidence = 0.9f,
            slotAssignments = mapOf("header" to "clock_plugin"),
            pluginConfigs = emptyMap(),
            suggestedRefreshPolicy = RefreshPolicy(emptyList()),
            suggestedPriorityWeights = PriorityWeights()
        )
    }

    @Test
    fun givenValidPlan_whenCreateCalled_thenReturnsSuccess() = runTest {
        val plan = createDummyPlan()
        val params = CreateWidgetParams(plan, WidgetSizeClass.MEDIUM)

        coEvery { widgetRepository.saveWidget(any()) } returns AppResult.Success(Unit)

        val result = createWidgetUseCase(params)

        assertTrue(result is AppResult.Success)
        val widget = (result as AppResult.Success).data
        assertEquals("Test Widget", widget.name)
        assertEquals("TPL_CARD_SINGLE", widget.templateId)
        coVerify(exactly = 1) { widgetRepository.saveWidget(any()) }
    }

    @Test
    fun givenPlanWithBlankName_whenCreateCalled_thenUsesGeneratedName() = runTest {
        val plan = createDummyPlan(name = "")
        val params = CreateWidgetParams(plan, WidgetSizeClass.MEDIUM)

        coEvery { widgetRepository.saveWidget(any()) } returns AppResult.Success(Unit)

        val result = createWidgetUseCase(params)

        assertTrue(result is AppResult.Success)
        val widget = (result as AppResult.Success).data
        assertEquals("Widget_TPL_CARD_SINGLE", widget.name)
        coVerify(exactly = 1) { widgetRepository.saveWidget(any()) }
    }

    @Test
    fun givenRepositoryError_whenCreateCalled_thenReturnsError() = runTest {
        val plan = createDummyPlan()
        val params = CreateWidgetParams(plan, WidgetSizeClass.MEDIUM)
        val errorException = Exception("DB Write Error")

        coEvery { widgetRepository.saveWidget(any()) } returns AppResult.Error(errorException)

        val result = createWidgetUseCase(params)

        assertTrue(result is AppResult.Error)
        assertEquals("DB Write Error", (result as AppResult.Error).message)
    }

    @Test
    fun givenValidPlan_whenCreateCalled_thenAssignsUUID() = runTest {
        val plan = createDummyPlan()
        val params = CreateWidgetParams(plan, WidgetSizeClass.MEDIUM)

        coEvery { widgetRepository.saveWidget(any()) } returns AppResult.Success(Unit)

        val result = createWidgetUseCase(params)

        assertTrue(result is AppResult.Success)
        val widget = (result as AppResult.Success).data
        assertNotNull(widget.id)
        assertTrue(widget.id.isNotBlank())
    }
}
