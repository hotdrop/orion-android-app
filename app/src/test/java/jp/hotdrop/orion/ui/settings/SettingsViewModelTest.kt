package jp.hotdrop.orion.ui.settings

import jp.hotdrop.orion.data.incoming.GoogleDriveFile
import jp.hotdrop.orion.data.incoming.GoogleDriveFolderMimeType
import jp.hotdrop.orion.data.incoming.GoogleDriveRemoteDataSource
import jp.hotdrop.orion.data.settings.GoogleDriveTarget
import jp.hotdrop.orion.data.settings.SettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun initialTarget_isLoadedWithoutStartingNetworkAccess() = runTest {
        val target = GoogleDriveTarget("folder-1", "ORION/Incoming")
        val repository = FakeSettingsRepository(target)
        val remote = FakeDriveRemoteDataSource()
        val viewModel = SettingsViewModel(repository, remote)

        advanceUntilIdle()

        assertEquals(target, viewModel.uiState.value.driveTarget)
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(0, remote.getFolderCalls)
    }

    @Test
    fun selectedFolder_fetchesNameAndPersistsTarget() = runTest {
        val repository = FakeSettingsRepository()
        val remote = FakeDriveRemoteDataSource(folderName = "Weekly Reports")
        val viewModel = SettingsViewModel(repository, remote)
        advanceUntilIdle()

        viewModel.beginFolderSelection()
        viewModel.saveSelectedFolder("token", "folder-1")
        advanceUntilIdle()

        assertEquals(GoogleDriveTarget("folder-1", "Weekly Reports"), repository.target.value)
        assertEquals(SettingsFeedback.FolderSaved, viewModel.uiState.value.feedback)
    }

    @Test
    fun clear_removesSelectedTarget() = runTest {
        val repository = FakeSettingsRepository(GoogleDriveTarget("folder-1", "Incoming"))
        val viewModel = SettingsViewModel(repository, FakeDriveRemoteDataSource())
        advanceUntilIdle()

        viewModel.clearDriveTarget()
        advanceUntilIdle()

        assertEquals(null, repository.target.value)
        assertEquals(SettingsFeedback.Cleared, viewModel.uiState.value.feedback)
    }
}

private class FakeSettingsRepository(initialTarget: GoogleDriveTarget? = null) : SettingsRepository {
    val target = MutableStateFlow(initialTarget)

    override fun observeDriveTarget(): Flow<GoogleDriveTarget?> = target

    override suspend fun setDriveTarget(target: GoogleDriveTarget) {
        this.target.value = target
    }

    override suspend fun clearDriveTarget() {
        target.value = null
    }
}

private class FakeDriveRemoteDataSource(
    private val folderName: String = "Incoming",
) : GoogleDriveRemoteDataSource {
    var getFolderCalls = 0

    override suspend fun getFolder(accessToken: String, folderId: String): GoogleDriveFile {
        getFolderCalls++
        return GoogleDriveFile(
            id = folderId,
            name = folderName,
            mimeType = GoogleDriveFolderMimeType,
            modifiedAt = 0,
            webViewLink = null,
        )
    }

    override suspend fun listChildren(accessToken: String, folderId: String) =
        emptyList<GoogleDriveFile>()
}
