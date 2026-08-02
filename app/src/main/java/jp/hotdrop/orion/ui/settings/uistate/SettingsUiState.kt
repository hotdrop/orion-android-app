package jp.hotdrop.orion.ui.settings.uistate

import androidx.compose.runtime.Immutable
import jp.hotdrop.orion.model.GoogleDriveTarget

@Immutable
data class SettingsUiState(
    val driveTarget: GoogleDriveTarget? = null,
    val operation: SettingsOperation = SettingsOperation.Loading,
    val feedback: SettingsFeedback = SettingsFeedback.None,
    val folderBrowser: DriveFolderBrowserUiState? = null,
) {
    val isLoading: Boolean
        get() = operation == SettingsOperation.Loading

    val isSelectingFolder: Boolean
        get() = operation == SettingsOperation.AuthorizingDrive ||
            operation == SettingsOperation.BrowsingFolders ||
            operation == SettingsOperation.SavingFolder

    val canSelectFolder: Boolean
        get() = operation == SettingsOperation.Idle

    val canClear: Boolean
        get() = driveTarget != null && operation == SettingsOperation.Idle
}

enum class SettingsOperation {
    Loading,
    Idle,
    AuthorizingDrive,
    BrowsingFolders,
    SavingFolder,
    Clearing,
}

@Immutable
data class DriveFolderBrowserUiState(
    val currentFolderId: String,
    val currentPath: String,
    val folders: List<DriveFolderItem> = emptyList(),
    val isLoading: Boolean = true,
    val canNavigateUp: Boolean = false,
)

@Immutable
data class DriveFolderItem(
    val id: String,
    val name: String,
)

enum class SettingsFeedback {
    None,
    FolderSaved,
    Cleared,
    SelectionFailed,
    ClearFailed,
    LoadFailed,
}
