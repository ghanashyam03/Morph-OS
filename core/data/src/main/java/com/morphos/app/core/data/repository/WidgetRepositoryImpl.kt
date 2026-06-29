package com.morphos.app.core.data.repository

import com.morphos.app.core.domain.model.WidgetConfig
import com.morphos.app.core.domain.repository.WidgetRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class WidgetRepositoryImpl @Inject constructor() : WidgetRepository {
    override fun getWidgets(): Flow<List<WidgetConfig>> {
        TODO("Not yet implemented")
    }

    override suspend fun getWidget(id: String): WidgetConfig? {
        TODO("Not yet implemented")
    }

    override suspend fun saveWidget(config: WidgetConfig) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteWidget(id: String) {
        TODO("Not yet implemented")
    }
}
