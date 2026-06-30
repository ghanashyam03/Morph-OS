package com.morphos.app

import android.app.Application
import android.os.StrictMode
import androidx.appcompat.app.AppCompatDelegate
import com.morphos.app.core.data.db.MorphOsDatabase
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class MorphOsApplication : Application() {

    @Inject
    lateinit var database: MorphOsDatabase

    override fun onCreate() {
        super.onCreate()
        
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(
            com.morphos.app.error.MorphOsUncaughtExceptionHandler(this, defaultHandler)
        )
        
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            
            // Enable StrictMode in DEBUG
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build()
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .build()
            )
        }
        
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

        // Database Pre-warm on IO thread
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                database.query("SELECT 1", null).use { cursor ->
                    cursor.moveToFirst()
                }
            } catch (e: Exception) {
                Timber.e(e, "MorphOsApplication: Database pre-warm failed")
            }
        }
    }
}
