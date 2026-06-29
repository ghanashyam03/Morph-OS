package com.morphos.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.morphos.app.core.data.worker.WorkScheduler
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class MorphOsApplication : Application() {

    @Inject
    lateinit var workScheduler: WorkScheduler

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        workScheduler.scheduleAll()
    }
}
