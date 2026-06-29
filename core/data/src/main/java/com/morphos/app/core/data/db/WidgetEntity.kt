package com.morphos.app.core.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "widgets")
data class WidgetEntity(
    @PrimaryKey val id: String,
    val name: String,
    val templateId: String,
    val sizeClass: String,
    val configJson: String,
    val createdAt: Long,
    val lastModified: Long,
    val version: Int
)

@Dao
interface WidgetDao {
    @Query("SELECT * FROM widgets")
    fun getWidgets(): Flow<List<WidgetEntity>>

    @Query("SELECT * FROM widgets WHERE id = :id")
    suspend fun getWidget(id: String): WidgetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWidget(widget: WidgetEntity)

    @Query("DELETE FROM widgets WHERE id = :id")
    suspend fun deleteWidget(id: String)
}
