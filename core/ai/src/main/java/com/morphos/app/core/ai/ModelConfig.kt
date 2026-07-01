package com.morphos.app.core.ai

data class ModelConfig(
    val name: String,
    val filename: String,
    val url: String,
    val sha256: String,
    val sizeBytes: Long
)
