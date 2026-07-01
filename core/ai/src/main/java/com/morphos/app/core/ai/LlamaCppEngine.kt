package com.morphos.app.core.ai

import android.content.Context
import com.arm.aichat.AiChat
import com.arm.aichat.InferenceEngine
import com.arm.aichat.isModelLoaded
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LlamaCppEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val engine by lazy { AiChat.getInferenceEngine(context) }
    private val mutex = Mutex()
    private var loadedPath: String? = null
    private var loadedSystemPrompt: String? = null

    fun isTier0Loaded(): Boolean = modelFile(TIER_0_FILENAME).isFile
    fun isTier1Loaded(): Boolean = modelFile(TIER_1_FILENAME).isFile

    suspend fun inferTier0(prompt: String): String = infer(
        modelFile(TIER_0_FILENAME),
        CLASSIFIER_SYSTEM_PROMPT,
        prompt,
        48
    )

    suspend fun inferTier1(prompt: String): String = infer(
        modelFile(TIER_1_FILENAME),
        PLANNER_SYSTEM_PROMPT,
        prompt,
        512
    )

    suspend fun inferPlanning(prompt: String): String = infer(
        if (modelFile(TIER_1_FILENAME).isFile) modelFile(TIER_1_FILENAME) else modelFile(TIER_0_FILENAME),
        PLANNER_SYSTEM_PROMPT,
        prompt,
        512
    )

    private suspend fun infer(
        model: File,
        systemPrompt: String,
        userPrompt: String,
        maxTokens: Int
    ): String = mutex.withLock {
        require(model.isFile) { "Model is not downloaded: ${model.name}" }
        awaitInitialized()
        if (loadedPath != model.absolutePath || loadedSystemPrompt != systemPrompt || !engine.state.value.isModelLoaded) {
            if (engine.state.value.isModelLoaded) engine.cleanUp()
            engine.loadModel(model.absolutePath)
            engine.setSystemPrompt(systemPrompt)
            loadedPath = model.absolutePath
            loadedSystemPrompt = systemPrompt
        }
        val output = StringBuilder()
        engine.sendUserPrompt(userPrompt, maxTokens).collect { token -> output.append(token) }
        output.toString().trim().also {
            Timber.d("Local model response model=%s prompt=%s response=%s", model.name, userPrompt, it)
        }
    }

    private suspend fun awaitInitialized() {
        repeat(200) {
            when (val state = engine.state.value) {
                is InferenceEngine.State.Initialized,
                is InferenceEngine.State.ModelReady -> return
                is InferenceEngine.State.Error -> throw state.exception
                else -> delay(25)
            }
        }
        error("Timed out initializing llama.cpp")
    }

    fun close() {
        runCatching { if (engine.state.value.isModelLoaded) engine.cleanUp() }
        loadedPath = null
        loadedSystemPrompt = null
    }

    private fun modelFile(name: String) = File(context.filesDir, name)

    companion object {
        private const val TIER_0_FILENAME = "SmolLM2-135M-Instruct.Q4_K_M.gguf"
        private const val TIER_1_FILENAME = "qwen2.5-0.5b-instruct-q4_k_m.gguf"
        private const val CLASSIFIER_SYSTEM_PROMPT =
            "You classify Android widget requests. Return only the requested enum value, with no explanation."
        private const val PLANNER_SYSTEM_PROMPT =
            "You design Android widgets. Return only strict JSON matching the schema in the user request. Never use markdown."
    }
}
