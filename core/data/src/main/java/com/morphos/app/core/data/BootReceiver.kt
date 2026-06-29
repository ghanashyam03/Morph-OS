package com.morphos.app.core.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.morphos.app.core.data.worker.WorkScheduler
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var workScheduler: WorkScheduler

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            workScheduler.scheduleAll()
        }
    }
}
