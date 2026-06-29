package com.morphos.app.core.ai

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModelDownloadManager @Inject constructor() {
    fun downloadModel(config: ModelConfig): Flow<Float> {
        TODO("Not yet implemented")
    }
}
