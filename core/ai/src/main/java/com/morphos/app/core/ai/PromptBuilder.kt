package com.morphos.app.core.ai

object PromptBuilder {

    fun buildClassificationPrompt(rawInput: String, intentTypes: List<String>): String {
        val listStr = intentTypes.joinToString(", ")
        return "Classify this user request into one of the following Intent Types: [$listStr]. Request: '$rawInput'. Reply with only the name of the matched Intent Type."
    }

    fun buildPlanningPrompt(rawInput: String, contextStr: String, availablePluginsStr: String): String {
        return """
            You are an Android Widget Planner. Design a widget plan matching this intent: '$rawInput'.
            Available plugins: [$availablePluginsStr].
            Current Context: $contextStr.
            Reply with ONLY a valid raw JSON object matching the required schema.
        """.trimIndent()
    }

    fun buildSummarizationPrompt(eventsLog: String): String {
        return "Summarize the following user activity log into a brief behavior summary:\n$eventsLog"
    }
}
