package com.morphos.app.core.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.morphos.app.core.common.AppDispatchers
import com.morphos.app.core.common.AppResult
import com.morphos.app.core.data.datastore.MemoryProfileDataSource
import com.morphos.app.core.data.db.LongTermMemoryDao
import com.morphos.app.core.data.db.MorphOsDatabase
import com.morphos.app.core.data.db.ShortTermEventDao
import com.morphos.app.core.domain.model.MemoryProfile
import com.morphos.app.core.domain.model.ShortTermEvent
import com.morphos.app.core.domain.model.ShortTermEventType
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
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
class MemoryRepositoryImplTest {

    private lateinit var db: MorphOsDatabase
    private lateinit var shortTermEventDao: ShortTermEventDao
    private lateinit var longTermMemoryDao: LongTermMemoryDao
    
    @MockK
    lateinit var memoryProfileDataSource: MemoryProfileDataSource

    private lateinit var memoryRepository: MemoryRepositoryImpl
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testDispatchers = AppDispatchers(
        main = testDispatcher,
        io = testDispatcher,
        default = testDispatcher,
        unconfined = testDispatcher
    )

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, MorphOsDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        shortTermEventDao = db.shortTermEventDao()
        longTermMemoryDao = db.longTermMemoryDao()
        memoryRepository = MemoryRepositoryImpl(
            shortTermEventDao,
            longTermMemoryDao,
            memoryProfileDataSource,
            testDispatchers
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun recordEvent_persisted() = runTest {
        val event = ShortTermEvent("id_1", ShortTermEventType.WIDGET_TAPPED, "widget_A", System.currentTimeMillis())
        val result = memoryRepository.recordShortTermEvent(event)
        assertTrue(result is AppResult.Success)

        memoryRepository.getRecentEvents(5).test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals("id_1", list[0].id)
            assertEquals("widget_A", list[0].payload)
        }
    }

    @Test
    fun getRecentEvents_limitRespected() = runTest {
        for (i in 1..5) {
            val event = ShortTermEvent("id_$i", ShortTermEventType.WIDGET_TAPPED, "widget_A", System.currentTimeMillis() + i)
            memoryRepository.recordShortTermEvent(event)
        }

        memoryRepository.getRecentEvents(3).test {
            val list = awaitItem()
            assertEquals(3, list.size)
            assertEquals("id_5", list[2].id)
            assertEquals("id_4", list[1].id)
            assertEquals("id_3", list[0].id)
        }
    }

    @Test
    fun clearAllMemory_deletesAllData() = runTest {
        val event = ShortTermEvent("id_1", ShortTermEventType.WIDGET_TAPPED, "widget_A", System.currentTimeMillis())
        memoryRepository.recordShortTermEvent(event)

        coEvery { memoryProfileDataSource.saveMemoryProfile(any()) } returns Unit

        val clearResult = memoryRepository.clearAllMemory()
        assertTrue(clearResult is AppResult.Success)

        coVerify(exactly = 1) { memoryProfileDataSource.saveMemoryProfile(any()) }

        memoryRepository.getRecentEvents(5).test {
            val list = awaitItem()
            assertTrue(list.isEmpty())
        }
    }

    @Test
    fun saveAndGetMemoryProfile_roundtrips() = runTest {
        val profile = MemoryProfile(mapOf("study" to 1.5f), emptyList(), emptyMap())

        coEvery { memoryProfileDataSource.saveMemoryProfile(any()) } returns Unit
        coEvery { memoryProfileDataSource.getMemoryProfile() } returns profile

        val saveResult = memoryRepository.saveMemoryProfile(profile)
        assertTrue(saveResult is AppResult.Success)

        val retrieved = memoryRepository.getMemoryProfile()
        assertEquals(profile, retrieved)
    }
}
