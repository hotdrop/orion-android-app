package jp.hotdrop.orion.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewModelScope
import java.util.logging.Level
import java.util.logging.Logger
import jp.hotdrop.orion.data.settings.SettingsRepository
import jp.hotdrop.orion.data.settings.normalizeGoogleDrivePath
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val googleDrivePath: String = "",
    val savedGoogleDrivePath: String? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val feedback: SettingsFeedback = SettingsFeedback.None,
) {
    val isDirty: Boolean
        get() = googleDrivePath != savedGoogleDrivePath.orEmpty()

    val canSave: Boolean
        get() = !isLoading && !isSaving && isDirty
}

enum class SettingsFeedback {
    None,
    Saved,
    Cleared,
    SaveFailed,
    LoadFailed,
}

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observeGoogleDrivePath()
    }

    fun onGoogleDrivePathChanged(path: String) {
        _uiState.update { state ->
            state.copy(
                googleDrivePath = path,
                feedback = SettingsFeedback.None,
            )
        }
    }

    fun save() {
        val state = _uiState.value
        if (!state.canSave) return

        val pathToSave = state.googleDrivePath
        _uiState.update { currentState ->
            currentState.copy(
                isSaving = true,
                feedback = SettingsFeedback.None,
            )
        }

        viewModelScope.launch {
            try {
                settingsRepository.setGoogleDrivePath(pathToSave)
                val normalizedPath = normalizeGoogleDrivePath(pathToSave)
                _uiState.update { currentState ->
                    currentState.copy(
                        googleDrivePath = normalizedPath,
                        savedGoogleDrivePath = normalizedPath.ifEmpty { null },
                        isSaving = false,
                        feedback = if (normalizedPath.isEmpty()) {
                            SettingsFeedback.Cleared
                        } else {
                            SettingsFeedback.Saved
                        },
                    )
                }
            } catch (error: Exception) {
                Logger.getLogger(SettingsViewModel::class.java.name).log(
                    Level.SEVERE,
                    "Failed to save the Google Drive path",
                    error,
                )
                _uiState.update { currentState ->
                    currentState.copy(
                        isSaving = false,
                        feedback = SettingsFeedback.SaveFailed,
                    )
                }
            }
        }
    }

    private fun observeGoogleDrivePath() {
        viewModelScope.launch {
            settingsRepository.observeGoogleDrivePath()
                .catch { error ->
                    Logger.getLogger(SettingsViewModel::class.java.name).log(
                        Level.SEVERE,
                        "Failed to load the Google Drive path",
                        error,
                    )
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            feedback = SettingsFeedback.LoadFailed,
                        )
                    }
                }
                .collect { savedPath ->
                    _uiState.update { state ->
                        val shouldRefreshDraft = state.isLoading || !state.isDirty
                        state.copy(
                            googleDrivePath = if (shouldRefreshDraft) {
                                savedPath.orEmpty()
                            } else {
                                state.googleDrivePath
                            },
                            savedGoogleDrivePath = savedPath,
                            isLoading = false,
                        )
                    }
                }
        }
    }

    companion object {
        fun factory(settingsRepository: SettingsRepository): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    SettingsViewModel(settingsRepository)
                }
            }
    }
}
