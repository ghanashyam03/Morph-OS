package com.morphos.app.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ContextSnapshot(
    val timestamp: Long,
    val hourOfDay: Int,                          // 0–23
    val dayOfWeek: Int,                          // 1=Mon … 7=Sun
    val batteryLevel: Int,                       // 0–100
    val isCharging: Boolean,
    val isOnWifi: Boolean,
    val isConnected: Boolean,
    val isBatterySaverActive: Boolean,
    val locationLabel: String? = null,           // e.g. "home", "office" — never raw GPS
    val upcomingCalendarEvent: UpcomingEvent? = null,
    val foregroundApp: String? = null,           // package name
    val screenOnDurationTodayMinutes: Int = 0
)

@Serializable
data class UpcomingEvent(
    val title: String,
    val startEpochMillis: Long,
    val minutesUntilStart: Int
)
