package com.morphos.app.core.data.worker

import org.junit.Assert.assertEquals
import org.junit.Test

class ModelDownloadWorkerTest {
    @Test
    fun progressIsBoundedAndDoesNotReportCompleteBeforeVerification() {
        assertEquals(0, ModelDownloadWorker.progressFor(0, 100))
        assertEquals(50, ModelDownloadWorker.progressFor(50, 100))
        assertEquals(99, ModelDownloadWorker.progressFor(100, 100))
        assertEquals(0, ModelDownloadWorker.progressFor(10, 0))
    }
}
