package com.morphos.app.core.data.agent

import android.Manifest
import android.app.usage.UsageStatsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.PowerManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import com.morphos.app.core.common.AppDispatchers
import com.morphos.app.core.common.AppResult
import com.morphos.app.core.common.safeCall
import com.morphos.app.core.domain.agent.ContextAgent
import com.morphos.app.core.domain.model.ContextSnapshot
import com.morphos.app.core.domain.model.UpcomingEvent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContextAgentImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dispatchers: AppDispatchers
) : ContextAgent {

    private val scope = CoroutineScope(SupervisorJob() + dispatchers.default)
    private val _contextFlow = MutableStateFlow(buildDefaultSnapshot())
    override val contextFlow: StateFlow<ContextSnapshot> = _contextFlow.asStateFlow()

    private var receiver: BroadcastReceiver? = null

    override suspend fun refresh(): AppResult<ContextSnapshot> = safeCall {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        // Convert Calendar.DAY_OF_WEEK (Sun=1...Sat=7) to Mon=1...Sun=7
        val calDay = calendar.get(Calendar.DAY_OF_WEEK)
        val dayOfWeek = when (calDay) {
            Calendar.SUNDAY -> 7
            else -> calDay - 1
        }

        // Battery Status
        val batteryFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context.registerReceiver(null, batteryFilter)
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = if (level >= 0 && scale > 0) (level * 100f / scale).toInt() else 50
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        // Connectivity Status
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetwork
        val caps = cm.getNetworkCapabilities(activeNetwork)
        val isConnected = caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        val isOnWifi = caps != null && caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)

        // Power Saver
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val isBatterySaver = pm.isPowerSaveMode

        // Upcoming Calendar Event (Lazy check)
        val calendarEvent = try {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
                val proj = arrayOf(CalendarContract.Events.TITLE, CalendarContract.Events.DTSTART)
                val sel = "${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DELETED} = 0"
                val selArgs = arrayOf(now.toString())
                context.contentResolver.query(
                    CalendarContract.Events.CONTENT_URI,
                    proj,
                    sel,
                    selArgs,
                    "${CalendarContract.Events.DTSTART} ASC LIMIT 1"
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val title = cursor.getString(0) ?: ""
                        val startTime = cursor.getLong(1)
                        val mins = ((startTime - now) / 60000).toInt()
                        UpcomingEvent(title, startTime, mins)
                    } else null
                }
            } else null
        } catch (e: SecurityException) {
            null
        }

        // Foreground App (Lazy check last 1 min)
        val foregroundApp = try {
            val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, now - 60000L, now)
            stats?.maxByOrNull { it.lastTimeUsed }?.packageName
        } catch (e: Exception) {
            null
        }

        val snapshot = ContextSnapshot(
            timestamp = now,
            hourOfDay = hour,
            dayOfWeek = dayOfWeek,
            batteryLevel = batteryPct,
            isCharging = isCharging,
            isOnWifi = isOnWifi,
            isConnected = isConnected,
            isBatterySaverActive = isBatterySaver,
            upcomingCalendarEvent = calendarEvent,
            foregroundApp = foregroundApp
        )

        _contextFlow.value = snapshot
        snapshot
    }

    override fun start() {
        if (receiver != null) return

        receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                scope.launch {
                    refresh()
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_USER_PRESENT)
            addAction(Intent.ACTION_BATTERY_CHANGED)
        }
        ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        scope.launch {
            refresh()
        }
    }

    override fun stop() {
        receiver?.let {
            context.unregisterReceiver(it)
            receiver = null
        }
    }

    private fun buildDefaultSnapshot() = ContextSnapshot(
        timestamp = System.currentTimeMillis(),
        hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
        dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK),
        batteryLevel = 100,
        isCharging = false,
        isOnWifi = false,
        isConnected = false,
        isBatterySaverActive = false
    )
}
