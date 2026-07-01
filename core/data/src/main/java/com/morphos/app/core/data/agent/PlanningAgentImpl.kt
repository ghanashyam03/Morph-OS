package com.morphos.app.core.data.agent

import com.morphos.app.core.common.AppDispatchers
import com.morphos.app.core.common.AppResult
import com.morphos.app.core.common.safeCall
import com.morphos.app.core.ai.AIEngineManager
import com.morphos.app.core.domain.agent.PlanningAgent
import com.morphos.app.core.domain.model.*
import com.morphos.app.core.domain.repository.PluginRepository
import com.morphos.app.core.domain.repository.SettingsRepository
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import timber.log.Timber

class PlanningAgentImpl @Inject constructor(
    private val aiEngineManager: AIEngineManager,
    private val pluginRepository: PluginRepository,
    private val settingsRepository: SettingsRepository,
    private val dispatchers: AppDispatchers
) : PlanningAgent {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun generatePlan(
        intent: WidgetIntent,
        context: ContextSnapshot,
        memoryProfile: MemoryProfile
    ): AppResult<WidgetPlan> = withContext(dispatchers.default) {
        safeCall {
            val isCloudEnabled = settingsRepository.isCloudAiEnabled()

            // Step 2 & 3: Custom Widget Planning via AI if available
            if (aiEngineManager.isTier1Available() || aiEngineManager.isTier0Available() || isCloudEnabled) {
                val availablePluginsStr = pluginRepository.getAvailablePlugins().joinToString(", ") { "${it.pluginId} (${it.displayName})" }
                val prompt = """
                    You are an Android Widget Planner. Design a widget plan matching this intent: '${intent.rawInput}'.
                    Available plugins: [$availablePluginsStr].
                    Current Context: HourOfDay=${context.hourOfDay}, Connected=${context.isConnected}.
                    You MUST choose only plugin IDs from the available plugins and only one of these template IDs:
                    TPL_CARD_SINGLE, TPL_CARD_DUAL, TPL_LIST_COMPACT, TPL_GRID_2X2, TPL_GRID_3X1,
                    TPL_HERO_PROGRESS, TPL_TIMELINE, TPL_WEATHER_FOCUS, TPL_COUNTDOWN,
                    TPL_NOTIFICATION_FEED, TPL_QUICK_ACTIONS.
                    Select capabilities based on the user's words. If the user says weather, slotAssignments MUST
                    contain weather. If the user says clock or time, it MUST contain clock. Do not copy generic
                    example values. The suggestedName must specifically describe the user's request.
                    Never write "plugin_id". Use real plugin IDs. Reply with ONLY one valid raw JSON object:
                    {
                      "widgetIntentRaw": "${intent.rawInput}",
                      "selectedTemplateId": "TPL_CARD_DUAL",
                      "suggestedName": "<specific short name derived from the request>",
                      "suggestedDescription": "<specific description derived from the request>",
                      "slotAssignments": {"slot1": "<relevant available plugin ID>"},
                      "pluginConfigs": {},
                      "suggestedRefreshPolicy": {
                        "type": "PERIODIC",
                        "minIntervalSeconds": 1800
                      },
                      "suggestedPriorityWeights": {
                        "recencyWeight": 0.3,
                        "engagementWeight": 0.4,
                        "contextRelevanceWeight": 0.3
                      },
                      "planConfidence": 0.9,
                      "planSource": "TIER_1_LOCAL"
                    }
                """.trimIndent()

                try {
                    var responseJson = if (aiEngineManager.isTier1Available() || aiEngineManager.isTier0Available()) {
                        aiEngineManager.inferLocalPlan(prompt)
                    } else if (intent.requiresCloudFallback && isCloudEnabled) {
                        aiEngineManager.inferTier2(prompt)
                    } else {
                        null
                    }

                    if (responseJson != null) {
                        val allowedPlugins = pluginRepository.getAvailablePlugins().map { it.pluginId }.toSet()
                        val lowerInput = intent.rawInput.lowercase()
                        val explicitlyRequested = buildSet {
                            if ("weather" in lowerInput || "temperature" in lowerInput) add("weather")
                            if ("clock" in lowerInput || "time" in lowerInput) add("clock")
                            if ("battery" in lowerInput || "charge" in lowerInput) add("battery")
                            if ("calendar" in lowerInput || "event" in lowerInput) add("calendar")
                            if ("step" in lowerInput || "walk" in lowerInput) add("steps")
                            if ("reminder" in lowerInput || "task" in lowerInput) add("reminders")
                            if ("countdown" in lowerInput) add("countdown")
                            if ("notification" in lowerInput) add("notifications")
                            if ("news" in lowerInput || "headline" in lowerInput) add("news_rss")
                        }
                        var lastError: Exception? = null
                        repeat(2) { attempt ->
                            try {
                                Timber.d("WidgetPlanning raw response attempt=%d input='%s': %s", attempt + 1, intent.rawInput, responseJson)
                                val plan = json.decodeFromString<WidgetPlan>(cleanJsonResponse(responseJson!!))
                                require(plan.slotAssignments.isNotEmpty()) { "AI plan contains no data bindings" }
                                require(plan.slotAssignments.values.all { it in allowedPlugins }) {
                                    "AI plan contains unavailable plugins: ${plan.slotAssignments.values - allowedPlugins}"
                                }
                                val missingCapabilities = explicitlyRequested - plan.slotAssignments.values.toSet()
                                val normalizedAssignments = plan.slotAssignments.toMutableMap().apply {
                                    missingCapabilities.forEach { plugin ->
                                        var index = size + 1
                                        while (containsKey("slot$index")) index++
                                        put("slot$index", plugin)
                                    }
                                }
                                val finalSource = if (aiEngineManager.isTier1Available() || aiEngineManager.isTier0Available()) {
                                    PlanSource.TIER_1_LOCAL
                                } else {
                                    PlanSource.TIER_2_CLOUD
                                }
                                return@safeCall plan.copy(
                                    slotAssignments = normalizedAssignments,
                                    planSource = finalSource
                                )
                            } catch (validationError: Exception) {
                                lastError = validationError
                                if (attempt == 0 && (aiEngineManager.isTier1Available() || aiEngineManager.isTier0Available())) {
                                    responseJson = aiEngineManager.inferLocalPlan(
                                        """Your previous widget plan was invalid.
                                            |Validation error: ${validationError.message}
                                            |User request: ${intent.rawInput}
                                            |Previous response: $responseJson
                                            |Return one corrected JSON object. Preserve the schema and include every explicitly requested capability: ${explicitlyRequested.joinToString()}.
                                        """.trimMargin()
                                    )
                                }
                            }
                        }
                        throw lastError ?: IllegalStateException("AI plan validation failed")
                    }
                } catch (e: Exception) {
                    Timber.e(e, "AI widget plan generation failed for '%s'", intent.rawInput)
                    throw IllegalStateException("AI could not produce a valid functional widget plan. Please retry.", e)
                }
            }

            // Step 1: Rule-based plans
            val rulePlan = when (intent.intentType) {
                IntentType.CREATE_STUDY_WIDGET -> WidgetPlan(
                    widgetIntentRaw = intent.rawInput,
                    selectedTemplateId = "TPL_TIMELINE",
                    suggestedName = "Study Helper",
                    suggestedDescription = "Manage focus, calendar items, and exams",
                    slotAssignments = mapOf("slot1" to "calendar", "slot2" to "countdown", "slot3" to "clock"),
                    pluginConfigs = emptyMap(),
                    suggestedRefreshPolicy = RefreshPolicy(RefreshType.PERIODIC),
                    suggestedPriorityWeights = PriorityWeights(),
                    planConfidence = 0.8f,
                    planSource = PlanSource.RULE_BASED_FALLBACK
                )
                IntentType.CREATE_TRAVEL_WIDGET -> WidgetPlan(
                    widgetIntentRaw = intent.rawInput,
                    selectedTemplateId = "TPL_CARD_DUAL",
                    suggestedName = "Travel Desk",
                    suggestedDescription = "Upcoming flights, local weather, and time",
                    slotAssignments = mapOf("slot1" to "weather", "slot2" to "calendar", "slot3" to "clock"),
                    pluginConfigs = emptyMap(),
                    suggestedRefreshPolicy = RefreshPolicy(RefreshType.PERIODIC),
                    suggestedPriorityWeights = PriorityWeights(),
                    planConfidence = 0.8f,
                    planSource = PlanSource.RULE_BASED_FALLBACK
                )
                IntentType.CREATE_FITNESS_WIDGET -> WidgetPlan(
                    widgetIntentRaw = intent.rawInput,
                    selectedTemplateId = "TPL_HERO_PROGRESS",
                    suggestedName = "Daily Steps Tracker",
                    suggestedDescription = "Track steps, battery, and clock",
                    slotAssignments = mapOf("slot1" to "steps", "slot2" to "clock", "slot3" to "battery"),
                    pluginConfigs = emptyMap(),
                    suggestedRefreshPolicy = RefreshPolicy(RefreshType.PERIODIC),
                    suggestedPriorityWeights = PriorityWeights(),
                    planConfidence = 0.8f,
                    planSource = PlanSource.RULE_BASED_FALLBACK
                )
                IntentType.CREATE_WEATHER_WIDGET -> WidgetPlan(
                    widgetIntentRaw = intent.rawInput,
                    selectedTemplateId = "TPL_WEATHER_FOCUS",
                    suggestedName = "Weather Station",
                    suggestedDescription = "Current weather and time",
                    slotAssignments = mapOf("slot1" to "weather", "slot2" to "clock"),
                    pluginConfigs = emptyMap(),
                    suggestedRefreshPolicy = RefreshPolicy(RefreshType.PERIODIC),
                    suggestedPriorityWeights = PriorityWeights(),
                    planConfidence = 0.8f,
                    planSource = PlanSource.RULE_BASED_FALLBACK
                )
                IntentType.CREATE_PRODUCTIVITY_WIDGET -> WidgetPlan(
                    widgetIntentRaw = intent.rawInput,
                    selectedTemplateId = "TPL_LIST_COMPACT",
                    suggestedName = "Task Board",
                    suggestedDescription = "Next alarms, reminders, and clock",
                    slotAssignments = mapOf("slot1" to "reminders", "slot2" to "calendar", "slot3" to "clock"),
                    pluginConfigs = emptyMap(),
                    suggestedRefreshPolicy = RefreshPolicy(RefreshType.PERIODIC),
                    suggestedPriorityWeights = PriorityWeights(),
                    planConfidence = 0.8f,
                    planSource = PlanSource.RULE_BASED_FALLBACK
                )
                IntentType.CREATE_FINANCE_WIDGET -> WidgetPlan(
                    widgetIntentRaw = intent.rawInput,
                    selectedTemplateId = "TPL_CARD_SINGLE",
                    suggestedName = "Finance Tracker",
                    suggestedDescription = "Overview of budgets and limits",
                    slotAssignments = mapOf("slot1" to "clock", "slot2" to "battery"),
                    pluginConfigs = emptyMap(),
                    suggestedRefreshPolicy = RefreshPolicy(RefreshType.PERIODIC),
                    suggestedPriorityWeights = PriorityWeights(),
                    planConfidence = 0.7f,
                    planSource = PlanSource.RULE_BASED_FALLBACK
                )
                else -> buildCustomFallback(intent.rawInput)
            }
            Timber.d(
                "WidgetPlanning plan input='%s' source=%s template=%s name='%s' slots=%s",
                intent.rawInput, rulePlan.planSource, rulePlan.selectedTemplateId,
                rulePlan.suggestedName, rulePlan.slotAssignments
            )
            rulePlan
        }
    }

    private fun buildCustomFallback(rawInput: String): WidgetPlan {
        val lower = rawInput.lowercase()
        val plugins = buildList {
            if ("weather" in lower || "temperature" in lower) add("weather")
            if ("clock" in lower || "time" in lower) add("clock")
            if ("battery" in lower || "charge" in lower) add("battery")
            if ("calendar" in lower || "event" in lower) add("calendar")
            if ("step" in lower || "walk" in lower) add("steps")
            if ("reminder" in lower || "todo" in lower || "task" in lower) add("reminders")
            if ("countdown" in lower || "days left" in lower) add("countdown")
            if ("notification" in lower) add("notifications")
            if ("news" in lower || "headline" in lower) add("news_rss")
        }.distinct().ifEmpty { listOf("clock", "battery") }
        val template = when {
            plugins == listOf("weather") || ("weather" in plugins && plugins.size <= 2) -> "TPL_WEATHER_FOCUS"
            "steps" in plugins -> "TPL_HERO_PROGRESS"
            "countdown" in plugins -> "TPL_COUNTDOWN"
            "notifications" in plugins -> "TPL_NOTIFICATION_FEED"
            "calendar" in plugins -> "TPL_TIMELINE"
            plugins.size >= 3 -> "TPL_GRID_3X1"
            plugins.size == 1 -> "TPL_CARD_SINGLE"
            else -> "TPL_CARD_DUAL"
        }
        val name = rawInput.trim().replaceFirstChar { it.uppercase() }.take(32)
            .ifBlank { "Custom Widget" }
        return WidgetPlan(
                    widgetIntentRaw = rawInput,
                    selectedTemplateId = template,
                    suggestedName = name,
                    suggestedDescription = "Custom widget for: $rawInput",
                    slotAssignments = plugins.mapIndexed { index, plugin -> "slot${index + 1}" to plugin }.toMap(),
                    pluginConfigs = emptyMap(),
                    suggestedRefreshPolicy = RefreshPolicy(RefreshType.PERIODIC),
                    suggestedPriorityWeights = PriorityWeights(),
                    planConfidence = 0.8f,
                    planSource = PlanSource.RULE_BASED_FALLBACK
                )
    }

    private fun cleanJsonResponse(response: String): String {
        val start = response.indexOf('{')
        require(start >= 0) { "Model response did not contain JSON" }
        var depth = 0
        var inString = false
        var escaped = false
        for (index in start until response.length) {
            val char = response[index]
            if (escaped) {
                escaped = false
                continue
            }
            if (char == '\\' && inString) {
                escaped = true
                continue
            }
            if (char == '"') inString = !inString
            if (!inString) {
                if (char == '{') depth++
                if (char == '}') {
                    depth--
                    if (depth == 0) return response.substring(start, index + 1)
                }
            }
        }
        error("Model response contained incomplete JSON")
    }
}
