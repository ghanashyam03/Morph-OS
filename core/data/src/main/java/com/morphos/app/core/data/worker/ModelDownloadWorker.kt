package com.morphos.app.core.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.content.pm.ServiceInfo
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
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest

@HiltWorker
class ModelDownloadWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val httpClient: OkHttpClient
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val filename = inputData.getString(KEY_FILENAME)
            ?: return failure("Missing model filename")
        val url = inputData.getString(KEY_URL)
            ?: return failure("Missing model URL")
        val expectedSha = inputData.getString(KEY_SHA).orEmpty()
        val expectedSize = inputData.getLong(KEY_SIZE, 0L)
        val destination = File(context.filesDir, filename)
        val partial = File(context.filesDir, "$filename.part")

        return try {
            destination.parentFile?.mkdirs()
            if (isValidExistingFile(destination, expectedSha, expectedSize)) {
                setProgress(workDataOf(KEY_PROGRESS to 100))
                return Result.success(workDataOf("path" to destination.absolutePath))
            }
            if (destination.exists()) destination.delete()

            val requiredBytes = (expectedSize - partial.length()).coerceAtLeast(0L)
            if (requiredBytes > 0 && context.filesDir.usableSpace < requiredBytes + MIN_FREE_SPACE) {
                return failure("Not enough storage space to download the model")
            }

            setForeground(createForegroundInfo(progressFor(partial.length(), expectedSize)))
            download(url, partial, expectedSize)

            if (expectedSize > 0 && partial.length() != expectedSize) {
                throw IOException("Incomplete download: ${partial.length()} of $expectedSize bytes")
            }
            if (expectedSha.matches(Regex("[0-9a-fA-F]{64}")) &&
                !sha256(partial).equals(expectedSha, ignoreCase = true)
            ) {
                partial.delete()
                return failure("Downloaded model failed integrity verification")
            }
            if (!partial.renameTo(destination)) {
                partial.copyTo(destination, overwrite = true)
                partial.delete()
            }
            setProgress(workDataOf(KEY_PROGRESS to 100))
            Result.success(workDataOf("path" to destination.absolutePath))
        } catch (e: Exception) {
            Timber.e(e, "Model download failed")
            if (runAttemptCount < MAX_RETRIES && e is IOException) Result.retry()
            else failure(e.message ?: "Model download failed")
        }
    }

    private suspend fun download(url: String, partial: File, expectedSize: Long) {
        var downloaded = partial.length()
        val request = Request.Builder().url(url).apply {
            if (downloaded > 0L) header("Range", "bytes=$downloaded-")
        }.build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Server returned HTTP ${response.code}")
            }
            if (downloaded > 0 && response.code != 206) {
                partial.delete()
                downloaded = 0L
            }
            val body = response.body ?: throw IOException("Server returned an empty response")
            val total = when {
                expectedSize > 0 -> expectedSize
                response.code == 206 -> downloaded + body.contentLength()
                else -> body.contentLength()
            }
            FileOutputStream(partial, downloaded > 0).use { output ->
                body.byteStream().buffered().use { input ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE * 4)
                    var lastProgress = progressFor(downloaded, total)
                    while (true) {
                        if (isStopped) throw IOException("Download cancelled")
                        val count = input.read(buffer)
                        if (count < 0) break
                        output.write(buffer, 0, count)
                        downloaded += count
                        val progress = progressFor(downloaded, total)
                        if (progress != lastProgress) {
                            lastProgress = progress
                            setProgress(workDataOf(KEY_PROGRESS to progress, KEY_BYTES to downloaded, KEY_TOTAL to total))
                            setForeground(createForegroundInfo(progress))
                        }
                    }
                    output.fd.sync()
                }
            }
        }
    }

    private fun isValidExistingFile(file: File, sha: String, size: Long): Boolean {
        if (!file.isFile || file.length() == 0L) return false
        if (size > 0 && file.length() != size) return false
        return !sha.matches(Regex("[0-9a-fA-F]{64}")) || sha256(file).equals(sha, true)
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE * 4)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private fun createForegroundInfo(progress: Int): ForegroundInfo {
        val manager = context.getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Model downloads", NotificationManager.IMPORTANCE_LOW)
            )
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle("Downloading AI model")
            .setContentText("$progress% complete")
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setProgress(100, progress, false)
            .build()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ForegroundInfo(NOTIFICATION_ID, notification)
        }
    }

    private fun failure(message: String) = Result.failure(workDataOf(KEY_ERROR to message))

    companion object {
        const val KEY_FILENAME = "model_filename"
        const val KEY_URL = "model_url"
        const val KEY_SHA = "sha256"
        const val KEY_SIZE = "size_bytes"
        const val KEY_PROGRESS = "progress"
        const val KEY_ERROR = "error"
        const val KEY_BYTES = "bytes_downloaded"
        const val KEY_TOTAL = "total_bytes"
        private const val CHANNEL_ID = "model_downloads"
        private const val NOTIFICATION_ID = 4101
        private const val MAX_RETRIES = 3
        private const val MIN_FREE_SPACE = 64L * 1024 * 1024

        fun progressFor(downloaded: Long, total: Long): Int =
            if (total <= 0) 0 else ((downloaded * 100L) / total).toInt().coerceIn(0, 99)
    }
}
