package com.morphos.app.core.domain.agent

import com.morphos.app.core.domain.model.ContextSnapshot
import com.morphos.app.core.domain.model.WidgetIntent

interface IntentAgent {
    suspend fun parseIntent(input: String, context: ContextSnapshot): WidgetIntent
}
