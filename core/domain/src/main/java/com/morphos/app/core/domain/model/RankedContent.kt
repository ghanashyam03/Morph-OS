package com.morphos.app.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class RankedContent(
    val contentId: String,
    val score: Float,
    val metadata: Map<String, String> = emptyMap()
)

@Serializable
data class ContentCandidate(
    val contentId: String,
    val recency: Long,
    val engagementCount: Int,
    val contextMatchScore: Float,
    val metadata: Map<String, String> = emptyMap()
)
