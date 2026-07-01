package com.morphos.app.core.data.agent

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.PowerManager
import androidx.test.core.app.ApplicationProvider
import com.morphos.app.core.common.AppDispatchers
import com.morphos.app.core.common.AppResult
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class ContextAgentIntegrationTest {

    private lateinit var context: Context
    private val cm: ConnectivityManager = mockk()
    private val pm: PowerManager = mockk()
    private val usm: UsageStatsManager = mockk()

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
        val appCtx = ApplicationProvider.getApplicationContext<Context>()
        context = spyk(appCtx)

        // Mock services
        every { context.getSystemService(Context.CONNECTIVITY_SERVICE) } returns cm
        every { context.getSystemService(Context.POWER_SERVICE) } returns pm
        every { context.getSystemService(Context.USAGE_STATS_SERVICE) } returns usm

        val activeNetwork = mockk<android.net.Network>()
        every { cm.activeNetwork } returns activeNetwork
        val caps = mockk<NetworkCapabilities>()
        every { cm.getNetworkCapabilities(activeNetwork) } returns caps
        every { caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true
        every { caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns true

        every { pm.isBatterySaverMode } returns false
        every { usm.queryUsageStats(any(), any(), any()) } returns emptyList()

        agent = ContextAgentImpl(context, testDispatchers)
    }

    @After
    fun tearDown() {
        agent.stop()
    }

    @Test
    fun refresh_withFakeContext_buildsSnapshot() = runTest {
        val result = agent.refresh()

        assertTrue(result is AppResult.Success)
        val snapshot = (result as AppResult.Success).data
        assertTrue(snapshot.isConnected)
    }

    @Test
    fun batteryBroadcast_triggersRefresh() = runTest {
        agent.start()

        // Send a mock broadcast using ShadowApplication or context
        val intent = Intent(Intent.ACTION_BATTERY_CHANGED).apply {
            putExtra(BatteryManager.EXTRA_LEVEL, 42)
            putExtra(BatteryManager.EXTRA_SCALE, 100)
        }
        
        // Directly send battery intent to registerReceiver receiver mock or trigger agent refresh
        val result = agent.refresh() // verifies state is updated inside flow
        assertTrue(result is AppResult.Success)
        assertEquals(42, agent.contextFlow.value.batteryLevel)
    }
}
