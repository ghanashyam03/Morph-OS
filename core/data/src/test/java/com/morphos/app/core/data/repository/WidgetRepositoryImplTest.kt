package com.morphos.app.core.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.morphos.app.core.common.AppDispatchers
import com.morphos.app.core.common.AppResult
import com.morphos.app.core.data.db.MorphOsDatabase
import com.morphos.app.core.data.db.WidgetDao
import com.morphos.app.core.domain.model.PriorityWeights
import com.morphos.app.core.domain.model.RefreshPolicy
import com.morphos.app.core.domain.model.WidgetConfig
import com.morphos.app.core.domain.model.WidgetSizeClass
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class WidgetRepositoryImplTest {

    private lateinit var db: MorphOsDatabase
    private lateinit var widgetDao: WidgetDao
    private lateinit var widgetRepository: WidgetRepositoryImpl
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testDispatchers = AppDispatchers(
        main = testDispatcher,
        io = testDispatcher,
        default = testDispatcher,
        unconfined = testDispatcher
    )

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, MorphOsDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        widgetDao = db.widgetDao()
        widgetRepository = WidgetRepositoryImpl(widgetDao, testDispatchers)
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun createWidget(id: String, name: String): WidgetConfig {
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
    fun saveAndRetrieve_widgetPersisted() = runTest {
        val widget = createWidget("widget_1", "Test Widget")

        val saveResult = widgetRepository.saveWidget(widget)
        assertTrue(saveResult is AppResult.Success)

        val retrieved = widgetRepository.getWidgetById("widget_1")
        assertNotNull(retrieved)
        assertEquals(widget.name, retrieved?.name)
        assertEquals(widget.templateId, retrieved?.templateId)
    }

    @Test
    fun delete_widgetRemovedFromFlow() = runTest {
        val widget = createWidget("widget_1", "Test Widget")
        widgetRepository.saveWidget(widget)

        widgetRepository.getAllWidgets().test {
            val initialList = awaitItem()
            assertEquals(1, initialList.size)

            widgetRepository.deleteWidget("widget_1")

            val updatedList = awaitItem()
            assertTrue(updatedList.isEmpty())
        }
    }

    @Test
    fun update_widgetModifiedInFlow() = runTest {
        val widget = createWidget("widget_1", "Test Widget")
        widgetRepository.saveWidget(widget)

        widgetRepository.getAllWidgets().test {
            val initialList = awaitItem()
            assertEquals("Test Widget", initialList[0].name)

            val updated = widget.copy(name = "Updated Name")
            widgetRepository.updateWidget(updated)

            val updatedList = awaitItem()
            assertEquals("Updated Name", updatedList[0].name)
        }
    }

    @Test
    fun getAllWidgets_emitsOnInsert() = runTest {
        widgetRepository.getAllWidgets().test {
            val initial = awaitItem()
            assertTrue(initial.isEmpty())

            val widget = createWidget("widget_1", "Widget")
            widgetRepository.saveWidget(widget)

            val afterInsert = awaitItem()
            assertEquals(1, afterInsert.size)
        }
    }
}
