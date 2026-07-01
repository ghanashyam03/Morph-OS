package com.morphos.app.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.morphos.app.core.common.AppDispatchers
import com.morphos.app.core.ai.ModelConfig
import com.morphos.app.core.ai.ModelDownloadManager
import com.morphos.app.core.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

import androidx.compose.runtime.Stable

@Stable
data class OnboardingState(
    val currentPage: Int = 0,
    val totalPages: Int = 4,
    val isAiDownloadStarted: Boolean = false,
    val tier0DownloadProgress: Int = 0,
    val isCompleting: Boolean = false
)

sealed class OnboardingIntent {
    object NextPage : OnboardingIntent()
    object PreviousPage : OnboardingIntent()
    object StartTier0Download : OnboardingIntent()
    object SkipDownload : OnboardingIntent()
    object CompleteOnboarding : OnboardingIntent()
    data class GrantPermission(val permission: String) : OnboardingIntent()
}

sealed class OnboardingEffect {
    object NavigateToDashboard : OnboardingEffect()
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val modelDownloadManager: ModelDownloadManager,
    private val dispatchers: AppDispatchers
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<OnboardingEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<OnboardingEffect> = _effects.asSharedFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        _state.update { it.copy(isCompleting = false, isAiDownloadStarted = false) }
    }

    fun processIntent(intent: OnboardingIntent) {
        when (intent) {
            OnboardingIntent.NextPage -> {
                _state.update {
                    if (it.currentPage < it.totalPages - 1) {
                        it.copy(currentPage = it.currentPage + 1)
                    } else {
                        it
                    }
                }
            }
            OnboardingIntent.PreviousPage -> {
                _state.update {
                    if (it.currentPage > 0) {
                        it.copy(currentPage = it.currentPage - 1)
                    } else {
                        it
                    }
                }
            }
            OnboardingIntent.StartTier0Download -> {
                _state.update { it.copy(isAiDownloadStarted = true) }
                startDownload()
            }
            OnboardingIntent.SkipDownload -> {
                _state.update { it.copy(currentPage = it.currentPage + 1) }
            }
            OnboardingIntent.CompleteOnboarding -> {
                complete()
            }
            is OnboardingIntent.GrantPermission -> {
                // Handled in UI
            }
        }
    }

    private fun startDownload() {
        viewModelScope.launch(dispatchers.io + exceptionHandler) {
            val config = ModelConfig(
                name = "smollm2-135m-q4",
                filename = "SmolLM2-135M-Instruct.Q4_K_M.gguf",
                url = "https://huggingface.co/jc-builds/SmolLM2-135M-Instruct-Q4_K_M-GGUF/resolve/main/SmolLM2-135M-Instruct.Q4_K_M.gguf",
                sha256 = "8030f04528538d47bda434f6f0bdf3952c40a58123e4d5e755332f23731a8684",
                sizeBytes = 105_454_144L
            )
            modelDownloadManager.downloadModel(config)
                .collect { progress ->
                    _state.update {
                        if (progress >= 1f) {
                            it.copy(tier0DownloadProgress = 100, currentPage = it.currentPage + 1)
                        } else if (progress < 0f) {
                            it.copy(tier0DownloadProgress = 0, isAiDownloadStarted = false)
                        } else {
                            it.copy(tier0DownloadProgress = (progress * 100).toInt())
                        }
                    }
                }
        }
    }

    private fun complete() {
        viewModelScope.launch(dispatchers.io + exceptionHandler) {
            _state.update { it.copy(isCompleting = true) }
            settingsRepository.setOnboardingComplete()
            _effects.emit(OnboardingEffect.NavigateToDashboard)
        }
    }
}
