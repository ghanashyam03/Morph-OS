package com.morphos.app.core.data.worker

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.morphos.app.core.common.AppResult
import com.morphos.app.core.domain.agent.AgentOrchestrator
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MemorySummarizationWorkerTest {

    private lateinit var context: Context
    private val agentOrchestrator: AgentOrchestrator = mockk()

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun summarizationSuccess_returnsSuccess() = runBlocking {
        coEvery { agentOrchestrator.triggerMemorySummarization() } returns AppResult.Success(Unit)

        val worker = TestListenableWorkerBuilder<MemorySummarizationWorker>(context)
            .setWorkerFactory(object : androidx.work.WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: androidx.work.WorkerParameters
                ): ListenableWorker {
                    return MemorySummarizationWorker(appContext, workerParameters, agentOrchestrator)
                }
            })
            .build()

        val result = worker.doWork()
        assertEquals(ListenableWorker.Result.success(), result)
    }
}
