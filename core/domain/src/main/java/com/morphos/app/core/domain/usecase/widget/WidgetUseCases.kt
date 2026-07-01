package com.morphos.app.core.domain.usecase.widget

import com.morphos.app.core.common.AppResult
import com.morphos.app.core.common.safeCall
import com.morphos.app.core.common.onSuccess
import com.morphos.app.core.common.onError
import com.morphos.app.core.domain.agent.IntentAgent
import com.morphos.app.core.domain.agent.MemoryAgent
import com.morphos.app.core.domain.agent.PlanningAgent
import com.morphos.app.core.domain.model.*
import com.morphos.app.core.domain.repository.MemoryRepository
import com.morphos.app.core.domain.repository.SettingsRepository
import com.morphos.app.core.domain.repository.UserPreferences
import com.morphos.app.core.domain.repository.WidgetRepository
import com.morphos.app.core.domain.usecase.FlowUseCase
import com.morphos.app.core.domain.usecase.NoParams
import com.morphos.app.core.domain.usecase.UseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

data class CreateWidgetParams(val plan: WidgetPlan, val sizeClass: WidgetSizeClass)

class CreateWidgetUseCase @Inject constructor(
    private val widgetRepository: WidgetRepository
) : UseCase<CreateWidgetParams, WidgetConfig>() {
    override suspend fun invoke(params: CreateWidgetParams): AppResult<WidgetConfig> = safeCall {
        val suggestedName = if (params.plan.suggestedName.isBlank()) {
            "Widget_${params.plan.selectedTemplateId}"
        } else {
            params.plan.suggestedName
        }
        require(params.plan.selectedTemplateId.isNotBlank()) { "Template ID cannot be blank" }

        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        val plugins = params.plan.slotAssignments.values.distinct()
        val slots = createTemplateSlots(params.plan.selectedTemplateId, plugins, suggestedName)

        // Map slotAssignments to DataBinding
        val dataBindings = plugins.map { pluginId ->
            DataBinding(
                dataSourceId = "${pluginId}_source",
                pluginId = pluginId,
                pluginConfig = params.plan.pluginConfigs[pluginId] ?: emptyMap()
            )
        }

        val config = WidgetConfig(
            id = id,
            name = suggestedName,
            description = params.plan.suggestedDescription,
            templateId = params.plan.selectedTemplateId,
            sizeClass = params.sizeClass,
            slots = slots,
            dataBindings = dataBindings,
            refreshPolicy = params.plan.suggestedRefreshPolicy,
            priorityWeights = params.plan.suggestedPriorityWeights,
            createdAt = now,
            lastModified = now
        )

        widgetRepository.saveWidget(config).onSuccess {}.onError { throw it }
        config
    }
}

private data class SlotSpec(
    val id: String,
    val plugin: String? = null,
    val field: String? = null,
    val fallback: String
)

private fun createTemplateSlots(
    templateId: String,
    plugins: List<String>,
    widgetName: String
): Map<String, SlotConfig> {
    fun plugin(index: Int, default: String = "clock") = plugins.getOrNull(index) ?: default
    fun valueFallback(id: String, source: String): String = when (source) {
        "clock" -> if (id.contains("date")) "Today" else "--:--"
        "battery" -> "Battery --%"
        "weather" -> if (id == "temperature") "--°" else "Weather unavailable"
        "steps" -> "0"
        "calendar" -> if (id.contains("time")) "Today" else "No upcoming events"
        "reminders" -> "No pending reminders"
        "countdown" -> if (id.contains("days")) "0" else "Countdown"
        "notifications" -> "No notifications"
        "news_rss" -> "No headlines"
        else -> "No data"
    }
    fun fieldFor(source: String, id: String): String = when (source) {
        "clock" -> if (id.contains("date")) "date" else "time"
        "battery" -> "level"
        "weather" -> when {
            id.contains("wind") -> "windspeed"
            id.contains("condition") -> "weathercode"
            else -> "temperature"
        }
        "steps" -> "steps"
        "calendar" -> if (id.contains("time")) "startTime" else "title"
        "reminders" -> "title"
        "countdown" -> if (id.contains("days")) "daysLeft" else "label"
        else -> "title"
    }
    val specs = when (templateId) {
        "TPL_WEATHER_FOCUS" -> listOf(
            SlotSpec("temperature", "weather", "temperature", "--°"),
            SlotSpec("condition", "weather", "weathercode", "Current weather"),
            SlotSpec("wind_speed", "weather", "windspeed", "--"),
            SlotSpec("condition_icon", fallback = "☀")
        )
        "TPL_HERO_PROGRESS" -> listOf(
            SlotSpec("title", fallback = widgetName),
            SlotSpec("progress_value", "steps", "steps", "0"),
            SlotSpec("progress_max", fallback = "10000"),
            SlotSpec("detail", "battery", "level", "Daily progress")
        )
        "TPL_TIMELINE" -> listOf(
            SlotSpec("event_0_title", "calendar", "title", "No upcoming events"),
            SlotSpec("event_0_time", "clock", "time", "Today")
        )
        "TPL_LIST_COMPACT" -> (1..4).map { index ->
            val source = plugin((index - 1) % plugins.size.coerceAtLeast(1), "reminders")
            SlotSpec("item_$index", source, fieldFor(source, "item_$index"), valueFallback("item_$index", source))
        }
        "TPL_COUNTDOWN" -> listOf(
            SlotSpec("label", "countdown", "label", widgetName),
            SlotSpec("days_left", "countdown", "daysLeft", "0"),
            SlotSpec("hours_left", "countdown", "hoursLeft", "0")
        )
        "TPL_GRID_3X1" -> (1..3).flatMap { index ->
            val source = plugin(index - 1)
            listOf(
                SlotSpec("slot_${index}_icon", fallback = source.replaceFirstChar { it.uppercase() }),
                SlotSpec("slot_${index}_value", source, fieldFor(source, "slot_${index}_value"), valueFallback("slot_${index}_value", source))
            )
        }
        "TPL_CARD_SINGLE" -> {
            val source = plugin(0)
            listOf(
                SlotSpec("header", fallback = widgetName),
                SlotSpec("body", source, fieldFor(source, "body"), valueFallback("body", source)),
                SlotSpec("action_label", fallback = "Open MorphOS")
            )
        }
        else -> (1..2).flatMap { index ->
            val source = plugin(index - 1)
            listOf(
                SlotSpec("header_$index", fallback = source.replaceFirstChar { it.uppercase() }),
                SlotSpec("body_$index", source, fieldFor(source, "body_$index"), valueFallback("body_$index", source))
            )
        }
    }
    return specs.associate { spec ->
        spec.id to SlotConfig(
            slotId = spec.id,
            contentType = ContentType.TEXT,
            dataSourceId = spec.plugin?.let { "${it}_source" }.orEmpty(),
            transformExpression = spec.field,
            fallbackValue = spec.fallback
        )
    }
}

