package com.morphos.app.core.ai.di

import android.content.Context
import com.morphos.app.core.ai.*
import com.morphos.app.core.ai.network.OpenRouterApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AIModule {

    @Provides
    @Singleton
    fun provideLlamaCppEngine(@ApplicationContext context: Context): LlamaCppEngine {
        return LlamaCppEngine(context)
    }

    @Provides
    @Singleton
    fun provideOnnxEmbeddingEngine(@ApplicationContext context: Context): OnnxEmbeddingEngine {
        return OnnxEmbeddingEngine(context)
    }

    @Provides
    @Singleton
    fun provideCloudAIEngine(openRouterApiService: OpenRouterApiService): CloudAIEngine {
        return CloudAIEngine(openRouterApiService)
    }

    @Provides
    @Singleton
    fun provideAIEngineManager(
        llama: LlamaCppEngine,
        onnx: OnnxEmbeddingEngine,
        cloud: CloudAIEngine
    ): AIEngineManager {
        return AIEngineManager(llama, onnx, cloud)
    }
}
