package com.morphos.app.core.testing

import com.morphos.app.core.common.AppResult
import com.morphos.app.core.domain.model.*
import com.morphos.app.core.domain.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class FakeWidgetRepository : WidgetRepository {
    private val widgetsMap = mutableMapOf<String, WidgetConfig>()
    private val widgetsFlow = MutableStateFlow<List<WidgetConfig>>(emptyList())

    override fun getAllWidgets(): Flow<List<WidgetConfig>> = widgetsFlow

    override suspend fun getWidgetById(id: String): WidgetConfig? = widgetsMap[id]

    override suspend fun saveWidget(widget: WidgetConfig): AppResult<Unit> {
        widgetsMap[widget.id] = widget
        widgetsFlow.value = widgetsMap.values.toList()
        return AppResult.Success(Unit)
    }

    override suspend fun deleteWidget(id: String): AppResult<Unit> {
        widgetsMap.remove(id)
        widgetsFlow.value = widgetsMap.values.toList()
        return AppResult.Success(Unit)
    }

    override suspend fun updateWidget(widget: WidgetConfig): AppResult<Unit> {
        widgetsMap[widget.id] = widget
        widgetsFlow.value = widgetsMap.values.toList()
        return AppResult.Success(Unit)
    }

    override suspend fun getWidgetsByTemplate(templateId: String): List<WidgetConfig> {
        return widgetsMap.values.filter { it.templateId == templateId }
    }
}

class FakeMemoryRepository : MemoryRepository {
    private val events = mutableListOf<ShortTermEvent>()
    private val memories = mutableListOf<LongTermMemory>()
    private var memoryProfile = MemoryProfile(emptyMap(), emptyList(), emptyMap())

    override suspend fun recordShortTermEvent(event: ShortTermEvent): AppResult<Unit> {
        events.add(event)
        return AppResult.Success(Unit)
    }

    override fun getRecentEvents(limit: Int): Flow<List<ShortTermEvent>> {
        return flowOf(events.takeLast(limit))
    }

    override suspend fun saveMemoryProfile(profile: MemoryProfile): AppResult<Unit> {
        memoryProfile = profile
        return AppResult.Success(Unit)
    }

    override suspend fun getMemoryProfile(): MemoryProfile = memoryProfile

    override suspend fun saveLongTermMemory(memory: LongTermMemory): AppResult<Unit> {
        memories.add(memory)
        return AppResult.Success(Unit)
    }

    override fun getLongTermMemories(type: MemoryType?): Flow<List<LongTermMemory>> {
        val filtered = if (type == null) memories else memories.filter { it.type == type }
        return flowOf(filtered)
    }

    override suspend fun deleteMemoriesOlderThan(epochMillis: Long): AppResult<Int> {
        val sizeBefore = events.size
        events.removeAll { it.timestamp < epochMillis }
        return AppResult.Success(sizeBefore - events.size)
    }

    override suspend fun clearAllMemory(): AppResult<Unit> {
        events.clear()
        memories.clear()
        memoryProfile = MemoryProfile(emptyMap(), emptyList(), emptyMap())
        return AppResult.Success(Unit)
    }
}

class FakePluginRepository : PluginRepository {
    private val cachedData = mutableMapOf<String, PluginData>()
    var mockFetchResult: AppResult<PluginData> = AppResult.Error(Exception("Not Mocked"))
    var availablePlugins: List<DataPlugin> = emptyList()

    override fun getAvailablePlugins(): List<DataPlugin> = availablePlugins

    override suspend fun fetchPluginData(pluginId: String, config: Map<String, String>): AppResult<PluginData> {
        return mockFetchResult
    }

    override suspend fun getCachedPluginData(dataSourceId: String): PluginData? {
        return cachedData[dataSourceId]
    }

    override suspend fun cachePluginData(data: PluginData, ttlSeconds: Int): AppResult<Unit> {
        cachedData[data.dataSourceId] = data
        return AppResult.Success(Unit)
    }

    override suspend fun invalidateCache(dataSourceId: String): AppResult<Unit> {
        cachedData.remove(dataSourceId)
        return AppResult.Success(Unit)
    }
}

class FakeEmbeddingRepository : EmbeddingRepository {
    private val embeddings = mutableListOf<EmbeddingEntry>()

    override suspend fun saveEmbedding(entry: EmbeddingEntry): AppResult<Unit> {
        embeddings.add(entry)
        return AppResult.Success(Unit)
    }

    override suspend fun findSimilar(queryEmbedding: FloatArray, topK: Int): List<EmbeddingEntry> {
        return embeddings.take(topK) // Simple mock fallback
    }

    override suspend fun deleteEmbedding(id: String): AppResult<Unit> {
        embeddings.removeAll { it.id == id }
        return AppResult.Success(Unit)
    }

    override suspend fun getAllEmbeddings(): List<EmbeddingEntry> = embeddings
}

class FakeNotificationRepository : NotificationRepository {
    private val notificationsMap = mutableMapOf<String, PrioritizedNotification>()
    private val notificationsFlow = MutableStateFlow<List<PrioritizedNotification>>(emptyList())

    override fun getPrioritizedNotifications(): Flow<List<PrioritizedNotification>> = notificationsFlow

    override suspend fun logNotification(notification: PrioritizedNotification): AppResult<Unit> {
        notificationsMap[notification.id] = notification
        notificationsFlow.value = notificationsMap.values.toList()
        return AppResult.Success(Unit)
    }

    override suspend fun dismissNotification(id: String): AppResult<Unit> {
        notificationsMap.remove(id)
        notificationsFlow.value = notificationsMap.values.toList()
        return AppResult.Success(Unit)
    }
}

class FakeSettingsRepository : SettingsRepository {
    private var userPreferences = UserPreferences()

    override fun getUserPreferences(): Flow<UserPreferences> = flowOf(userPreferences)

    override suspend fun updateUserPreferences(prefs: UserPreferences): AppResult<Unit> {
        userPreferences = prefs
        return AppResult.Success(Unit)
    }

    override suspend fun isOnboardingComplete(): Boolean = userPreferences.isOnboardingComplete

    override suspend fun setOnboardingComplete(): AppResult<Unit> {
        userPreferences = userPreferences.copy(isOnboardingComplete = true)
        return AppResult.Success(Unit)
    }

    override suspend fun isCloudAiEnabled(): Boolean = userPreferences.isCloudAiEnabled

    override suspend fun setCloudAiEnabled(enabled: Boolean): AppResult<Unit> {
        userPreferences = userPreferences.copy(isCloudAiEnabled = enabled)
        return AppResult.Success(Unit)
    }

    override suspend fun getRetentionDays(): Int = userPreferences.retentionDays

    override suspend fun setRetentionDays(days: Int): AppResult<Unit> {
        userPreferences = userPreferences.copy(retentionDays = days)
        return AppResult.Success(Unit)
    }
}
