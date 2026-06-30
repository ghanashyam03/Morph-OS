package com.morphos.app.core.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "widgets")
data class WidgetEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val templateId: String,
    val sizeClass: String,               // enum name
    val configJson: String,              // Full WidgetConfig serialized as JSON
    val isPinned: Boolean,
    val createdAt: Long,
    val lastModified: Long,
    val version: Int
)

@Dao
interface WidgetDao {
    @Query("SELECT * FROM widgets ORDER BY lastModified DESC")
    fun getAllWidgets(): Flow<List<WidgetEntity>>

    @Query("SELECT * FROM widgets WHERE id = :id")
    suspend fun getWidgetById(id: String): WidgetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWidget(widget: WidgetEntity)

    @Update
    suspend fun updateWidget(widget: WidgetEntity)

    @Query("DELETE FROM widgets WHERE id = :id")
    suspend fun deleteWidget(id: String)

    @Query("SELECT * FROM widgets WHERE templateId = :templateId")
    suspend fun getByTemplate(templateId: String): List<WidgetEntity>

    @Transaction
    suspend fun deleteWidgetWithCache(id: String, cacheDao: PluginDataCacheDao) {
        deleteWidget(id)
        cacheDao.invalidate(id)
    }
}
