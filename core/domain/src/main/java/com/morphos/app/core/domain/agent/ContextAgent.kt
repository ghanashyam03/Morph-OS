package com.morphos.app.core.domain.agent

import com.morphos.app.core.domain.model.ContextSnapshot
import kotlinx.coroutines.flow.StateFlow

interface ContextAgent {
    val contextState: StateFlow<ContextSnapshot?>
    suspend fun refreshContext()
}
