package com.morphos.app.core.data.mapper

import com.morphos.app.core.data.db.*
import com.morphos.app.core.domain.model.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.ByteBuffer

private val json = Json { ignoreUnknownKeys = true }

fun WidgetEntity.toDomain(): WidgetConfig {
    return json.decodeFromString(configJson)
}

fun WidgetConfig.toEntity(): WidgetEntity {
    return WidgetEntity(
        id = id,
        name = name,
        description = description,
        templateId = templateId,
        sizeClass = sizeClass.name,
        configJson = json.encodeToString(this),
        isPinned = isPinned,
        createdAt = createdAt,
        lastModified = lastModified,
        version = version
    )
}

fun ShortTermEventEntity.toDomain(): ShortTermEvent {
    val metadataMap = try {
        json.decodeFromString<Map<String, String>>(metadataJson)
    } catch (e: Exception) {
        emptyMap()
    }
    return ShortTermEvent(
        id = id,
        eventType = ShortTermEventType.valueOf(eventType),
        widgetId = widgetId,
        metadata = metadataMap,
        timestamp = timestamp
    )
}

fun ShortTermEvent.toEntity(): ShortTermEventEntity {
    return ShortTermEventEntity(
        id = id,
        eventType = eventType.name,
        widgetId = widgetId,
        metadataJson = json.encodeToString(metadata),
        timestamp = timestamp
    )
}

fun LongTermMemoryEntity.toDomain(): LongTermMemory {
    return LongTermMemory(
        id = id,
        content = content,
        embeddingId = embeddingId,
        memoryType = MemoryType.valueOf(memoryType),
        confidence = confidence,
        createdAt = createdAt,
        expiresAt = expiresAt
    )
}

fun LongTermMemory.toEntity(): LongTermMemoryEntity {
    return LongTermMemoryEntity(
        id = id,
        content = content,
        embeddingId = embeddingId,
        memoryType = memoryType.name,
        confidence = confidence,
        createdAt = createdAt,
        expiresAt = expiresAt
    )
}

fun NotificationLogEntity.toDomain(): PrioritizedNotification {
    return PrioritizedNotification(
        id = id,
        packageName = packageName,
        appName = appName,
        title = title,
        text = text,
        priority = NotificationPriority.valueOf(priority),
        postedAt = postedAt
    )
}

fun PrioritizedNotification.toEntity(): NotificationLogEntity {
    return NotificationLogEntity(
        id = id,
        packageName = packageName,
        appName = appName,
        title = title,
        text = text,
        priority = priority.name,
        postedAt = postedAt,
        isDismissed = false
    )
}

fun EmbeddingEntryEntity.toDomain(): EmbeddingEntry {
    val buffer = ByteBuffer.wrap(embeddingBlob).asFloatBuffer()
    val array = FloatArray(buffer.remaining())
    buffer.get(array)
    return EmbeddingEntry(
        id = id,
        sourceText = sourceText,
        embedding = array,
        createdAt = createdAt
    )
}

fun EmbeddingEntry.toEntity(): EmbeddingEntryEntity {
    val byteBuffer = ByteBuffer.allocate(embedding.size * 4)
    byteBuffer.asFloatBuffer().put(embedding)
    return EmbeddingEntryEntity(
        id = id,
        sourceText = sourceText,
        embeddingBlob = byteBuffer.array(),
        createdAt = createdAt
    )
}
