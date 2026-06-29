package com.morphos.app.core.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.OnConflictStrategy

@Entity(tableName = "embedding_store")
data class EmbeddingEntryEntity(
    @PrimaryKey val id: String,
    val sourceText: String,
    val embeddingBlob: ByteArray,
    val createdAt: Long
)

@Dao
interface EmbeddingEntryDao {
    @Query("SELECT * FROM embedding_store")
    suspend fun getAllEmbeddings(): List<EmbeddingEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmbedding(entry: EmbeddingEntryEntity)

    @Query("DELETE FROM embedding_store")
    suspend fun clearEmbeddings()
}
