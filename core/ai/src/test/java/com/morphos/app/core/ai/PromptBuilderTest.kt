package com.morphos.app.core.ai

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PromptBuilderTest {

    @Test
    fun intentClassificationPrompt_containsInputText() {
        val prompt = PromptBuilder.buildClassificationPrompt("build study tracker", listOf("CREATE_STUDY_WIDGET"))
        assertTrue(prompt.contains("build study tracker"))
    }

    @Test
    fun intentClassificationPrompt_containsAllEnumValues() {
        val types = listOf("CREATE_STUDY_WIDGET", "CREATE_FITNESS_WIDGET", "CREATE_WEATHER_WIDGET")
        val prompt = PromptBuilder.buildClassificationPrompt("input text", types)
        
        types.forEach { type ->
            assertTrue(prompt.contains(type))
        }
    }

    @Test
    fun widgetPlanPrompt_containsPluginList() {
        val prompt = PromptBuilder.buildPlanningPrompt("input", "context", "clock, calendar, weather")
        assertTrue(prompt.contains("clock, calendar, weather"))
    }

    @Test
    fun widgetPlanPrompt_containsJsonSchema() {
        val prompt = PromptBuilder.buildPlanningPrompt("input", "context", "plugins")
        // Planning prompt instructs model to return raw JSON matching schema
        assertTrue(prompt.contains("JSON"))
    }

    @Test
    fun summarizationPrompt_containsEventList() {
        val eventsLog = "Event 1: TAPPED, Event 2: SAVED"
        val prompt = PromptBuilder.buildSummarizationPrompt(eventsLog)
        assertTrue(prompt.contains(eventsLog))
    }
}
