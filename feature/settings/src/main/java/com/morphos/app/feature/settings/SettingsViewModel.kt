package com.morphos.app.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.morphos.app.core.common.AppDispatchers
import com.morphos.app.core.common.AppResult
import com.morphos.app.core.common.NoParams
import com.morphos.app.core.ai.ModelConfig
import com.morphos.app.core.ai.ModelDownloadManager
import com.morphos.app.core.domain.model.NotificationPreference
import com.morphos.app.core.domain.repository.SettingsRepository
import com.morphos.app.core.domain.repository.UserPreferences
import com.morphos.app.core.domain.usecase.widget.ClearAllMemoryUseCase
import com.morphos.app.core.domain.usecase.widget.GetUserPreferencesUseCase
import com.morphos.app.core.domain.usecase.widget.UpdateUserPreferencesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

import androidx.compose.runtime.Stable

@Stable
data class SettingsState(
    val preferences: UserPreferences = UserPreferences(),
    val isLoading: Boolean = true,
    val isTier0Downloaded: Boolean = false,
    val isTier1Downloaded: Boolean = false,
    val isEmbeddingModelDownloaded: Boolean = false,
    val tier1DownloadProgress: Int = 0,
    val isDownloadingTier1: Boolean = false,
    val message: String? = null
)

sealed class SettingsIntent {
    data class ToggleCloudAi(val enabled: Boolean) : SettingsIntent()
    data class ToggleNotifications(val pref: NotificationPreference) : SettingsIntent()
    object DownloadTier1Model : SettingsIntent()
    object ClearAllMemory : SettingsIntent()
    data class UpdateRetentionDays(val days: Int) : SettingsIntent()
    object ClearMessage : SettingsIntent()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getUserPreferencesUseCase: GetUserPreferencesUseCase,
    private val updateUserPreferencesUseCase: UpdateUserPreferencesUseCase,
    private val clearAllMemoryUseCase: ClearAllMemoryUseCase,
    private val modelDownloadManager: ModelDownloadManager,
    private val settingsRepository: SettingsRepository,
    private val dispatchers: AppDispatchers
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        _state.update {
            it.copy(
                isLoading = false,
                isDownloadingTier1 = false,
                message = throwable.message ?: "Unknown error occurred"
            )
        }
    }

    init {
        viewModelScope.launch(dispatchers.io + exceptionHandler) {
            getUserPreferencesUseCase(NoParams).collect { result ->
                when (result) {
                    is AppResult.Success -> {
                        _state.update { it.copy(preferences = result.data, isLoading = false) }
                    }
                    is AppResult.Error -> {
                        _state.update { it.copy(isLoading = false, message = result.message) }
                    }
                    AppResult.Loading -> {
                        _state.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }

    fun processIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.ToggleCloudAi -> {
                updatePrefs { it.copy(isCloudAiEnabled = intent.enabled) }
            }
            is SettingsIntent.ToggleNotifications -> {
                updatePrefs { it.copy(notificationPreference = intent.pref) }
            }
            SettingsIntent.DownloadTier1Model -> {
                _state.update { it.copy(isDownloadingTier1 = true) }
                downloadTier1()
            }
            SettingsIntent.ClearAllMemory -> {
                viewModelScope.launch(dispatchers.io + exceptionHandler) {
                    when (val result = clearAllMemoryUseCase(NoParams)) {
                        is AppResult.Success -> {
                            _state.update { it.copy(message = "All memory cleared successfully") }
                        }
                        is AppResult.Error -> {
                            _state.update { it.copy(message = "Error: ${result.message}") }
                        }
                        AppResult.Loading -> {}
                    }
                }
            }
            is SettingsIntent.UpdateRetentionDays -> {
                updatePrefs { it.copy(retentionDays = intent.days) }
            }
            SettingsIntent.ClearMessage -> {
                _state.update { it.copy(message = null) }
            }
        }
    }

    private fun updatePrefs(transform: (UserPreferences) -> UserPreferences) {
        viewModelScope.launch(dispatchers.io + exceptionHandler) {
            val updated = transform(_state.value.preferences)
            when (val result = updateUserPreferencesUseCase(updated)) {
                is AppResult.Success -> {
                    _state.update { it.copy(preferences = updated) }
                }
                is AppResult.Error -> {
                    _state.update { it.copy(message = "Failed to update settings: ${result.message}") }
                }
                AppResult.Loading -> {}
            }
        }
    }

    private fun downloadTier1() {
        viewModelScope.launch(dispatchers.io + exceptionHandler) {
            val config = ModelConfig(
                name = "gemma3-1b",
                filename = "gemma-3-1b-it-q4_k_m.gguf",
                url = "https://huggingface.co/google/gemma-3-1b-it-GGUF/resolve/main/gemma-3-1b-it-q4_k_m.gguf",
                sha256 = "mock_sha",
                sizeBytes = 900_000_000L
            )
            modelDownloadManager.downloadModel(config)
                .collect { progress ->
                    _state.update {
                        if (progress >= 1f) {
                            it.copy(
                                isDownloadingTier1 = false,
                                isTier1Downloaded = true,
                                tier1DownloadProgress = 100,
                                message = "Gemma-3 1B download complete!"
                            )
                        } else if (progress < 0f) {
                            it.copy(
                                isDownloadingTier1 = false,
                                tier1DownloadProgress = 0,
                                message = "Gemma-3 1B download failed"
                            )
                        } else {
                            it.copy(tier1DownloadProgress = (progress * 100).toInt())
                        }
                    }
                }
        }
    }
}
