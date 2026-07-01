package com.morphos.app.feature.settings

import com.morphos.app.core.ai.ModelDownloadManager
import com.morphos.app.core.common.AppDispatchers
import com.morphos.app.core.common.AppResult
import com.morphos.app.core.common.NoParams
import com.morphos.app.core.domain.repository.SettingsRepository
import com.morphos.app.core.domain.repository.UserPreferences
import com.morphos.app.core.domain.usecase.widget.ClearAllMemoryUseCase
import com.morphos.app.core.domain.usecase.widget.GetUserPreferencesUseCase
import com.morphos.app.core.domain.usecase.widget.UpdateUserPreferencesUseCase
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockKExtension::class)
class SettingsViewModelTest {

    @MockK
    lateinit var getUserPreferencesUseCase: GetUserPreferencesUseCase

    @MockK
    lateinit var updateUserPreferencesUseCase: UpdateUserPreferencesUseCase

    @MockK
    lateinit var clearAllMemoryUseCase: ClearAllMemoryUseCase

    @MockK
    lateinit var modelDownloadManager: ModelDownloadManager

    @MockK
    lateinit var settingsRepository: SettingsRepository

    private lateinit var viewModel: SettingsViewModel
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testDispatchers = AppDispatchers(
        main = testDispatcher,
        io = testDispatcher,
        default = testDispatcher,
        unconfined = testDispatcher
    )

    private val dummyPrefs = UserPreferences(isCloudAiEnabled = false, retentionDays = 30)

    @BeforeEach
    fun setUp() {
        every { getUserPreferencesUseCase(NoParams) } returns flowOf(AppResult.Success(dummyPrefs))
    }

    private fun initViewModel() {
        viewModel = SettingsViewModel(
            getUserPreferencesUseCase,
            updateUserPreferencesUseCase,
            clearAllMemoryUseCase,
            modelDownloadManager,
            settingsRepository,
            testDispatchers
        )
    }

    @Test
    fun init_loadsPreferences() = runTest {
        initViewModel()

        assertEquals(dummyPrefs, viewModel.state.value.preferences)
        assertEquals(false, viewModel.state.value.isLoading)
    }

    @Test
    fun toggleCloudAi_updatesPreferences() = runTest {
        val updated = dummyPrefs.copy(isCloudAiEnabled = true)
        coEvery { updateUserPreferencesUseCase(updated) } returns AppResult.Success(Unit)

        initViewModel()
        viewModel.processIntent(SettingsIntent.ToggleCloudAi(true))

        coVerify(exactly = 1) { updateUserPreferencesUseCase(updated) }
    }

    @Test
    fun clearMemory_callsUseCase() = runTest {
        coEvery { clearAllMemoryUseCase(NoParams) } returns AppResult.Success(Unit)

        initViewModel()
        viewModel.processIntent(SettingsIntent.ClearAllMemory)

        coVerify(exactly = 1) { clearAllMemoryUseCase(NoParams) }
        assertEquals("All memory cleared successfully", viewModel.state.value.message)
    }

    @Test
    fun downloadTier1_callsDownloadManager() = runTest {
        coEvery { modelDownloadManager.downloadModel(any()) } returns flowOf(0.5f)

        initViewModel()
        viewModel.processIntent(SettingsIntent.DownloadTier1Model)

        coVerify(exactly = 1) { modelDownloadManager.downloadModel(any()) }
        assertTrue(viewModel.state.value.isDownloadingTier1)
    }
}
