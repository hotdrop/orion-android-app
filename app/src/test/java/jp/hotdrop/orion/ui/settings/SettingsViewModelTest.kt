package jp.hotdrop.orion.ui.settings

import jp.hotdrop.orion.data.SettingsRepository
import jp.hotdrop.orion.data.local.dao.SettingsDao
import jp.hotdrop.orion.data.local.entity.SettingsEntity
import jp.hotdrop.orion.data.remote.GoogleDriveFile
import jp.hotdrop.orion.data.remote.GoogleDriveFolderMimeType
import jp.hotdrop.orion.data.remote.GoogleDriveRemoteDataSource
import jp.hotdrop.orion.ui.settings.uistate.SettingsFeedback
import jp.hotdrop.orion.ui.settings.uistate.SettingsOperation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun folderSelection_browsesFoldersAndSavesCurrentPath() = runTest(dispatcher) {
        val settingsDao = FakeSettingsDao()
        val remote = FakeDriveRemoteDataSource(
            folders = mapOf(
                "root" to listOf(folder("tech", "技術資料")),
                "tech" to listOf(folder("android", "Android")),
            ),
        )
        val viewModel = SettingsViewModel(SettingsRepository(settingsDao), remote)
        advanceUntilIdle()

        assertTrue(viewModel.beginFolderSelection())
        viewModel.openFolderBrowser("token")
        advanceUntilIdle()

        assertEquals(SettingsOperation.BrowsingFolders, viewModel.uiState.value.operation)
        assertEquals("My Drive", viewModel.uiState.value.folderBrowser?.currentPath)
        assertEquals(listOf("技術資料"), viewModel.uiState.value.folderBrowser?.folders?.map { it.name })

        viewModel.openFolder(viewModel.uiState.value.folderBrowser!!.folders.single())
        advanceUntilIdle()

        assertEquals("My Drive/技術資料", viewModel.uiState.value.folderBrowser?.currentPath)
        assertTrue(viewModel.uiState.value.folderBrowser?.canNavigateUp == true)

        viewModel.saveCurrentFolder()
        advanceUntilIdle()

        assertEquals("tech", settingsDao.settings.value?.googleDriveFolderId)
        assertEquals("My Drive/技術資料", settingsDao.settings.value?.googleDrivePath)
        assertEquals(SettingsFeedback.FolderSaved, viewModel.uiState.value.feedback)
        assertNull(viewModel.uiState.value.folderBrowser)
    }

    @Test
    fun folderSelection_canNavigateBackToDriveRoot() = runTest(dispatcher) {
        val remote = FakeDriveRemoteDataSource(
            folders = mapOf("root" to listOf(folder("tech", "技術資料"))),
        )
        val viewModel = SettingsViewModel(SettingsRepository(FakeSettingsDao()), remote)
        advanceUntilIdle()

        viewModel.beginFolderSelection()
        viewModel.openFolderBrowser("token")
        advanceUntilIdle()
        viewModel.openFolder(viewModel.uiState.value.folderBrowser!!.folders.single())
        advanceUntilIdle()
        viewModel.navigateToParentFolder()
        advanceUntilIdle()

        assertEquals("root", viewModel.uiState.value.folderBrowser?.currentFolderId)
        assertEquals("My Drive", viewModel.uiState.value.folderBrowser?.currentPath)
        assertFalse(viewModel.uiState.value.folderBrowser?.canNavigateUp ?: true)
    }

    @Test
    fun folderSelection_remoteFailureClosesBrowserAndShowsRetryableError() = runTest(dispatcher) {
        val viewModel = SettingsViewModel(
            SettingsRepository(FakeSettingsDao()),
            FakeDriveRemoteDataSource(fail = true),
        )
        advanceUntilIdle()

        viewModel.beginFolderSelection()
        viewModel.openFolderBrowser("token")
        advanceUntilIdle()

        assertEquals(SettingsOperation.Idle, viewModel.uiState.value.operation)
        assertEquals(SettingsFeedback.SelectionFailed, viewModel.uiState.value.feedback)
        assertNull(viewModel.uiState.value.folderBrowser)
        assertTrue(viewModel.uiState.value.canSelectFolder)
    }

    private fun folder(id: String, name: String) = GoogleDriveFile(
        id = id,
        name = name,
        mimeType = GoogleDriveFolderMimeType,
        modifiedAt = 0,
        webViewLink = null,
    )
}

private class FakeSettingsDao : SettingsDao {
    val settings = MutableStateFlow<SettingsEntity?>(null)

    override fun observeSettings(id: Int): Flow<SettingsEntity?> = settings

    override suspend fun upsert(settings: SettingsEntity) {
        this.settings.value = settings
    }

    override suspend fun delete(id: Int) {
        settings.value = null
    }
}

private class FakeDriveRemoteDataSource(
    private val folders: Map<String, List<GoogleDriveFile>> = emptyMap(),
    private val fail: Boolean = false,
) : GoogleDriveRemoteDataSource {
    override suspend fun listChildren(
        accessToken: String,
        folderId: String,
    ): List<GoogleDriveFile> = error("Not used")

    override suspend fun listFolders(
        accessToken: String,
        parentFolderId: String,
    ): List<GoogleDriveFile> {
        check(!fail) { "Drive request failed" }
        return folders[parentFolderId].orEmpty()
    }
}
