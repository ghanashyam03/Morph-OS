package com.morphos.app.core.domain.agent

import com.morphos.app.core.common.AppResult
import com.morphos.app.core.domain.model.ContextSnapshot
import com.morphos.app.core.domain.model.WidgetIntent

interface IntentAgent {
    suspend fun parseIntent(
        rawInput: String,
        context: ContextSnapshot
    ): AppResult<WidgetIntent>
}
