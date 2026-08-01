package jp.hotdrop.orion.ui.settings.uistate

import androidx.compose.runtime.Immutable
import jp.hotdrop.orion.model.GoogleDriveTarget

@Immutable
data class SettingsUiState(
    val driveTarget: GoogleDriveTarget? = null,
    val operation: SettingsOperation = SettingsOperation.Loading,
    val feedback: SettingsFeedback = SettingsFeedback.None,
) {
    val isLoading: Boolean
        get() = operation == SettingsOperation.Loading

    val isSelectingFolder: Boolean
        get() = operation == SettingsOperation.SelectingFolder

    val canSelectFolder: Boolean
        get() = operation == SettingsOperation.Idle

    val canClear: Boolean
        get() = driveTarget != null && operation == SettingsOperation.Idle
}

enum class SettingsOperation {
    Loading,
    Idle,
    SelectingFolder,
    Clearing,
}

enum class SettingsFeedback {
    None,
    FolderSaved,
    Cleared,
    SelectionFailed,
    ClearFailed,
    LoadFailed,
}
