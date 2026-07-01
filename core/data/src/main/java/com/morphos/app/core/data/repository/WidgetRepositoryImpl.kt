package com.morphos.app.core.data.repository

import com.morphos.app.core.common.AppDispatchers
import com.morphos.app.core.common.AppResult
import com.morphos.app.core.data.db.WidgetDao
import com.morphos.app.core.data.mapper.toDomain
import com.morphos.app.core.data.mapper.toEntity
import com.morphos.app.core.domain.model.WidgetConfig
import com.morphos.app.core.domain.repository.WidgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class WidgetRepositoryImpl @Inject constructor(
    private val widgetDao: WidgetDao,
    private val dispatchers: AppDispatchers
) : WidgetRepository {

    override fun getAllWidgets(): Flow<List<WidgetConfig>> {
        return widgetDao.getAllWidgets().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getWidgetById(id: String): WidgetConfig? = withContext(dispatchers.io) {
        try {
            widgetDao.getWidgetById(id)?.toDomain()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun saveWidget(widget: WidgetConfig): AppResult<Unit> = withContext(dispatchers.io) {
        try {
            widgetDao.insertWidget(widget.toEntity())
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e)
        }
    }

    override suspend fun deleteWidget(id: String): AppResult<Unit> = withContext(dispatchers.io) {
        try {
            widgetDao.deleteWidget(id)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e)
        }
    }

    override suspend fun updateWidget(widget: WidgetConfig): AppResult<Unit> = withContext(dispatchers.io) {
        try {
            widgetDao.updateWidget(widget.toEntity())
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e)
        }
    }

    override suspend fun getWidgetsByTemplate(templateId: String): List<WidgetConfig> = withContext(dispatchers.io) {
        try {
            widgetDao.getByTemplate(templateId).map { it.toDomain() }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
