package jp.hotdrop.orion.ui.settings

import jp.hotdrop.orion.data.remote.GoogleDriveFile
import jp.hotdrop.orion.data.remote.GoogleDriveFolderMimeType
import jp.hotdrop.orion.data.remote.GoogleDriveRemoteDataSource
import jp.hotdrop.orion.data.SettingsRepository
import jp.hotdrop.orion.model.GoogleDriveTarget
import jp.hotdrop.orion.ui.settings.uistate.SettingsFeedback
import jp.hotdrop.orion.ui.settings.uistate.SettingsOperation
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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
        assertEquals(SettingsOperation.Idle, viewModel.uiState.value.operation)
        assertEquals(0, remote.getFolderCalls)
    }

    @Test
    fun selectedFolder_fetchesNameAndPersistsTarget() = runTest {
        val repository = FakeSettingsRepository()
        val remote = FakeDriveRemoteDataSource(folderName = "Weekly Reports")
        val viewModel = SettingsViewModel(repository, remote)
        advanceUntilIdle()

        assertTrue(viewModel.beginFolderSelection())
        viewModel.saveSelectedFolder("token", "folder-1")
        advanceUntilIdle()

        assertEquals(GoogleDriveTarget("folder-1", "Weekly Reports"), repository.target.value)
        assertEquals(SettingsOperation.Idle, viewModel.uiState.value.operation)
        assertEquals(SettingsFeedback.FolderSaved, viewModel.uiState.value.feedback)
    }

    @Test
    fun cancelledSelection_returnsToIdleWithoutError() = runTest {
        val viewModel = SettingsViewModel(
            FakeSettingsRepository(),
            FakeDriveRemoteDataSource(),
        )
        advanceUntilIdle()

        viewModel.beginFolderSelection()
        viewModel.cancelFolderSelection()
        viewModel.reportFolderSelectionFailure(IllegalStateException("late callback"))

        assertEquals(SettingsOperation.Idle, viewModel.uiState.value.operation)
        assertEquals(SettingsFeedback.None, viewModel.uiState.value.feedback)
    }

    @Test
    fun selectionFailure_returnsToIdleWithSelectionFeedback() = runTest {
        val viewModel = SettingsViewModel(
            FakeSettingsRepository(),
            FakeDriveRemoteDataSource(getFolderFailure = IllegalStateException("offline")),
        )
        advanceUntilIdle()

        viewModel.beginFolderSelection()
        viewModel.saveSelectedFolder("token", "folder-1")
        advanceUntilIdle()

        assertEquals(SettingsOperation.Idle, viewModel.uiState.value.operation)
        assertEquals(SettingsFeedback.SelectionFailed, viewModel.uiState.value.feedback)
    }

    @Test
    fun nonFolderSelection_isRejected() = runTest {
        val repository = FakeSettingsRepository()
        val viewModel = SettingsViewModel(
            repository,
            FakeDriveRemoteDataSource(mimeType = "application/vnd.google-apps.document"),
        )
        advanceUntilIdle()

        viewModel.beginFolderSelection()
        viewModel.saveSelectedFolder("token", "document-1")
        advanceUntilIdle()

        assertNull(repository.target.value)
        assertEquals(SettingsFeedback.SelectionFailed, viewModel.uiState.value.feedback)
    }

    @Test
    fun operationInProgress_rejectsDuplicateActions() = runTest {
        val target = GoogleDriveTarget("folder-1", "Incoming")
        val repository = FakeSettingsRepository(target)
        val viewModel = SettingsViewModel(repository, FakeDriveRemoteDataSource())
        advanceUntilIdle()

        assertTrue(viewModel.beginFolderSelection())
        assertFalse(viewModel.beginFolderSelection())
        viewModel.clearDriveTarget()

        assertEquals(SettingsOperation.SelectingFolder, viewModel.uiState.value.operation)
        assertEquals(target, repository.target.value)
    }

    @Test
    fun clear_removesSelectedTarget() = runTest {
        val repository = FakeSettingsRepository(GoogleDriveTarget("folder-1", "Incoming"))
        val viewModel = SettingsViewModel(repository, FakeDriveRemoteDataSource())
        advanceUntilIdle()

        viewModel.clearDriveTarget()
        advanceUntilIdle()

        assertNull(repository.target.value)
        assertEquals(SettingsOperation.Idle, viewModel.uiState.value.operation)
        assertEquals(SettingsFeedback.Cleared, viewModel.uiState.value.feedback)
    }

    @Test
    fun clearFailure_keepsTargetAndReportsDedicatedFeedback() = runTest {
        val target = GoogleDriveTarget("folder-1", "Incoming")
        val repository = FakeSettingsRepository(
            initialTarget = target,
            clearFailure = IllegalStateException("database unavailable"),
        )
        val viewModel = SettingsViewModel(repository, FakeDriveRemoteDataSource())
        advanceUntilIdle()

        viewModel.clearDriveTarget()
        advanceUntilIdle()

        assertEquals(target, repository.target.value)
        assertEquals(SettingsOperation.Idle, viewModel.uiState.value.operation)
        assertEquals(SettingsFeedback.ClearFailed, viewModel.uiState.value.feedback)
    }

    @Test
    fun loadFailure_finishesLoadingAndReportsFailure() = runTest {
        val viewModel = SettingsViewModel(
            FakeSettingsRepository(loadFailure = IllegalStateException("database unavailable")),
            FakeDriveRemoteDataSource(),
        )

        advanceUntilIdle()

        assertEquals(SettingsOperation.Idle, viewModel.uiState.value.operation)
        assertEquals(SettingsFeedback.LoadFailed, viewModel.uiState.value.feedback)
    }
}

private class FakeSettingsRepository(
    initialTarget: GoogleDriveTarget? = null,
    private val loadFailure: Throwable? = null,
    private val clearFailure: Throwable? = null,
) : SettingsRepository {
    val target = MutableStateFlow(initialTarget)

    override fun observeDriveTarget(): Flow<GoogleDriveTarget?> = loadFailure?.let { error ->
        flow { throw error }
    } ?: target

    override suspend fun setDriveTarget(target: GoogleDriveTarget) {
        this.target.value = target
    }

    override suspend fun clearDriveTarget() {
        clearFailure?.let { throw it }
        target.value = null
    }
}

private class FakeDriveRemoteDataSource(
    private val folderName: String = "Incoming",
    private val mimeType: String = GoogleDriveFolderMimeType,
    private val getFolderFailure: Throwable? = null,
) : GoogleDriveRemoteDataSource {
    var getFolderCalls = 0

    override suspend fun getFolder(accessToken: String, folderId: String): GoogleDriveFile {
        getFolderCalls++
        getFolderFailure?.let { throw it }
        return GoogleDriveFile(
            id = folderId,
            name = folderName,
            mimeType = mimeType,
            modifiedAt = 0,
            webViewLink = null,
        )
    }

    override suspend fun listChildren(accessToken: String, folderId: String) =
        emptyList<GoogleDriveFile>()
}
