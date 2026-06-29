package com.morphos.app.core.data.db

import androidx.room.*

@Entity(tableName = "embedding_store")
data class EmbeddingEntryEntity(
    @PrimaryKey val id: String,
    val sourceText: String,
    val embeddingBlob: ByteArray,  // FloatArray serialized as ByteArray
    val createdAt: Long
)

@Dao
interface EmbeddingEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: EmbeddingEntryEntity)

    @Query("SELECT * FROM embedding_store")
    suspend fun getAll(): List<EmbeddingEntryEntity>

    @Query("DELETE FROM embedding_store WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM embedding_store WHERE createdAt < :cutoff")
    suspend fun pruneOld(cutoff: Long)
}
