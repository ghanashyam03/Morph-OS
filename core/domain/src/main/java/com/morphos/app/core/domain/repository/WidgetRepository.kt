package com.morphos.app.core.domain.repository

import com.morphos.app.core.domain.model.WidgetConfig
import kotlinx.coroutines.flow.Flow

interface WidgetRepository {
    fun getWidgets(): Flow<List<WidgetConfig>>
    suspend fun getWidget(id: String): WidgetConfig?
    suspend fun saveWidget(config: WidgetConfig)
    suspend fun deleteWidget(id: String)
}
