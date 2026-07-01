package com.morphos.app.core.data.worker

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.morphos.app.core.data.db.*
import com.morphos.app.core.domain.repository.SettingsRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OldMemoryPruneWorkerTest {

    private lateinit var context: Context
    private lateinit var db: MorphOsDatabase
    private lateinit var shortTermEventDao: ShortTermEventDao
    private lateinit var longTermMemoryDao: LongTermMemoryDao
    private lateinit var notificationLogDao: NotificationLogDao
    private lateinit var agentTaskDao: AgentTaskDao
    private val settingsRepository: SettingsRepository = mockk()

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, MorphOsDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        shortTermEventDao = db.shortTermEventDao()
        longTermMemoryDao = db.longTermMemoryDao()
        notificationLogDao = db.notificationLogDao()
        agentTaskDao = db.agentTaskDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun pruneWorker_deletesOldEvents() = runBlocking {
        coEvery { settingsRepository.getRetentionDays() } returns 7 // 7 days retention

        val now = System.currentTimeMillis()
        val oldCutoff = now - (10 * 86400000L) // 10 days ago (should be pruned)
        val recentTime = now - (2 * 86400000L) // 2 days ago (should be kept)

        // Seed data
        shortTermEventDao.insertEvent(ShortTermEventEntity("1", "TAPPED", "widget", oldCutoff))
        shortTermEventDao.insertEvent(ShortTermEventEntity("2", "TAPPED", "widget", recentTime))

        val worker = TestListenableWorkerBuilder<OldMemoryPruneWorker>(context)
            .setWorkerFactory(object : androidx.work.WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: androidx.work.WorkerParameters
                ): ListenableWorker {
                    return OldMemoryPruneWorker(
                        appContext, workerParameters, settingsRepository,
                        shortTermEventDao, longTermMemoryDao, notificationLogDao, agentTaskDao
                    )
                }
            })
            .build()

        val result = worker.doWork()
        assertEquals(ListenableWorker.Result.success(), result)

        // Verify old pruned, recent kept
        val events = shortTermEventDao.getAllEventsRaw()
        assertEquals(1, events.size)
        assertEquals("2", events[0].id)
    }

    @Test
    fun pruneWorker_keepsRecentEvents() = runBlocking {
        coEvery { settingsRepository.getRetentionDays() } returns 30

        val now = System.currentTimeMillis()
        val recentTime = now - (5 * 86400000L) // 5 days ago

        shortTermEventDao.insertEvent(ShortTermEventEntity("recent_1", "TAPPED", "widget", recentTime))

        val worker = TestListenableWorkerBuilder<OldMemoryPruneWorker>(context)
            .setWorkerFactory(object : androidx.work.WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: androidx.work.WorkerParameters
                ): ListenableWorker {
                    return OldMemoryPruneWorker(
                        appContext, workerParameters, settingsRepository,
                        shortTermEventDao, longTermMemoryDao, notificationLogDao, agentTaskDao
                    )
                }
            })
            .build()

        val result = worker.doWork()
        assertEquals(ListenableWorker.Result.success(), result)

        val events = shortTermEventDao.getAllEventsRaw()
        assertEquals(1, events.size)
        assertEquals("recent_1", events[0].id)
    }
}
