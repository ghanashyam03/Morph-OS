package com.morphos.app.core.domain.model

data class ContentCandidate(
    val slotId: String,
    val title: String,
    val value: String,
    val icon: String?,
    val timestamp: Long,
    val sourcePluginId: String
)

data class RankedContent(
    val candidate: ContentCandidate,
    val score: Float
)
