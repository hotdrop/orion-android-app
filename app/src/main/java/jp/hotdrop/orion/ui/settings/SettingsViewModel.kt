package jp.hotdrop.orion.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.logging.Level
import java.util.logging.Logger
import javax.inject.Inject
import jp.hotdrop.orion.data.remote.GoogleDriveRemoteDataSource
import jp.hotdrop.orion.data.SettingsRepository
import jp.hotdrop.orion.model.GoogleDriveTarget
import jp.hotdrop.orion.ui.settings.uistate.DriveFolderBrowserUiState
import jp.hotdrop.orion.ui.settings.uistate.DriveFolderItem
import jp.hotdrop.orion.ui.settings.uistate.SettingsFeedback
import jp.hotdrop.orion.ui.settings.uistate.SettingsOperation
import jp.hotdrop.orion.ui.settings.uistate.SettingsUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val driveRemoteDataSource: GoogleDriveRemoteDataSource,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    private var folderSelectionAccessToken: String? = null
    private val folderPath = mutableListOf<DriveFolderLocation>()

    init {
        observeDriveTarget()
    }

    fun beginFolderSelection(): Boolean {
        if (!_uiState.value.canSelectFolder) return false
        _uiState.update {
            it.copy(
                operation = SettingsOperation.AuthorizingDrive,
                feedback = SettingsFeedback.None,
            )
        }
        return true
    }

    fun cancelFolderSelection() {
        folderSelectionAccessToken = null
        folderPath.clear()
        _uiState.update { state ->
            if (!state.isSelectingFolder) {
                state
            } else {
                state.copy(
                    operation = SettingsOperation.Idle,
                    feedback = SettingsFeedback.None,
                    folderBrowser = null,
                )
            }
        }
    }

    fun openFolderBrowser(accessToken: String) {
        if (_uiState.value.operation != SettingsOperation.AuthorizingDrive) return
        folderSelectionAccessToken = accessToken
        folderPath.clear()
        folderPath += DriveFolderLocation(
            id = DRIVE_ROOT_FOLDER_ID,
            name = DRIVE_ROOT_NAME,
        )
        _uiState.update {
            it.copy(
                operation = SettingsOperation.BrowsingFolders,
                folderBrowser = currentBrowserState(isLoading = true),
            )
        }
        loadCurrentFolder()
    }

    fun openFolder(folder: DriveFolderItem) {
        val browser = _uiState.value.folderBrowser ?: return
        if (_uiState.value.operation != SettingsOperation.BrowsingFolders || browser.isLoading) return
        folderPath += DriveFolderLocation(folder.id, folder.name)
        _uiState.update { it.copy(folderBrowser = currentBrowserState(isLoading = true)) }
        loadCurrentFolder()
    }

    fun navigateToParentFolder() {
        val browser = _uiState.value.folderBrowser ?: return
        if (
            _uiState.value.operation != SettingsOperation.BrowsingFolders ||
            browser.isLoading ||
            folderPath.size <= 1
        ) {
            return
        }
        folderPath.removeAt(folderPath.lastIndex)
        _uiState.update { it.copy(folderBrowser = currentBrowserState(isLoading = true)) }
        loadCurrentFolder()
    }

    fun saveCurrentFolder() {
        val browser = _uiState.value.folderBrowser ?: return
        if (_uiState.value.operation != SettingsOperation.BrowsingFolders || browser.isLoading) return
        val selectedFolder = folderPath.lastOrNull() ?: return
        _uiState.update { it.copy(operation = SettingsOperation.SavingFolder) }
        viewModelScope.launch {
            try {
                val target = GoogleDriveTarget(
                    folderId = selectedFolder.id,
                    displayPath = folderPath.joinToString("/") { it.name },
                )
                settingsRepository.setDriveTarget(target)
                folderSelectionAccessToken = null
                folderPath.clear()
                _uiState.update { state ->
                    if (state.operation != SettingsOperation.SavingFolder) {
                        state
                    } else {
                        state.copy(
                            driveTarget = target,
                            operation = SettingsOperation.Idle,
                            feedback = SettingsFeedback.FolderSaved,
                            folderBrowser = null,
                        )
                    }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                reportFolderSelectionFailure(error)
            }
        }
    }

    fun reportFolderSelectionFailure(error: Throwable? = null) {
        if (!_uiState.value.isSelectingFolder) return
        error?.let { logFailure("Failed to select a Google Drive folder", it) }
        folderSelectionAccessToken = null
        folderPath.clear()
        _uiState.update { state ->
            if (!state.isSelectingFolder) {
                state
            } else {
                state.copy(
                    operation = SettingsOperation.Idle,
                    feedback = SettingsFeedback.SelectionFailed,
                    folderBrowser = null,
                )
            }
        }
    }

    private fun loadCurrentFolder() {
        val accessToken = folderSelectionAccessToken
        val currentFolder = folderPath.lastOrNull()
        if (accessToken == null || currentFolder == null) {
            reportFolderSelectionFailure()
            return
        }
        viewModelScope.launch {
            try {
                val folders = driveRemoteDataSource.listFolders(accessToken, currentFolder.id)
                    .map { DriveFolderItem(id = it.id, name = it.name) }
                _uiState.update { state ->
                    if (
                        state.operation != SettingsOperation.BrowsingFolders ||
                        folderPath.lastOrNull()?.id != currentFolder.id
                    ) {
                        state
                    } else {
                        state.copy(
                            folderBrowser = currentBrowserState(
                                folders = folders,
                                isLoading = false,
                            ),
                        )
                    }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                reportFolderSelectionFailure(error)
            }
        }
    }

    private fun currentBrowserState(
        folders: List<DriveFolderItem> = emptyList(),
        isLoading: Boolean,
    ): DriveFolderBrowserUiState {
        val currentFolder = checkNotNull(folderPath.lastOrNull())
        return DriveFolderBrowserUiState(
            currentFolderId = currentFolder.id,
            currentPath = folderPath.joinToString("/") { it.name },
            folders = folders,
            isLoading = isLoading,
            canNavigateUp = folderPath.size > 1,
        )
    }

    fun clearDriveTarget() {
        if (!_uiState.value.canClear) return
        _uiState.update {
            it.copy(
                operation = SettingsOperation.Clearing,
                feedback = SettingsFeedback.None,
            )
        }
        viewModelScope.launch {
            try {
                settingsRepository.clearDriveTarget()
                _uiState.update { state ->
                    if (state.operation != SettingsOperation.Clearing) {
                        state
                    } else {
                        state.copy(
                            driveTarget = null,
                            operation = SettingsOperation.Idle,
                            feedback = SettingsFeedback.Cleared,
                        )
                    }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                logFailure("Failed to clear the Google Drive folder", error)
                _uiState.update { state ->
                    if (state.operation != SettingsOperation.Clearing) {
                        state
                    } else {
                        state.copy(
                            operation = SettingsOperation.Idle,
                            feedback = SettingsFeedback.ClearFailed,
                        )
                    }
                }
            }
        }
    }

    private fun observeDriveTarget() {
        viewModelScope.launch {
            settingsRepository.observeDriveTarget()
                .catch { error ->
                    logFailure("Failed to load the Google Drive target", error)
                    _uiState.update {
                        it.copy(
                            operation = SettingsOperation.Idle,
                            feedback = SettingsFeedback.LoadFailed,
                        )
                    }
                }
                .collect { target ->
                    _uiState.update { state ->
                        state.copy(
                            driveTarget = target,
                            operation = if (state.operation == SettingsOperation.Loading) {
                                SettingsOperation.Idle
                            } else {
                                state.operation
                            },
                        )
                    }
                }
        }
    }

    private fun logFailure(message: String, error: Throwable) {
        Logger.getLogger(SettingsViewModel::class.java.name).log(Level.SEVERE, message, error)
    }

    private companion object {
        const val DRIVE_ROOT_FOLDER_ID = "root"
        const val DRIVE_ROOT_NAME = "My Drive"
    }
}

private data class DriveFolderLocation(
    val id: String,
    val name: String,
)
