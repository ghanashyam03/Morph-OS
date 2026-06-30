package com.morphos.app.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.morphos.app.core.common.AppDispatchers
import com.morphos.app.core.ai.ModelConfig
import com.morphos.app.core.ai.ModelDownloadManager
import com.morphos.app.core.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

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
                // Handled in UI, can track state here if needed
            }
        }
    }

    private fun startDownload() {
        viewModelScope.launch(dispatchers.io) {
            val config = ModelConfig(
                name = "smollm2-135m-q4",
                filename = "smollm2-135m-q4.gguf",
                url = "https://huggingface.co/second-state/SmolLM2-135M-Instruct-GGUF/resolve/main/smollm2-135m-instruct-q4_k_m.gguf",
                sha256 = "mock_sha",
                sizeBytes = 90_000_000L
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
        viewModelScope.launch(dispatchers.io) {
            _state.update { it.copy(isCompleting = true) }
            settingsRepository.setOnboardingComplete()
            _effects.emit(OnboardingEffect.NavigateToDashboard)
        }
    }
}
