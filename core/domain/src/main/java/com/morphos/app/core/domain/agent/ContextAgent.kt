package com.morphos.app.core.domain.agent

import com.morphos.app.core.common.AppResult
import com.morphos.app.core.domain.model.ContextSnapshot
import kotlinx.coroutines.flow.StateFlow

interface ContextAgent {
    val contextFlow: StateFlow<ContextSnapshot>
    suspend fun refresh(): AppResult<ContextSnapshot>
    fun start()
    fun stop()
}
