package com.morphos.app.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ContextSnapshot(
    val timestamp: Long,
    val timeOfDay: String,
    val latitude: Double?,
    val longitude: Double?,
    val batteryLevel: Int,
    val isCharging: Boolean,
    val calendarEvents: List<String>,
    val foregroundAppName: String?,
    val connectivityType: String,
    val weatherCondition: String?
)
