package com.morphos.app.core.ai.di

import com.morphos.app.core.ai.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AIModule {

    @Provides
    @Singleton
    fun provideLlamaCppEngine(): LlamaCppEngine {
        TODO("Not yet implemented")
    }

    @Provides
    @Singleton
    fun provideOnnxEmbeddingEngine(): OnnxEmbeddingEngine {
        TODO("Not yet implemented")
    }

    @Provides
    @Singleton
    fun provideCloudAIEngine(): CloudAIEngine {
        TODO("Not yet implemented")
    }

    @Provides
    @Singleton
    fun provideAIEngineManager(
        llama: LlamaCppEngine,
        onnx: OnnxEmbeddingEngine,
        cloud: CloudAIEngine
    ): AIEngineManager {
        TODO("Not yet implemented")
    }
}
