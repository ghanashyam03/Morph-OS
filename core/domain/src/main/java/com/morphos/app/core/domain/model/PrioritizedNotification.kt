package com.morphos.app.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PrioritizedNotification(
    val id: String,
    val packageName: String,
    val appName: String,
    val title: String?,
    val text: String?,
    val priority: NotificationPriority,
    val postedAt: Long
)

enum class NotificationPriority { HIGH, MEDIUM, LOW, SUPPRESSED }
