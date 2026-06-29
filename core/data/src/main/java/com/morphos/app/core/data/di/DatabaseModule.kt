package com.morphos.app.core.data.di

import android.content.Context
import com.morphos.app.core.data.db.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideMorphOsDatabase(@ApplicationContext context: Context): MorphOsDatabase {
        TODO("Not yet implemented")
    }

    @Provides
    @Singleton
    fun provideEmbeddingDatabase(@ApplicationContext context: Context): EmbeddingDatabase {
        TODO("Not yet implemented")
    }

    @Provides
    fun provideWidgetDao(db: MorphOsDatabase): WidgetDao = db.widgetDao()

    @Provides
    fun provideShortTermEventDao(db: MorphOsDatabase): ShortTermEventDao = db.shortTermEventDao()

    @Provides
    fun provideLongTermMemoryDao(db: MorphOsDatabase): LongTermMemoryDao = db.longTermMemoryDao()

    @Provides
    fun providePluginDataCacheDao(db: MorphOsDatabase): PluginDataCacheDao = db.pluginDataCacheDao()

    @Provides
    fun provideNotificationLogDao(db: MorphOsDatabase): NotificationLogDao = db.notificationLogDao()

    @Provides
    fun provideAgentTaskDao(db: MorphOsDatabase): AgentTaskDao = db.agentTaskDao()

    @Provides
    fun provideEmbeddingEntryDao(db: EmbeddingDatabase): EmbeddingEntryDao = db.embeddingEntryDao()
}
