package com.morphos.app.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class MemoryProfile(
    val preferredWidgetTypes: List<String> = emptyList(),
    val activeHours: List<Int> = emptyList(),   // hours of day user is active
    val preferredDataSources: List<String> = emptyList(),
    val dislikedContentTypes: List<String> = emptyList(),
    val notificationPreference: NotificationPreference = NotificationPreference.MODERATE,
    val lastUpdated: Long = 0L
)

enum class NotificationPreference { MINIMAL, MODERATE, VERBOSE }
