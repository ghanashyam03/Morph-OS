package com.morphos.app.core.domain.usecase.widget

import com.morphos.app.core.common.AppResult
import com.morphos.app.core.common.NoParams
import com.morphos.app.core.domain.model.PriorityWeights
import com.morphos.app.core.domain.model.RefreshPolicy
import com.morphos.app.core.domain.model.WidgetConfig
import com.morphos.app.core.domain.model.WidgetSizeClass
import com.morphos.app.core.domain.repository.WidgetRepository
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockKExtension::class)
class GetAllWidgetsUseCaseTest {

    @MockK
    lateinit var widgetRepository: WidgetRepository

    private lateinit var getAllWidgetsUseCase: GetAllWidgetsUseCase

    @BeforeEach
    fun setUp() {
        getAllWidgetsUseCase = GetAllWidgetsUseCase(widgetRepository)
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
    fun givenWidgets_whenObserved_thenEmitsWidgetList() = runTest {
        val widgetList = listOf(createDummyWidget("1", "Widget 1"), createDummyWidget("2", "Widget 2"))

        every { widgetRepository.getAllWidgets() } returns flowOf(widgetList)

        getAllWidgetsUseCase(NoParams).collect { result ->
            assertTrue(result is AppResult.Success)
            assertEquals(widgetList, (result as AppResult.Success).data)
        }
    }

    @Test
    fun givenEmpty_whenObserved_thenEmitsEmptyList() = runTest {
        every { widgetRepository.getAllWidgets() } returns flowOf(emptyList())

        getAllWidgetsUseCase(NoParams).collect { result ->
            assertTrue(result is AppResult.Success)
            assertTrue((result as AppResult.Success).data.isEmpty())
        }
    }
}
