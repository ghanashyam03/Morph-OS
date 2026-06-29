package com.morphos.app.core.data.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.morphos.app.core.data.db.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import net.sqlcipher.database.SupportFactory
import net.sqlcipher.database.SQLiteDatabase

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideMorphOsDatabase(@ApplicationContext context: Context): MorphOsDatabase {
        // TODO: Passphrase must come from Android Keystore in production.
        val passphrase = SQLiteDatabase.getBytes("morphos_key".toCharArray())
        val factory = SupportFactory(passphrase)
        return Room.databaseBuilder(
            context,
            MorphOsDatabase::class.java,
            "morphos.db"
        )
        .openHelperFactory(factory)
        .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideEmbeddingDatabase(@ApplicationContext context: Context): EmbeddingDatabase {
        return Room.databaseBuilder(
            context,
            EmbeddingDatabase::class.java,
            "embedding.db"
        )
        .fallbackToDestructiveMigration()
        .build()
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
