package jp.hotdrop.orion.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.logging.Level
import java.util.logging.Logger
import javax.inject.Inject
import jp.hotdrop.orion.data.remote.GoogleDriveFolderMimeType
import jp.hotdrop.orion.data.remote.GoogleDriveRemoteDataSource
import jp.hotdrop.orion.data.SettingsRepository
import jp.hotdrop.orion.model.GoogleDriveTarget
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

    init {
        observeDriveTarget()
    }

    fun beginFolderSelection(): Boolean {
        if (!_uiState.value.canSelectFolder) return false
        _uiState.update {
            it.copy(
                operation = SettingsOperation.SelectingFolder,
                feedback = SettingsFeedback.None,
            )
        }
        return true
    }

    fun cancelFolderSelection() {
        _uiState.update { state ->
            if (state.operation != SettingsOperation.SelectingFolder) {
                state
            } else {
                state.copy(
                    operation = SettingsOperation.Idle,
                    feedback = SettingsFeedback.None,
                )
            }
        }
    }

    fun saveSelectedFolder(accessToken: String, folderId: String) {
        if (_uiState.value.operation != SettingsOperation.SelectingFolder) return
        viewModelScope.launch {
            try {
                val folder = driveRemoteDataSource.getFolder(accessToken, folderId)
                check(folder.mimeType == GoogleDriveFolderMimeType) {
                    "The selected Drive item is not a folder"
                }
                val target = GoogleDriveTarget(folderId = folder.id, displayPath = folder.name)
                settingsRepository.setDriveTarget(target)
                _uiState.update { state ->
                    if (state.operation != SettingsOperation.SelectingFolder) {
                        state
                    } else {
                        state.copy(
                            driveTarget = target,
                            operation = SettingsOperation.Idle,
                            feedback = SettingsFeedback.FolderSaved,
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
        if (_uiState.value.operation != SettingsOperation.SelectingFolder) return
        error?.let { logFailure("Failed to select a Google Drive folder", it) }
        _uiState.update { state ->
            if (state.operation != SettingsOperation.SelectingFolder) {
                state
            } else {
                state.copy(
                    operation = SettingsOperation.Idle,
                    feedback = SettingsFeedback.SelectionFailed,
                )
            }
        }
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
}
