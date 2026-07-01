package com.morphos.app.core.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import com.morphos.app.core.common.AppDispatchers
import com.morphos.app.core.common.AppResult
import com.morphos.app.core.domain.model.*
import com.morphos.app.core.domain.repository.PluginRepository
import com.morphos.app.core.domain.repository.WidgetRepository
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockKExtension::class)
class WidgetUpdatePipelineTest {

    @MockK
    lateinit var widgetRepository: WidgetRepository

    @MockK
    lateinit var pluginRepository: PluginRepository

    @MockK
    lateinit var context: Context

    private lateinit var pipeline: WidgetUpdatePipeline
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testDispatchers = AppDispatchers(
        main = testDispatcher,
        io = testDispatcher,
        default = testDispatcher,
        unconfined = testDispatcher
    )

    @BeforeEach
    fun setUp() {
        mockkConstructor(GlanceAppWidgetManager::class)
        mockkStatic("androidx.glance.appwidget.state.UpdateAppWidgetStateKt")
        mockkConstructor(MorphOsGlanceWidget::class)

        pipeline = WidgetUpdatePipeline(widgetRepository, pluginRepository, context, testDispatchers)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    private fun createWidget(id: String): WidgetConfig {
        return WidgetConfig(
            id = id,
            name = "Weather Widget",
            description = "Desc",
            templateId = "TPL_WEATHER_FOCUS",
            sizeClass = WidgetSizeClass.MEDIUM,
            slots = mapOf("temp" to SlotConfig("temp", ContentType.TEXT, "weather_src", transformExpression = "temp")),
            dataBindings = listOf(DataBinding("weather_src", "weather_plugin", emptyMap())),
            refreshPolicy = RefreshPolicy(emptyList()),
            priorityWeights = PriorityWeights(),
            createdAt = 0L,
            lastModified = 0L
        )
    }

    @Test
    fun updateWidget_missingConfig_doesNotCrash() = runTest {
        coEvery { widgetRepository.getWidgetById("missing") } returns null

        pipeline.updateWidget("missing") // Should complete gracefully without throwing
    }

    @Test
    fun updateWidget_allSlotsResolved_stateUpdated() = runTest {
        val widget = createWidget("1")
        coEvery { widgetRepository.getWidgetById("1") } returns widget

        val rawData = """{"temp":"25°C"}"""
        val pluginData = PluginData("weather_plugin", "weather_src", rawData, System.currentTimeMillis(), false)
        coEvery { pluginRepository.getCachedPluginData("weather_src") } returns pluginData

        val mockGlanceId = mockk<GlanceId>()
        every { mockGlanceId.hashCode().toString() } returns "1"
        every { anyConstructed<GlanceAppWidgetManager>().getGlanceIds(any()) } returns listOf(mockGlanceId)

        // Mock updateAppWidgetState calls
        coEvery { updateAppWidgetState(any(), any(), any(), any()) } returns mockk()
        coEvery { anyConstructed<MorphOsGlanceWidget>().update(any(), any()) } returns Unit

        pipeline.updateWidget("1")

        // Verifies update was triggered
        coVerify(exactly = 1) { anyConstructed<MorphOsGlanceWidget>().update(any(), mockGlanceId) }
    }

    @Test
    fun updateWidget_pluginFails_usesFallback() = runTest {
        val widget = createWidget("1")
        coEvery { widgetRepository.getWidgetById("1") } returns widget

        coEvery { pluginRepository.getCachedPluginData("weather_src") } returns null
        coEvery { pluginRepository.fetchPluginData("weather_plugin", any()) } returns AppResult.Error(Exception("Network error"))

        val mockGlanceId = mockk<GlanceId>()
        every { mockGlanceId.hashCode().toString() } returns "1"
        every { anyConstructed<GlanceAppWidgetManager>().getGlanceIds(any()) } returns listOf(mockGlanceId)

        coEvery { updateAppWidgetState(any(), any(), any(), any()) } returns mockk()
        coEvery { anyConstructed<MorphOsGlanceWidget>().update(any(), any()) } returns Unit

        pipeline.updateWidget("1")

        coVerify(exactly = 1) { anyConstructed<MorphOsGlanceWidget>().update(any(), mockGlanceId) }
    }

    @Test
    fun updateAllWidgets_callsEachWidget() = runTest {
        val widgets = listOf(createWidget("1"), createWidget("2"))
        every { widgetRepository.getAllWidgets() } returns flowOf(widgets)

        coEvery { widgetRepository.getWidgetById("1") } returns widgets[0]
        coEvery { widgetRepository.getWidgetById("2") } returns widgets[1]
        coEvery { pluginRepository.getCachedPluginData(any()) } returns null
        coEvery { pluginRepository.fetchPluginData(any(), any()) } returns AppResult.Error(Exception("Error"))

        val mockGlanceId1 = mockk<GlanceId>()
        every { mockGlanceId1.hashCode().toString() } returns "1"
        val mockGlanceId2 = mockk<GlanceId>()
        every { mockGlanceId2.hashCode().toString() } returns "2"
        every { anyConstructed<GlanceAppWidgetManager>().getGlanceIds(any()) } returns listOf(mockGlanceId1, mockGlanceId2)

        coEvery { updateAppWidgetState(any(), any(), any(), any()) } returns mockk()
        coEvery { anyConstructed<MorphOsGlanceWidget>().update(any(), any()) } returns Unit

        pipeline.updateAllWidgets()

        coVerify(exactly = 1) { anyConstructed<MorphOsGlanceWidget>().update(any(), mockGlanceId1) }
        coVerify(exactly = 1) { anyConstructed<MorphOsGlanceWidget>().update(any(), mockGlanceId2) }
    }
}
