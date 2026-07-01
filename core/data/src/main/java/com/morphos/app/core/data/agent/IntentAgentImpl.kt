package com.morphos.app.core.data.agent

import com.morphos.app.core.common.AppDispatchers
import com.morphos.app.core.common.AppResult
import com.morphos.app.core.common.safeCall
import com.morphos.app.core.ai.AIEngineManager
import com.morphos.app.core.domain.agent.IntentAgent
import com.morphos.app.core.domain.model.ContextSnapshot
import com.morphos.app.core.domain.model.IntentType
import com.morphos.app.core.domain.model.WidgetIntent
import kotlinx.coroutines.withContext
import javax.inject.Inject

class IntentAgentImpl @Inject constructor(
    private val aiEngineManager: AIEngineManager,
    private val dispatchers: AppDispatchers
) : IntentAgent {

    private fun sanitizeInput(input: String): String {
        return input
            .replace(Regex("<[^>]*>"), "") // Strip HTML tags
            .replace(Regex("(?i)javascript:"), "") // Strip javascript protocol
            .replace(Regex("(?i)<script.*?>.*?</script>"), "") // Strip script tags
            .replace(Regex("['\"#;\\-\\-*]"), "") // Strip simple SQL/comment chars
            .trim()
    }

    override suspend fun parseIntent(
        rawInput: String,
        context: ContextSnapshot
    ): AppResult<WidgetIntent> = withContext(dispatchers.default) {
        safeCall {
            val sanitizedInput = sanitizeInput(rawInput)
            val lower = sanitizedInput.lowercase()

            // Step 1: Keyword matching fallback
            val keywordType = when {
                lower.contains("study") || lower.contains("learn") || lower.contains("homework") -> IntentType.CREATE_STUDY_WIDGET
                lower.contains("travel") || lower.contains("trip") || lower.contains("flight") || lower.contains("hotel") -> IntentType.CREATE_TRAVEL_WIDGET
                lower.contains("fitness") || lower.contains("workout") || lower.contains("gym") || lower.contains("steps") || lower.contains("run") -> IntentType.CREATE_FITNESS_WIDGET
                lower.contains("weather") || lower.contains("temperature") || lower.contains("forecast") -> IntentType.CREATE_WEATHER_WIDGET
                lower.contains("todo") || lower.contains("task") || lower.contains("reminder") || lower.contains("deadline") -> IntentType.CREATE_PRODUCTIVITY_WIDGET
                lower.contains("finance") || lower.contains("money") || lower.contains("budget") || lower.contains("expense") -> IntentType.CREATE_FINANCE_WIDGET
                else -> null
            }

            if (keywordType != null) {
                return@safeCall WidgetIntent(
                    rawInput = sanitizedInput,
                    intentType = keywordType,
                    slots = emptyMap(),
                    confidence = 0.7f,
                    requiresCloudFallback = false
                )
            }

            // Step 2: Tier 0 local model
            if (aiEngineManager.isTier0Available()) {
                val enumList = IntentType.values().joinToString(", ") { it.name }
                val prompt = "Classify this widget request into one of: [$enumList]. Request: '${sanitizedInput}'. Reply with only the enum value name."
                val response = aiEngineManager.inferTier0(prompt).trim()
                val parsedType = try {
                    IntentType.valueOf(response)
                } catch (e: Exception) {
                    IntentType.UNKNOWN
                }

                if (parsedType != IntentType.UNKNOWN) {
                    return@safeCall WidgetIntent(
                        rawInput = sanitizedInput,
                        intentType = parsedType,
                        slots = emptyMap(),
                        confidence = 0.85f,
                        requiresCloudFallback = false
                    )
                }
            }

            // Step 3: Cloud Fallback
            WidgetIntent(
                rawInput = sanitizedInput,
                intentType = IntentType.UNKNOWN,
                slots = emptyMap(),
                confidence = 0.1f,
                requiresCloudFallback = true
            )
        }
    }
}
