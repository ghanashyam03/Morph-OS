package com.morphos.app.core.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.morphos.app.core.common.AppDispatchers
import com.morphos.app.core.common.AppResult
import com.morphos.app.core.data.db.MorphOsDatabase
import com.morphos.app.core.data.db.PluginDataCacheDao
import com.morphos.app.core.domain.model.PluginData
import com.morphos.app.core.domain.repository.DataPlugin
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
class PluginRepositoryImplTest {

    private lateinit var db: MorphOsDatabase
    private lateinit var cacheDao: PluginDataCacheDao

    @MockK
    lateinit var mockPlugin: DataPlugin

    private lateinit var repository: PluginRepositoryImpl
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
        cacheDao = db.pluginDataCacheDao()

        every { mockPlugin.pluginId } returns "test_plugin"
        repository = PluginRepositoryImpl(setOf(mockPlugin), cacheDao, testDispatchers)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun getCachedData_returnsCache_whenNotExpired() = runTest {
        val now = System.currentTimeMillis()
        val data = PluginData("test_plugin", "source_1", "raw value", now, false)

        val cacheResult = repository.cachePluginData(data, ttlSeconds = 60) // 1 min ttl
        assertTrue(cacheResult is AppResult.Success)

        val retrieved = repository.getCachedPluginData("source_1")
        assertNotNull(retrieved)
        assertEquals("raw value", retrieved?.rawValue)
        assertFalse(retrieved?.isStale ?: true)
    }

    @Test
    fun getCachedData_returnsNull_whenExpired() = runTest {
        val past = System.currentTimeMillis() - 10000L // 10s ago
        val data = PluginData("test_plugin", "source_1", "raw value", past, false)

        // TTL is 5 seconds, so it is expired
        val cacheResult = repository.cachePluginData(data, ttlSeconds = 5)
        assertTrue(cacheResult is AppResult.Success)

        val retrieved = repository.getCachedPluginData("source_1")
        assertNotNull(retrieved)
        assertTrue(retrieved?.isStale ?: false)
    }

    @Test
    fun fetchPluginData_callsCorrectPlugin() = runTest {
        val expectedData = PluginData("test_plugin", "source_1", "value", System.currentTimeMillis(), false)
        val config = mapOf("key" to "value")

        coEvery { mockPlugin.fetch(config) } returns AppResult.Success(expectedData)

        val result = repository.fetchPluginData("test_plugin", config)

        assertTrue(result is AppResult.Success)
        assertEquals(expectedData, (result as AppResult.Success).data)
        coVerify(exactly = 1) { mockPlugin.fetch(config) }
    }

    @Test
    fun fetchPluginData_unknownPlugin_returnsError() = runTest {
        val result = repository.fetchPluginData("unknown_plugin", emptyMap())
        assertTrue(result is AppResult.Error)
        assertTrue((result as AppResult.Error).message?.contains("not found") == true)
    }
}
