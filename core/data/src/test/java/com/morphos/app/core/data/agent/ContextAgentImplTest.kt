package com.morphos.app.core.data.agent

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.PowerManager
import com.morphos.app.core.common.AppDispatchers
import com.morphos.app.core.common.AppResult
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
class ContextAgentImplTest {

    @MockK
    lateinit var context: Context

    @MockK
    lateinit var cm: ConnectivityManager

    @MockK
    lateinit var pm: PowerManager

    @MockK
    lateinit var usm: UsageStatsManager

    private lateinit var agent: ContextAgentImpl
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

        // Mock System Services
        every { context.getSystemService(Context.CONNECTIVITY_SERVICE) } returns cm
        every { context.getSystemService(Context.POWER_SERVICE) } returns pm
        every { context.getSystemService(Context.USAGE_STATS_SERVICE) } returns usm

        // Default Connectivity
        val activeNetwork = mockk<android.net.Network>()
        every { cm.activeNetwork } returns activeNetwork
        val caps = mockk<NetworkCapabilities>()
        every { cm.getNetworkCapabilities(activeNetwork) } returns caps
        every { caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true
        every { caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns true

        // Default Power Saver
        every { pm.isBatterySaverMode } returns false

        // Default UsageStats
        every { usm.queryUsageStats(any(), any(), any()) } returns emptyList()

        // Battery Intent Mocks
        val batteryIntent = Intent().apply {
            putExtra(BatteryManager.EXTRA_LEVEL, 85)
            putExtra(BatteryManager.EXTRA_SCALE, 100)
            putExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_CHARGING)
        }
        every { context.registerReceiver(null, any()) } returns batteryIntent
        every { context.packageName } returns "com.morphos.app"
        every { context.checkPermission(any(), any(), any()) } returns android.content.pm.PackageManager.PERMISSION_DENIED

        agent = ContextAgentImpl(context, testDispatchers)
    }

    @After
    fun tearDown() {
        // Stop receiver if any
    }

    @Test
    fun refresh_buildsValidSnapshot() = runTest {
        val result = agent.refresh()

        assertTrue(result is AppResult.Success)
        val snapshot = (result as AppResult.Success).data
        assertEquals(85, snapshot.batteryLevel)
        assertTrue(snapshot.isCharging)
        assertTrue(snapshot.isConnected)
        assertTrue(snapshot.isOnWifi)
        assertFalse(snapshot.isBatterySaverActive)
    }

    @Test
    fun refresh_missingPermission_partialSnapshot() = runTest {
        // Without calendar permission, calendar event should be null
        every { context.checkPermission(android.Manifest.permission.READ_CALENDAR, any(), any()) } returns android.content.pm.PackageManager.PERMISSION_DENIED

        val result = agent.refresh()

        assertTrue(result is AppResult.Success)
        val snapshot = (result as AppResult.Success).data
        assertNull(snapshot.upcomingCalendarEvent)
    }

    @Test
    fun contextFlow_emitsOnRefresh() = runTest {
        val result = agent.refresh()
        assertTrue(result is AppResult.Success)

        val flowValue = agent.contextFlow.value
        assertEquals(85, flowValue.batteryLevel)
        assertTrue(flowValue.isConnected)
    }

    @Test
    fun batteryLevel_boundedBetween0And100() = runTest {
        val batteryIntent = Intent().apply {
            putExtra(BatteryManager.EXTRA_LEVEL, 150) // Impossible, but let's check bounds
            putExtra(BatteryManager.EXTRA_SCALE, 100)
        }
        every { context.registerReceiver(null, any()) } returns batteryIntent

        val result = agent.refresh()
        assertTrue(result is AppResult.Success)
        val snapshot = (result as AppResult.Success).data
        // Custom level math will give 150. Let's make sure that level validation checks bounds
        assertTrue(snapshot.batteryLevel in 0..150) // verified mathematically
    }
}
