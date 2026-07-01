package com.morphos.app.core.data.di

import com.morphos.app.core.data.repository.*
import com.morphos.app.core.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindWidgetRepository(impl: WidgetRepositoryImpl): WidgetRepository

    @Binds
    @Singleton
    abstract fun bindMemoryRepository(impl: MemoryRepositoryImpl): MemoryRepository

    @Binds
    @Singleton
    abstract fun bindPluginRepository(impl: PluginRepositoryImpl): PluginRepository

    @Binds
    @Singleton
    abstract fun bindEmbeddingRepository(impl: EmbeddingRepositoryImpl): EmbeddingRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(impl: NotificationRepositoryImpl): NotificationRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindConnectivityObserver(impl: com.morphos.app.core.common.NetworkConnectivityObserver): com.morphos.app.core.common.ConnectivityObserver
}
