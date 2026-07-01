package com.morphos.app.core.data.agent

import com.morphos.app.core.common.AppDispatchers
import com.morphos.app.core.common.AppResult
import com.morphos.app.core.common.safeCall
import com.morphos.app.core.ai.AIEngineManager
import com.morphos.app.core.domain.agent.MemoryAgent
import com.morphos.app.core.domain.model.*
import com.morphos.app.core.domain.repository.EmbeddingRepository
import com.morphos.app.core.domain.repository.MemoryRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject

class MemoryAgentImpl @Inject constructor(
    private val memoryRepository: MemoryRepository,
    private val embeddingRepository: EmbeddingRepository,
    private val aiEngineManager: AIEngineManager,
    private val dispatchers: AppDispatchers
) : MemoryAgent {

    override suspend fun recordEvent(event: ShortTermEvent): AppResult<Unit> = withContext(dispatchers.io) {
        memoryRepository.recordShortTermEvent(event)
    }

    override suspend fun summarizeRecentMemory(): AppResult<MemoryProfile> = withContext(dispatchers.default) {
        safeCall {
            val events = memoryRepository.getRecentEvents(50).first()
            val existingProfile = memoryRepository.getMemoryProfile()

            if (events.isEmpty()) return@safeCall existingProfile

            val eventsText = events.joinToString("\n") {
                "Time: ${it.timestamp}, Type: ${it.eventType.name}, Widget: ${it.widgetId}, Metadata: ${it.metadata}"
            }

            val proseSummary = if (aiEngineManager.isTier1Available()) {
                val prompt = "Summarize the following user activity log into a brief behavior summary:\n$eventsText"
                aiEngineManager.inferTier1(prompt)
            } else {
                "User interacted with widgets ${events.size} times. Frequently tapped action events recorded."
            }

            val memoryId = UUID.randomUUID().toString()
            val now = System.currentTimeMillis()

            // Compute embedding for long-term memory
            val embeddingId = if (aiEngineManager.isTier0Available()) {
                val embeddingArray = aiEngineManager.computeEmbedding(proseSummary)
                val embeddingEntry = EmbeddingEntry(
                    id = UUID.randomUUID().toString(),
                    sourceText = proseSummary,
                    embedding = embeddingArray,
                    createdAtEpochMilli = now
                )
                embeddingRepository.saveEmbedding(embeddingEntry)
                embeddingEntry.id
            } else {
                null
            }

            val longTermMemory = LongTermMemory(
                id = memoryId,
                content = proseSummary,
                embeddingId = embeddingId,
                memoryType = MemoryType.BEHAVIORAL_PATTERN,
                confidence = 0.9f,
                createdAt = now,
                expiresAt = now + 30L * 24 * 60 * 60 * 1000 // 30 days
            )
            memoryRepository.saveLongTermMemory(longTermMemory)

            // Compute profile updates
            val widgetTypeCounts = mutableMapOf<String, Int>()
            val activeHoursCounts = mutableMapOf<Int, Int>()

            events.forEach { event ->
                event.widgetId?.let { widgetId ->
                    widgetTypeCounts[widgetId] = (widgetTypeCounts[widgetId] ?: 0) + 1
                }
                val cal = Calendar.getInstance().apply { timeInMillis = event.timestamp }
                val hour = cal.get(Calendar.HOUR_OF_DAY)
                activeHoursCounts[hour] = (activeHoursCounts[hour] ?: 0) + 1
            }

            val preferredWidgets = widgetTypeCounts.entries
                .sortedByDescending { it.value }
                .take(3)
                .map { it.key }

            val activeHours = activeHoursCounts.entries
                .sortedByDescending { it.value }
                .take(5)
                .map { it.key }

            val updatedProfile = existingProfile.copy(
                preferredWidgetTypes = (existingProfile.preferredWidgetTypes + preferredWidgets).distinct().take(5),
                activeHours = activeHours.sorted(),
                lastUpdated = now
            )

            memoryRepository.saveMemoryProfile(updatedProfile)
            updatedProfile
        }
    }

    override suspend fun retrieveRelevantMemories(query: String): AppResult<List<LongTermMemory>> = withContext(dispatchers.default) {
        safeCall {
            val allMemories = memoryRepository.getLongTermMemories().first()
            if (allMemories.isEmpty()) return@safeCall emptyList()

            if (aiEngineManager.isTier0Available()) {
                val queryEmbedding = aiEngineManager.computeEmbedding(query)
                val matchingEmbeddings = embeddingRepository.findSimilar(queryEmbedding, 5)
                val matchingIds = matchingEmbeddings.map { it.id }.toSet()
                allMemories.filter { it.embeddingId in matchingIds }
            } else {
                // Fallback to simple text matching
                allMemories.filter { it.content.contains(query, ignoreCase = true) }
            }
        }
    }

    override suspend fun updateMemoryProfile(profile: MemoryProfile): AppResult<Unit> = withContext(dispatchers.io) {
        memoryRepository.saveMemoryProfile(profile)
    }
}
