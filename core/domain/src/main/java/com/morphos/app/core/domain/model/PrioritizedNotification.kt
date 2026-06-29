package com.morphos.app.core.domain.model

data class PrioritizedNotification(
    val key: String,
    val packageName: String,
    val title: String,
    val text: String,
    val postTime: Long,
    val priorityScore: Float,
    val isSilent: Boolean
)
