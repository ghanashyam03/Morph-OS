package com.morphos.app.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        WidgetEntity::class,
        ShortTermEventEntity::class,
        LongTermMemoryEntity::class,
        PluginDataCacheEntity::class,
        NotificationLogEntity::class,
        AgentTaskEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DbConverters::class)
abstract class MorphOsDatabase : RoomDatabase() {
    abstract fun widgetDao(): WidgetDao
    abstract fun shortTermEventDao(): ShortTermEventDao
    abstract fun longTermMemoryDao(): LongTermMemoryDao
    abstract fun pluginDataCacheDao(): PluginDataCacheDao
    abstract fun notificationLogDao(): NotificationLogDao
    abstract fun agentTaskDao(): AgentTaskDao
}

@Database(
    entities = [EmbeddingEntryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class EmbeddingDatabase : RoomDatabase() {
    abstract fun embeddingEntryDao(): EmbeddingEntryDao
}
