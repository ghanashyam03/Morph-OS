package com.morphos.app.error

import android.content.Context
import android.os.Build
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MorphOsUncaughtExceptionHandler(
    private val context: Context,
    private val defaultHandler: Thread.UncaughtExceptionHandler?
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            val crashDir = File(context.filesDir, "crashes").also { it.mkdirs() }
            val timestamp = System.currentTimeMillis()
            val crashFile = File(crashDir, "crash_$timestamp.txt")
            crashFile.writeText(buildCrashReport(throwable, timestamp))
        } catch (e: Exception) {
            // Ignore failure during crash logging
        }
        defaultHandler?.uncaughtException(thread, throwable)
    }

    private fun buildCrashReport(throwable: Throwable, timestamp: Long): String {
        val sw = StringWriter()
        throwable.printStackTrace(PrintWriter(sw))
        val stackTrace = sw.toString()

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
        val timeStr = dateFormat.format(Date(timestamp))

        val appVersion = try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }

        return """
            =========================================
            MORPHOS CRASH REPORT
            =========================================
            Time: $timeStr ($timestamp)
            App Version: $appVersion
            Device Model: ${Build.MODEL}
            OS Version: Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})
            Fingerprint: ${Build.FINGERPRINT}
            Thread: ${thread.name} (ID: ${thread.id})
            
            -----------------------------------------
            STACK TRACE
            -----------------------------------------
            $stackTrace
            =========================================
        """.trimIndent()
    }
}
