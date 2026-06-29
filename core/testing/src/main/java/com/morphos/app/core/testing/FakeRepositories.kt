package com.morphos.app.core.testing

import com.morphos.app.core.domain.model.*
import com.morphos.app.core.domain.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeWidgetRepository : WidgetRepository {
    override fun getWidgets(): Flow<List<WidgetConfig>> = flowOf(emptyList())
    override suspend fun getWidget(id: String): WidgetConfig? = null
    override suspend fun saveWidget(config: WidgetConfig) {}
    override suspend fun deleteWidget(id: String) {}
}

class FakeMemoryRepository : MemoryRepository {
    override fun getShortTermEvents(): Flow<List<ShortTermEvent>> = flowOf(emptyList())
    override suspend fun saveShortTermEvent(event: ShortTermEvent) {}
    override suspend fun getLongTermMemories(): List<LongTermMemory> = emptyList()
    override suspend fun saveLongTermMemory(memory: LongTermMemory) {}
    override fun getMemoryProfile(): Flow<MemoryProfile> = flowOf(MemoryProfile(emptyMap(), emptyList(), emptyMap()))
    override suspend fun saveMemoryProfile(profile: MemoryProfile) {}
    override suspend fun clearAllMemory() {}
}

class FakePluginRepository : PluginRepository {
    override suspend fun getCachedPluginData(pluginId: String, widgetId: String): PluginData? = null
    override suspend fun cachePluginData(pluginId: String, widgetId: String, data: PluginData, ttlSeconds: Int) {}
}

class FakeEmbeddingRepository : EmbeddingRepository {
    override suspend fun saveEmbedding(entry: EmbeddingEntry) {}
    override suspend fun findSimilar(embedding: FloatArray, limit: Int): List<EmbeddingEntry> = emptyList()
    override suspend fun clearEmbeddings() {}
}

class FakeNotificationRepository : NotificationRepository {
    override fun getNotifications(): Flow<List<PrioritizedNotification>> = flowOf(emptyList())
    override suspend fun saveNotification(notification: PrioritizedNotification) {}
    override suspend fun clearNotifications() {}
}

class FakeSettingsRepository : SettingsRepository {
    override fun getSetting(key: String, defaultValue: String): Flow<String> = flowOf(defaultValue)
    override suspend fun saveSetting(key: String, value: String) {}
    override fun isOnboardingCompleted(): Flow<Boolean> = flowOf(false)
    override suspend fun setOnboardingCompleted(completed: Boolean) {}
}
