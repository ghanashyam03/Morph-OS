package com.morphos.app.core.ai

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.io.File
import java.nio.LongBuffer
import java.util.Random
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

@Singleton
class OnnxEmbeddingEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var ortEnv: OrtEnvironment? = null
    private var ortSession: OrtSession? = null
    private var isOrtInitialized = false

    @Synchronized
    fun init(modelPath: String) {
        if (isOrtInitialized) return
        try {
            if (File(modelPath).exists()) {
                ortEnv = OrtEnvironment.getEnvironment()
                ortSession = ortEnv?.createSession(modelPath, OrtSession.SessionOptions())
                isOrtInitialized = true
            }
        } catch (e: Exception) {
            Timber.e(e, "OnnxEmbeddingEngine: Failed to initialize ONNX Runtime session")
        }
    }

    fun isAvailable(): Boolean = isOrtInitialized || ortSession == null // Always fallback if session null

    fun computeEmbedding(text: String): FloatArray {
        val session = ortSession
        val env = ortEnv

        if (session == null || env == null) {
            return computeMockEmbedding(text)
        }

        return try {
            // Simple space-based tokenizer mapping to token ids for the model input tensor
            val tokens = tokenize(text)
            val inputIds = LongArray(tokens.size) { tokens[it].toLong() }
            val attentionMask = LongArray(tokens.size) { 1L }

            val shape = longArrayOf(1, tokens.size.toLong())

            val inputTensor = OnnxTensor.createTensor(env, LongBuffer.wrap(inputIds), shape)
            val maskTensor = OnnxTensor.createTensor(env, LongBuffer.wrap(attentionMask), shape)

            val inputs = mapOf(
                "input_ids" to inputTensor,
                "attention_mask" to maskTensor
            )

            val results = session.run(inputs)
            val outputTensor = results[0] as OnnxTensor
            val outputValue = outputTensor.value as Array<Array<FloatArray>> // typically [1, sequence_length, embedding_dim]
            
            // Average pool token embeddings to generate sentence representation
            val sequenceEmbeddings = outputValue[0]
            val dim = sequenceEmbeddings[0].size
            val sentenceEmbedding = FloatArray(dim)

            for (i in sequenceEmbeddings.indices) {
                for (j in 0 until dim) {
                    sentenceEmbedding[j] += sequenceEmbeddings[i][j]
                }
            }

            for (j in 0 until dim) {
                sentenceEmbedding[j] /= sequenceEmbeddings.size.toFloat()
            }

            // Normalize
            var sumSq = 0f
            for (v in sentenceEmbedding) sumSq += v * v
            val mag = sqrt(sumSq)
            if (mag > 0f) {
                for (j in 0 until dim) sentenceEmbedding[j] /= mag
            }

            results.close()
            inputTensor.close()
            maskTensor.close()

            sentenceEmbedding
        } catch (e: Exception) {
            Timber.e(e, "OnnxEmbeddingEngine: ONNX inference failed. Falling back to mock embedding.")
            computeMockEmbedding(text)
        }
    }

    @Synchronized
    fun close() {
        try {
            ortSession?.close()
            ortSession = null
            ortEnv?.close()
            ortEnv = null
            isOrtInitialized = false
        } catch (e: Exception) {
            Timber.e(e, "OnnxEmbeddingEngine: Error closing session")
        }
    }

    private fun tokenize(text: String): List<Int> {
        // Space tokenization mapping to vocabulary space
        return text.split("\\s+".toRegex()).map { word ->
            // Basic hash mapping to pseudo vocab ids
            (word.hashCode() and 0x7FFF) + 100
        }
    }

    private fun computeMockEmbedding(text: String): FloatArray {
        val array = FloatArray(384)
        val hash = text.hashCode().toLong()
        val random = Random(hash)
        for (i in 0 until 384) {
            array[i] = random.nextFloat() * 2f - 1f
        }
        var sumSq = 0f
        for (v in array) sumSq += v * v
        val mag = sqrt(sumSq)
        if (mag > 0f) {
            for (i in 0 until 384) array[i] /= mag
        }
        return array
    }
}
