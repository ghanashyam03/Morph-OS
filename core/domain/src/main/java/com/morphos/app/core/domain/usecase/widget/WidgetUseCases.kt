package com.morphos.app.core.domain.usecase.widget

import com.morphos.app.core.common.AppResult
import com.morphos.app.core.common.safeCall
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

        // Map slotAssignments to SlotConfig
        val slots = params.plan.slotAssignments.mapValues { (slotId, pluginId) ->
            SlotConfig(
                slotId = slotId,
                contentType = ContentType.TEXT, // Default mapping
                dataSourceId = "${pluginId}_source"
            )
        }

        // Map slotAssignments to DataBinding
        val dataBindings = params.plan.slotAssignments.map { (_, pluginId) ->
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
