package com.morphos.app.core.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import okhttp3.OkHttpClient
import okhttp3.Request
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

    override suspend fun doWork(): Result {
        val filename = inputData.getString("model_filename") ?: return Result.failure()
        val url = inputData.getString("model_url") ?: return Result.failure()
        val expectedSha = inputData.getString("sha256") ?: return Result.failure()

        return try {
            val request = Request.Builder().url(url).build()
            val response = httpClient.newCall(request).execute()

            if (!response.isSuccessful) return Result.failure()

            val body = response.body ?: return Result.failure()
            val totalBytes = body.contentLength()

            val destinationFile = File(context.filesDir, filename)
            FileOutputStream(destinationFile).use { fos ->
                BufferedInputStream(body.byteStream()).use { bis ->
                    val buffer = ByteArray(8192)
                    var bytesRead = 0L
                    var read: Int

                    while (bis.read(buffer).also { read = it } != -1) {
                        fos.write(buffer, 0, read)
                        bytesRead += read
                        if (totalBytes > 0) {
                            val percent = (bytesRead * 100 / totalBytes).toInt()
                            setProgress(workDataOf("progress" to percent))
                        }
                    }
                    fos.flush()
                }
            }

            // Verify hash
            val calculatedSha = getSha256(destinationFile)
            if (!calculatedSha.equals(expectedSha, ignoreCase = true)) {
                if (destinationFile.exists()) {
                    destinationFile.delete()
                }
                Result.failure()
            } else {
                Result.success()
            }
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun getSha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(8192)
        FileInputStream(file).use { fis ->
            var read: Int
            while (fis.read(buffer).also { read = it } != -1) {
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