class GetAllWidgetsUseCase @Inject constructor(
    private val widgetRepository: WidgetRepository
) : FlowUseCase<NoParams, List<WidgetConfig>>() {
    override fun invoke(params: NoParams): Flow<AppResult<List<WidgetConfig>>> {
        return widgetRepository.getAllWidgets().map { AppResult.Success(it) }
    }
}

class DeleteWidgetUseCase @Inject constructor(
    private val widgetRepository: WidgetRepository
) : UseCase<String, Unit>() {
    override suspend fun invoke(params: String): AppResult<Unit> {
        return widgetRepository.deleteWidget(params)
    }
}

class UpdateWidgetUseCase @Inject constructor(
    private val widgetRepository: WidgetRepository
) : UseCase<WidgetConfig, Unit>() {
    override suspend fun invoke(params: WidgetConfig): AppResult<Unit> = safeCall {
        require(params.id.isNotBlank()) { "Widget ID cannot be blank" }
        widgetRepository.updateWidget(params).onSuccess {}.onError { throw it }
    }
}

data class ParseIntentParams(val rawInput: String, val context: ContextSnapshot)

class ParseUserIntentUseCase @Inject constructor(
    private val intentAgent: IntentAgent
) : UseCase<ParseIntentParams, WidgetIntent>() {
    override suspend fun invoke(params: ParseIntentParams): AppResult<WidgetIntent> {
        if (params.rawInput.isBlank()) {
            return AppResult.Error(IllegalArgumentException("Raw input cannot be blank"))
        }
        return intentAgent.parseIntent(params.rawInput, params.context)
    }
}

data class GeneratePlanParams(
    val intent: WidgetIntent,
    val context: ContextSnapshot,
    val profile: MemoryProfile
)

class GenerateWidgetPlanUseCase @Inject constructor(
    private val planningAgent: PlanningAgent
) : UseCase<GeneratePlanParams, WidgetPlan>() {
    override suspend fun invoke(params: GeneratePlanParams): AppResult<WidgetPlan> {
        if (params.intent.intentType == IntentType.UNKNOWN && !params.intent.requiresCloudFallback) {
            return AppResult.Error(IllegalArgumentException("Cannot generate plan for unknown intent type without cloud fallback"))
        }
        return planningAgent.generatePlan(params.intent, params.context, params.profile)
    }
}

class RecordUserEventUseCase @Inject constructor(
    private val memoryAgent: MemoryAgent
) : UseCase<ShortTermEvent, Unit>() {
    override suspend fun invoke(params: ShortTermEvent): AppResult<Unit> {
        return memoryAgent.recordEvent(params)
    }
}

class GetMemoryProfileUseCase @Inject constructor(
    private val memoryRepository: MemoryRepository
) : UseCase<NoParams, MemoryProfile>() {
    override suspend fun invoke(params: NoParams): AppResult<MemoryProfile> = safeCall {
        memoryRepository.getMemoryProfile()
    }
}

class ClearAllMemoryUseCase @Inject constructor(
    private val memoryRepository: MemoryRepository
) : UseCase<NoParams, Unit>() {
    override suspend fun invoke(params: NoParams): AppResult<Unit> {
        return memoryRepository.clearAllMemory()
    }
}

class GetUserPreferencesUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) : FlowUseCase<NoParams, UserPreferences>() {
    override fun invoke(params: NoParams): Flow<AppResult<UserPreferences>> {
        return settingsRepository.getUserPreferences().map { AppResult.Success(it) }
    }
}

class UpdateUserPreferencesUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) : UseCase<UserPreferences, Unit>() {
    override suspend fun invoke(params: UserPreferences): AppResult<Unit> {
        return settingsRepository.updateUserPreferences(params)
    }
}
