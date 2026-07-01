package com.morphos.app.core.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.morphos.app.core.common.AppDispatchers
import com.morphos.app.core.common.AppResult
import com.morphos.app.core.data.db.EmbeddingDatabase
import com.morphos.app.core.data.db.EmbeddingEntryDao
import com.morphos.app.core.domain.model.EmbeddingEntry
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class EmbeddingRepositoryImplTest {

    private lateinit var db: EmbeddingDatabase
    private lateinit var dao: EmbeddingEntryDao
    private lateinit var repository: EmbeddingRepositoryImpl
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testDispatchers = AppDispatchers(
        main = testDispatcher,
        io = testDispatcher,
        default = testDispatcher,
        unconfined = testDispatcher
    )

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, EmbeddingDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.embeddingEntryDao()
        repository = EmbeddingRepositoryImpl(dao, testDispatchers)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun saveEmbedding_persisted() = runTest {
        val entry = EmbeddingEntry("id_1", "text content", floatArrayOf(1.0f, 0.0f, 0.0f), System.currentTimeMillis())
        val result = repository.saveEmbedding(entry)
        assertTrue(result is AppResult.Success)

        val all = repository.getAllEmbeddings()
        assertEquals(1, all.size)
        assertEquals("id_1", all[0].id)
    }

    @Test
    fun findSimilar_returnsMostSimilar() = runTest {
        // We insert two entries:
        // Entry A: [1.0, 0.0, 0.0] -> Perfectly parallel to query [1.0, 0.0, 0.0]
        // Entry B: [0.0, 1.0, 0.0] -> Orthogonal to query [1.0, 0.0, 0.0]
        val entryA = EmbeddingEntry("A", "text A", floatArrayOf(1f, 0f, 0f), System.currentTimeMillis())
        val entryB = EmbeddingEntry("B", "text B", floatArrayOf(0f, 1f, 0f), System.currentTimeMillis())

        repository.saveEmbedding(entryA)
        repository.saveEmbedding(entryB)

        val query = floatArrayOf(1f, 0f, 0f)
        val similar = repository.findSimilar(query, 2)

        assertEquals(2, similar.size)
        assertEquals("A", similar[0].id) // Most similar is A
        assertEquals("B", similar[1].id)
    }

    @Test
    fun findSimilar_handlesEmptyStore() = runTest {
        val similar = repository.findSimilar(floatArrayOf(1f, 0f, 0f), 5)
        assertTrue(similar.isEmpty())
    }

    @Test
    fun findSimilar_respectsTopK() = runTest {
        for (i in 1..10) {
            val entry = EmbeddingEntry("id_$i", "text $i", floatArrayOf(1f, 0f, 0f), System.currentTimeMillis())
            repository.saveEmbedding(entry)
        }

        val similar = repository.findSimilar(floatArrayOf(1f, 0f, 0f), 3)
        assertEquals(3, similar.size)
    }
}
