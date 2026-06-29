package com.morphos.app.core.domain.repository

import com.morphos.app.core.common.AppResult
import com.morphos.app.core.domain.model.WidgetConfig
import kotlinx.coroutines.flow.Flow

interface WidgetRepository {
    fun getAllWidgets(): Flow<List<WidgetConfig>>
    suspend fun getWidgetById(id: String): WidgetConfig?
    suspend fun saveWidget(widget: WidgetConfig): AppResult<Unit>
    suspend fun deleteWidget(id: String): AppResult<Unit>
    suspend fun updateWidget(widget: WidgetConfig): AppResult<Unit>
    suspend fun getWidgetsByTemplate(templateId: String): List<WidgetConfig>
}
