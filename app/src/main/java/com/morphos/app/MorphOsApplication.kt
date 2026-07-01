package com.morphos.app

import android.app.Application
import android.os.StrictMode
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.morphos.app.core.data.db.MorphOsDatabase
import com.morphos.app.core.data.worker.WorkScheduler
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class MorphOsApplication : Application(), Configuration.Provider {

    @Inject lateinit var database: MorphOsDatabase
    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var workScheduler: WorkScheduler

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        Thread.setDefaultUncaughtExceptionHandler(
            com.morphos.app.error.MorphOsUncaughtExceptionHandler(
                this, Thread.getDefaultUncaughtExceptionHandler()
            )
        )

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build()
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .build()
            )
        }

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            runCatching { database.openHelper.writableDatabase }
                .onFailure { Timber.e(it, "Database pre-warm failed") }
            runCatching { workScheduler.scheduleAll() }
                .onFailure { Timber.e(it, "WorkScheduler failed") }
        }
    }
}
