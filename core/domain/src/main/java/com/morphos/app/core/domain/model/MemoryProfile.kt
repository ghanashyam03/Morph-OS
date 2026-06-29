package com.morphos.app.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class MemoryProfile(
    val userPreferences: Map<String, String>,
    val summarizedBehaviorPatterns: List<String>,
    val engagementHistory: Map<String, Float>
)
