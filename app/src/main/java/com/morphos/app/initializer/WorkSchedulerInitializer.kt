package com.morphos.app.initializer

import android.content.Context
import androidx.startup.Initializer
import androidx.work.WorkManagerInitializer
import com.morphos.app.core.data.worker.WorkScheduler
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

class WorkSchedulerInitializer : Initializer<WorkScheduler> {
    override fun create(context: Context): WorkScheduler {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WorkSchedulerInitializerEntryPoint::class.java
        )
        val scheduler = entryPoint.workScheduler()
        scheduler.scheduleAll()
        return scheduler
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = listOf(
        WorkManagerInitializer::class.java
    )

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WorkSchedulerInitializerEntryPoint {
        fun workScheduler(): WorkScheduler
    }
}
