package jp.hotdrop.orion.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewModelScope
import java.util.logging.Level
import java.util.logging.Logger
import jp.hotdrop.orion.data.incoming.GoogleDriveFolderMimeType
import jp.hotdrop.orion.data.incoming.GoogleDriveRemoteDataSource
import jp.hotdrop.orion.data.settings.GoogleDriveTarget
import jp.hotdrop.orion.data.settings.SettingsRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val driveTarget: GoogleDriveTarget? = null,
    val isLoading: Boolean = true,
    val isSelectingFolder: Boolean = false,
    val isClearing: Boolean = false,
    val feedback: SettingsFeedback = SettingsFeedback.None,
) {
    val canSelectFolder: Boolean
        get() = !isLoading && !isSelectingFolder && !isClearing

    val canClear: Boolean
        get() = driveTarget != null && canSelectFolder
}

enum class SettingsFeedback {
    None,
    FolderSaved,
    Cleared,
    SelectionFailed,
    LoadFailed,
}

class SettingsViewModel(
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
            it.copy(isSelectingFolder = true, feedback = SettingsFeedback.None)
        }
        return true
    }

    fun saveSelectedFolder(accessToken: String, folderId: String) {
        if (!_uiState.value.isSelectingFolder) return
        viewModelScope.launch {
            try {
                val folder = driveRemoteDataSource.getFolder(accessToken, folderId)
                check(folder.mimeType == GoogleDriveFolderMimeType) {
                    "The selected Drive item is not a folder"
                }
                val target = GoogleDriveTarget(folderId = folder.id, displayPath = folder.name)
                settingsRepository.setDriveTarget(target)
                _uiState.update {
                    it.copy(
                        driveTarget = target,
                        isSelectingFolder = false,
                        feedback = SettingsFeedback.FolderSaved,
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                logFailure("Failed to save the selected Google Drive folder", error)
                reportFolderSelectionFailure()
            }
        }
    }

    fun reportFolderSelectionFailure() {
        _uiState.update {
            it.copy(
                isSelectingFolder = false,
                feedback = SettingsFeedback.SelectionFailed,
            )
        }
    }

    fun clearDriveTarget() {
        if (!_uiState.value.canClear) return
        _uiState.update { it.copy(isClearing = true, feedback = SettingsFeedback.None) }
        viewModelScope.launch {
            try {
                settingsRepository.clearDriveTarget()
                _uiState.update {
                    it.copy(
                        driveTarget = null,
                        isClearing = false,
                        feedback = SettingsFeedback.Cleared,
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                logFailure("Failed to clear the Google Drive folder", error)
                _uiState.update {
                    it.copy(isClearing = false, feedback = SettingsFeedback.SelectionFailed)
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
                        it.copy(isLoading = false, feedback = SettingsFeedback.LoadFailed)
                    }
                }
                .collect { target ->
                    _uiState.update { it.copy(driveTarget = target, isLoading = false) }
                }
        }
    }

    private fun logFailure(message: String, error: Throwable) {
        Logger.getLogger(SettingsViewModel::class.java.name).log(Level.SEVERE, message, error)
    }

    companion object {
        fun factory(
            settingsRepository: SettingsRepository,
            driveRemoteDataSource: GoogleDriveRemoteDataSource,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                SettingsViewModel(settingsRepository, driveRemoteDataSource)
            }
        }
    }
}
