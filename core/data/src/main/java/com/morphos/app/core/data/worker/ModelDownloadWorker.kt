package com.morphos.app.core.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest

@HiltWorker
class ModelDownloadWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val httpClient: OkHttpClient
) : CoroutineWorker(context, params) {

    companion object {
        const val CHANNEL_ID = "morphos_model_download"
        const val NOTIF_ID = 1001
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        createNotificationChannel()
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("MorphOS")
            .setContentText("Downloading AI model...")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOngoing(true)
            .build()
        return ForegroundInfo(NOTIF_ID, notification)
    }

    override suspend fun doWork(): Result {
        val filename = inputData.getString("model_filename") ?: return Result.failure()
        val url = inputData.getString("model_url") ?: return Result.failure()
        val expectedSha = inputData.getString("sha256") ?: return Result.failure()

        setForeground(getForegroundInfo())

        return try {
            Timber.d("ModelDownloadWorker: Starting download from $url")
            val request = Request.Builder().url(url).build()
            val response = httpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                Timber.e("ModelDownloadWorker: HTTP error ${response.code}")
                return Result.failure(workDataOf("error" to "HTTP ${response.code}"))
            }

            val body = response.body ?: return Result.failure(workDataOf("error" to "Empty body"))
            val totalBytes = body.contentLength()
            Timber.d("ModelDownloadWorker: Content-Length=$totalBytes")

            val destinationFile = File(context.filesDir, filename)
            FileOutputStream(destinationFile).use { fos ->
                BufferedInputStream(body.byteStream()).use { bis ->
                    val buffer = ByteArray(32768)
                    var bytesRead = 0L
                    var read: Int
                    var lastReportedPercent = -1

                    while (bis.read(buffer).also { read = it } != -1) {
                        fos.write(buffer, 0, read)
                        bytesRead += read
                        if (totalBytes > 0) {
                            val percent = (bytesRead * 100 / totalBytes).toInt()
                            if (percent != lastReportedPercent) {
                                lastReportedPercent = percent
                                setProgress(workDataOf("progress" to percent))
                            }
                        }
                    }
                    fos.flush()
                }
            }

            Timber.d("ModelDownloadWorker: Download complete, size=${destinationFile.length()}")

            // Only verify SHA256 when a real 64-char hash is provided
            if (expectedSha.length == 64) {
                val calculatedSha = getSha256(destinationFile)
                if (!calculatedSha.equals(expectedSha, ignoreCase = true)) {
                    Timber.e("ModelDownloadWorker: SHA256 mismatch!")
                    if (destinationFile.exists()) destinationFile.delete()
                    return Result.failure(workDataOf("error" to "SHA256 mismatch"))
                }
            }

            setProgress(workDataOf("progress" to 100))
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "ModelDownloadWorker: Download failed")
            Result.failure(workDataOf("error" to (e.message ?: "Unknown error")))
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Model Downloads", NotificationManager.IMPORTANCE_LOW
            )
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    private fun getSha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(32768)
        FileInputStream(file).use { fis ->
            var read: Int
            while (fis.read(buffer).also { read = it } != -1) {
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
