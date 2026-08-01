package jp.hotdrop.orion.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import jp.hotdrop.orion.model.GoogleDriveTarget
import jp.hotdrop.orion.ui.settings.components.SettingsDriveTargetPanel
import jp.hotdrop.orion.ui.settings.components.SettingsHeader
import jp.hotdrop.orion.ui.settings.components.SettingsStatusPanel
import jp.hotdrop.orion.ui.settings.uistate.SettingsFeedback
import jp.hotdrop.orion.ui.settings.uistate.SettingsOperation
import jp.hotdrop.orion.ui.settings.uistate.SettingsUiState
import jp.hotdrop.orion.ui.theme.OrionDeepNavy
import jp.hotdrop.orion.ui.theme.OrionTheme

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onSelectFolder: () -> Unit,
    onClearFolder: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val status = uiState.toStatusPresentation()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OrionDeepNavy)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SettingsHeader()
        SettingsDriveTargetPanel(
            displayPath = uiState.driveTarget?.displayPath,
            isLoading = uiState.isLoading,
            isSelectingFolder = uiState.isSelectingFolder,
            canSelectFolder = uiState.canSelectFolder,
            canClearTarget = uiState.canClear,
            onSelectFolder = onSelectFolder,
            onClearTarget = onClearFolder,
        )
        SettingsStatusPanel(
            codeRes = status.codeRes,
            messageRes = status.messageRes,
            tone = status.tone,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenLoadingPreview() {
    OrionTheme {
        SettingsScreen(
            uiState = SettingsUiState(),
            onSelectFolder = {},
            onClearFolder = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenIdlePreview() {
    OrionTheme {
        SettingsScreen(
            uiState = SettingsUiState(operation = SettingsOperation.Idle),
            onSelectFolder = {},
            onClearFolder = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenSelectingFolderPreview() {
    OrionTheme {
        SettingsScreen(
            uiState = SettingsUiState(operation = SettingsOperation.SelectingFolder),
            onSelectFolder = {},
            onClearFolder = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenFolderSavedPreview() {
    OrionTheme {
        SettingsScreen(
            uiState = SettingsUiState(
                driveTarget = GoogleDriveTarget(
                    folderId = "folder-id",
                    displayPath = "ORION/Incoming",
                ),
                operation = SettingsOperation.Idle,
                feedback = SettingsFeedback.FolderSaved,
            ),
            onSelectFolder = {},
            onClearFolder = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenClearedPreview() {
    OrionTheme {
        SettingsScreen(
            uiState = SettingsUiState(
                operation = SettingsOperation.Idle,
                feedback = SettingsFeedback.Cleared,
            ),
            onSelectFolder = {},
            onClearFolder = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenClearFailedPreview() {
    OrionTheme {
        SettingsScreen(
            uiState = SettingsUiState(
                driveTarget = GoogleDriveTarget(
                    folderId = "folder-id",
                    displayPath = "ORION/Incoming",
                ),
                operation = SettingsOperation.Idle,
                feedback = SettingsFeedback.ClearFailed,
            ),
            onSelectFolder = {},
            onClearFolder = {},
        )
    }
}
