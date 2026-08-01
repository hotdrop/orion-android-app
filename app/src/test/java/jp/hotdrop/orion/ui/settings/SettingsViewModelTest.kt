package jp.hotdrop.orion.ui.settings

import jp.hotdrop.orion.data.settings.SettingsRepository
import jp.hotdrop.orion.data.settings.normalizeGoogleDrivePath
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun initialPath_isLoadedFromRepository() = runTest {
        val repository = FakeSettingsRepository(initialPath = "ORION/Incoming")
        val viewModel = SettingsViewModel(repository)

        advanceUntilIdle()

        assertEquals("ORION/Incoming", viewModel.uiState.value.googleDrivePath)
        assertFalse(viewModel.uiState.value.isLoading)
        assertFalse(viewModel.uiState.value.isDirty)
    }

    @Test
    fun save_normalizesPathAndReportsSuccess() = runTest {
        val repository = FakeSettingsRepository()
        val viewModel = SettingsViewModel(repository)
        advanceUntilIdle()

        viewModel.onGoogleDrivePathChanged(" /ORION/Incoming/ ")
        viewModel.save()
        advanceUntilIdle()

        assertEquals(listOf(" /ORION/Incoming/ "), repository.savedRawPaths)
        assertEquals("ORION/Incoming", viewModel.uiState.value.googleDrivePath)
        assertEquals(SettingsFeedback.Saved, viewModel.uiState.value.feedback)
        assertFalse(viewModel.uiState.value.isDirty)
    }

    @Test
    fun save_emptyPathClearsExistingSetting() = runTest {
        val repository = FakeSettingsRepository(initialPath = "ORION/Incoming")
        val viewModel = SettingsViewModel(repository)
        advanceUntilIdle()

        viewModel.onGoogleDrivePathChanged("")
        viewModel.save()
        advanceUntilIdle()

        assertEquals(SettingsFeedback.Cleared, viewModel.uiState.value.feedback)
        assertEquals(null, viewModel.uiState.value.savedGoogleDrivePath)
        assertFalse(viewModel.uiState.value.isDirty)
    }

    @Test
    fun saveFailure_keepsDraftAndAllowsRetry() = runTest {
        val repository = FakeSettingsRepository(failOnSave = true)
        val viewModel = SettingsViewModel(repository)
        advanceUntilIdle()

        viewModel.onGoogleDrivePathChanged("ORION/Incoming")
        viewModel.save()
        advanceUntilIdle()

        assertEquals("ORION/Incoming", viewModel.uiState.value.googleDrivePath)
        assertEquals(SettingsFeedback.SaveFailed, viewModel.uiState.value.feedback)
        assertTrue(viewModel.uiState.value.canSave)
    }

    @Test
    fun repeatedSaveWhileSaving_isIgnored() = runTest {
        val repository = FakeSettingsRepository()
        val viewModel = SettingsViewModel(repository)
        advanceUntilIdle()
        viewModel.onGoogleDrivePathChanged("ORION/Incoming")

        viewModel.save()
        viewModel.save()
        advanceUntilIdle()

        assertEquals(1, repository.savedRawPaths.size)
    }
}

private class FakeSettingsRepository(
    initialPath: String? = null,
    private val failOnSave: Boolean = false,
) : SettingsRepository {
    private val path = MutableStateFlow(initialPath)
    val savedRawPaths = mutableListOf<String>()

    override fun observeGoogleDrivePath(): Flow<String?> = path

    override suspend fun setGoogleDrivePath(rawPath: String) {
        savedRawPaths += rawPath
        check(!failOnSave) { "save failed" }
        path.value = normalizeGoogleDrivePath(rawPath).ifEmpty { null }
    }
}
