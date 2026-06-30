package com.morphos.app.core.data.worker

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.morphos.app.core.common.AppResult
import com.morphos.app.core.domain.agent.ContextAgent
import com.morphos.app.core.domain.model.ContextSnapshot
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ContextRefreshWorkerTest {

    private lateinit var context: Context
    private val contextAgent: ContextAgent = mockk()

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun workerSucceeds_returnsSuccess() = runBlocking {
        val dummySnapshot = ContextSnapshot(0, 0L, 80, "WiFi", 0.0, 0.0, false)
        coEvery { contextAgent.refresh() } returns AppResult.Success(dummySnapshot)

        val worker = TestListenableWorkerBuilder<ContextRefreshWorker>(context)
            .setWorkerFactory(object : androidx.work.WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: androidx.work.WorkerParameters
                ): ListenableWorker {
                    return ContextRefreshWorker(appContext, workerParameters, contextAgent)
                }
            })
            .build()

        val result = worker.doWork()
        assertEquals(ListenableWorker.Result.success(), result)
    }

    @Test
    fun workerExceptionOnFirstAttempt_returnsRetry() = runBlocking {
        coEvery { contextAgent.refresh() } returns AppResult.Error(Exception("Error"))

        val worker = TestListenableWorkerBuilder<ContextRefreshWorker>(context)
            .setRunAttemptCount(0)
            .setWorkerFactory(object : androidx.work.WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: androidx.work.WorkerParameters
                ): ListenableWorker {
                    return ContextRefreshWorker(appContext, workerParameters, contextAgent)
                }
            })
            .build()

        val result = worker.doWork()
        assertEquals(ListenableWorker.Result.retry(), result)
    }

    @Test
    fun workerExceptionOnThirdAttempt_returnsFailure() = runBlocking {
        coEvery { contextAgent.refresh() } returns AppResult.Error(Exception("Error"))

        val worker = TestListenableWorkerBuilder<ContextRefreshWorker>(context)
            .setRunAttemptCount(3)
            .setWorkerFactory(object : androidx.work.WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: androidx.work.WorkerParameters
                ): ListenableWorker {
                    return ContextRefreshWorker(appContext, workerParameters, contextAgent)
                }
            })
            .build()

        val result = worker.doWork()
        assertEquals(ListenableWorker.Result.failure(), result)
    }
}
