package com.morphos.app.core.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.morphos.app.core.data.worker.ContextRefreshWorker

class ContextRefreshReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return
        val request = OneTimeWorkRequestBuilder<ContextRefreshWorker>().build()
        WorkManager.getInstance(context).enqueue(request)
    }
}
