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
            if (intent.intentType == IntentType.CREATE_CUSTOM_WIDGET || intent.requiresCloudFallback) {
                val availablePluginsStr = pluginRepository.getAvailablePlugins().joinToString(", ") { "${it.pluginId} (${it.displayName})" }
                val prompt = """
                    You are an Android Widget Planner. Design a widget plan matching this intent: '${intent.rawInput}'.
                    Available plugins: [$availablePluginsStr].
                    Current Context: HourOfDay=${context.hourOfDay}, Connected=${context.isConnected}.
                    Reply with ONLY a valid raw JSON object matching this schema:
                    {
                      "widgetIntentRaw": "${intent.rawInput}",
                      "selectedTemplateId": "TPL_CARD_DUAL",
                      "suggestedName": "Custom Widget",
                      "suggestedDescription": "Custom created widget",
                      "slotAssignments": {"slot1": "plugin_id"},
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
                    val responseJson = if (intent.intentType == IntentType.CREATE_CUSTOM_WIDGET && aiEngineManager.isTier1Available()) {
                        aiEngineManager.inferTier1(prompt)
                    } else if (intent.requiresCloudFallback && isCloudEnabled) {
                        aiEngineManager.inferTier2(prompt)
                    } else {
                        null
                    }

                    if (responseJson != null) {
                        val cleanedJson = cleanJsonResponse(responseJson)
                        val plan = json.decodeFromString<WidgetPlan>(cleanedJson)
                        // Make sure correct planSource is set
                        val finalSource = if (intent.requiresCloudFallback) PlanSource.TIER_2_CLOUD else PlanSource.TIER_1_LOCAL
                        return@safeCall plan.copy(planSource = finalSource)
                    }
                } catch (e: Exception) {
                    // Fall back to rule-based plan on JSON parsing errors
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
                else -> WidgetPlan( // default custom/unknown fallback
                    widgetIntentRaw = intent.rawInput,
                    selectedTemplateId = "TPL_CARD_DUAL",
                    suggestedName = "Adaptive Dashboard",
                    suggestedDescription = "General overview of your day",
                    slotAssignments = mapOf("slot1" to "weather", "slot2" to "clock", "slot3" to "battery"),
                    pluginConfigs = emptyMap(),
                    suggestedRefreshPolicy = RefreshPolicy(RefreshType.PERIODIC),
                    suggestedPriorityWeights = PriorityWeights(),
                    planConfidence = 0.5f,
                    planSource = PlanSource.RULE_BASED_FALLBACK
                )
            }
            rulePlan
        }
    }

    private fun cleanJsonResponse(response: String): String {
        var clean = response.trim()
        if (clean.startsWith("```json")) {
            clean = clean.substringAfter("```json").substringBeforeLast("```").trim()
        } else if (clean.startsWith("```")) {
            clean = clean.substringAfter("```").substringBeforeLast("```").trim()
        }
        return clean
    }
}
