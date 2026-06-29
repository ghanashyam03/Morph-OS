package com.morphos.app.core.ai

import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

@Singleton
class LlamaCppEngine @Inject constructor() {

    private var tier0Handle: Long = 0L
    private var tier1Handle: Long = 0L

    companion object {
        private var isLibLoaded = false
        init {
            try {
                System.loadLibrary("llama_android")
                isLibLoaded = true
            } catch (e: UnsatisfiedLinkError) {
                Timber.w("LlamaCppEngine: llama_android native library not found. Falling back to mock engine.")
            }
        }
    }

    // Native JNI Declarations
    private external fun loadModel(path: String): Long
    private external fun freeModel(handle: Long)
    private external fun nativeInfer(handle: Long, prompt: String, maxTokens: Int, temp: Float): String

    @Synchronized
    fun initTier0(path: String) {
        if (tier0Handle != 0L) return
        if (isLibLoaded && File(path).exists()) {
            try {
                tier0Handle = loadModel(path)
            } catch (e: Exception) {
                Timber.e(e, "LlamaCppEngine: Failed to load Tier 0 model from path: $path")
            }
        }
    }

    @Synchronized
    fun initTier1(path: String) {
        if (tier1Handle != 0L) return
        if (isLibLoaded && File(path).exists()) {
            try {
                tier1Handle = loadModel(path)
            } catch (e: Exception) {
                Timber.e(e, "LlamaCppEngine: Failed to load Tier 1 model from path: $path")
            }
        }
    }

    fun isTier0Loaded(): Boolean = tier0Handle != 0L || !isLibLoaded
    fun isTier1Loaded(): Boolean = tier1Handle != 0L || !isLibLoaded

    fun inferTier0(prompt: String): String {
        if (tier0Handle == 0L || !isLibLoaded) {
            // Mock inference fallback
            val lower = prompt.lowercase()
            return when {
                lower.contains("study") || lower.contains("exam") || lower.contains("homework") -> "CREATE_STUDY_WIDGET"
                lower.contains("travel") || lower.contains("trip") || lower.contains("flight") -> "CREATE_TRAVEL_WIDGET"
                lower.contains("fitness") || lower.contains("workout") || lower.contains("steps") -> "CREATE_FITNESS_WIDGET"
                lower.contains("weather") || lower.contains("temperature") -> "CREATE_WEATHER_WIDGET"
                lower.contains("todo") || lower.contains("task") || lower.contains("reminder") -> "CREATE_PRODUCTIVITY_WIDGET"
                else -> "CREATE_CUSTOM_WIDGET"
            }
        }
        return nativeInfer(tier0Handle, prompt, 64, 0.2f)
    }

    fun inferTier1(prompt: String): String {
        if (tier1Handle == 0L || !isLibLoaded) {
            // Mock inference fallback returning rule JSON representation
            return """
                {
                  "widgetIntentRaw": "custom request",
                  "selectedTemplateId": "TPL_CARD_DUAL",
                  "suggestedName": "Adaptive Widget",
                  "suggestedDescription": "Locally planned adaptive widget",
                  "slotAssignments": {"slot1": "weather", "slot2": "clock"},
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
        }
        return nativeInfer(tier1Handle, prompt, 256, 0.3f)
    }

    @Synchronized
    fun close() {
        if (isLibLoaded) {
            if (tier0Handle != 0L) {
                freeModel(tier0Handle)
                tier0Handle = 0L
            }
            if (tier1Handle != 0L) {
                freeModel(tier1Handle)
                tier1Handle = 0L
            }
        }
    }
}
