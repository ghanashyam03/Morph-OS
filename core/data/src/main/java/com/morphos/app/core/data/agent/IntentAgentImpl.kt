package com.morphos.app.core.data.agent

import com.morphos.app.core.domain.agent.IntentAgent
import com.morphos.app.core.domain.model.ContextSnapshot
import com.morphos.app.core.domain.model.WidgetIntent
import javax.inject.Inject

class IntentAgentImpl @Inject constructor() : IntentAgent {
    override suspend fun parseIntent(input: String, context: ContextSnapshot): WidgetIntent {
        TODO("Not yet implemented")
    }
}
