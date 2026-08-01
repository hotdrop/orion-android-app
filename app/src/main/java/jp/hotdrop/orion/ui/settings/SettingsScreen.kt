package jp.hotdrop.orion.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.hotdrop.orion.R
import jp.hotdrop.orion.model.GoogleDriveTarget
import jp.hotdrop.orion.ui.settings.uistate.SettingsFeedback
import jp.hotdrop.orion.ui.settings.uistate.SettingsOperation
import jp.hotdrop.orion.ui.settings.uistate.SettingsUiState
import jp.hotdrop.orion.ui.theme.OrionAmber
import jp.hotdrop.orion.ui.theme.OrionCyan
import jp.hotdrop.orion.ui.theme.OrionCyanMuted
import jp.hotdrop.orion.ui.theme.OrionDeepNavy
import jp.hotdrop.orion.ui.theme.OrionError
import jp.hotdrop.orion.ui.theme.OrionPanel
import jp.hotdrop.orion.ui.theme.OrionPanelElevated
import jp.hotdrop.orion.ui.theme.OrionTextMuted
import jp.hotdrop.orion.ui.theme.OrionTheme

internal const val SelectDriveFolderButtonTag = "select_drive_folder_button"
internal const val ClearDriveFolderButtonTag = "clear_drive_folder_button"

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onSelectFolder: () -> Unit,
    onClearFolder: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OrionDeepNavy)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SettingsModuleHeader()
        DriveTargetPanel(
            uiState = uiState,
            onSelectFolder = onSelectFolder,
            onClearFolder = onClearFolder,
        )
        SettingsStatusPanel(uiState)
    }
}

@Composable
private fun SettingsModuleHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = stringResource(R.string.settings_module_code),
                color = OrionCyan,
                fontSize = 10.sp,
                letterSpacing = 1.4.sp,
            )
            Text(
                text = stringResource(R.string.settings_module_title),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
            )
        }
        Text(
            text = stringResource(R.string.settings_module_state),
            color = OrionCyan,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun DriveTargetPanel(
    uiState: SettingsUiState,
    onSelectFolder: () -> Unit,
    onClearFolder: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, OrionCyanMuted, PanelShape)
            .background(OrionPanel.copy(alpha = 0.82f), PanelShape)
            .padding(18.dp),
    ) {
        Text(
            text = stringResource(R.string.settings_drive_panel_title),
            color = OrionCyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.settings_drive_panel_description),
            color = OrionTextMuted,
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.height(18.dp))
        SelectedDriveTarget(uiState)
        Spacer(Modifier.height(16.dp))
        SelectDriveFolderButton(
            isSelecting = uiState.isSelectingFolder,
            enabled = uiState.canSelectFolder,
            onClick = onSelectFolder,
        )
        if (uiState.driveTarget != null) {
            Spacer(Modifier.height(10.dp))
            ClearDriveTargetButton(
                enabled = uiState.canClear,
                onClick = onClearFolder,
            )
        }
    }
}

@Composable
private fun SelectedDriveTarget(uiState: SettingsUiState) {
    val targetText = when {
        uiState.isLoading -> stringResource(R.string.settings_drive_target_loading)
        uiState.driveTarget == null -> stringResource(R.string.settings_drive_target_unset)
        else -> uiState.driveTarget.displayPath
    }
    val targetColor = if (uiState.driveTarget == null && !uiState.isLoading) {
        OrionAmber
    } else {
        MaterialTheme.colorScheme.onBackground
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, OrionCyanMuted.copy(alpha = 0.7f))
            .background(OrionPanelElevated.copy(alpha = 0.35f))
            .padding(14.dp),
    ) {
        Text(
            text = stringResource(R.string.settings_drive_target_label),
            color = OrionTextMuted,
            fontSize = 9.sp,
            letterSpacing = 1.sp,
        )
        Spacer(Modifier.height(5.dp))
        Text(
            text = targetText,
            color = targetColor,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun SelectDriveFolderButton(
    isSelecting: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val actionDescription = stringResource(R.string.settings_select_folder_content_description)
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .testTag(SelectDriveFolderButtonTag)
            .semantics { contentDescription = actionDescription },
        enabled = enabled,
        shape = ActionShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = OrionCyan,
            contentColor = OrionDeepNavy,
            disabledContainerColor = OrionCyanMuted.copy(alpha = 0.35f),
            disabledContentColor = OrionTextMuted,
        ),
    ) {
        if (isSelecting) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = OrionDeepNavy,
                strokeWidth = 2.dp,
            )
        } else {
            Text(
                text = stringResource(R.string.settings_select_folder_action),
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun ClearDriveTargetButton(
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val actionDescription = stringResource(R.string.settings_clear_folder_content_description)
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .testTag(ClearDriveFolderButtonTag)
            .semantics { contentDescription = actionDescription },
        enabled = enabled,
        shape = ActionShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = OrionAmber,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = OrionTextMuted,
        ),
    ) {
        Text(
            text = stringResource(R.string.settings_clear_folder_action),
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun SettingsStatusPanel(uiState: SettingsUiState) {
    val status = uiState.toStatusPresentation()
    val statusColor = status.tone.toColor()
    val statusCode = stringResource(status.codeRes)
    val statusMessage = stringResource(status.messageRes)
    val statusDescription = stringResource(
        R.string.settings_status_content_description,
        statusMessage,
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, statusColor.copy(alpha = 0.75f))
            .background(OrionPanelElevated.copy(alpha = 0.48f))
            .padding(16.dp)
            .semantics { contentDescription = statusDescription },
    ) {
        Text(
            text = statusCode,
            color = statusColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = statusMessage,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

private fun SettingsStatusTone.toColor(): Color = when (this) {
    SettingsStatusTone.Normal -> OrionCyan
    SettingsStatusTone.Warning -> OrionAmber
    SettingsStatusTone.Error -> OrionError
}

private val PanelShape = CutCornerShape(topStart = 18.dp, bottomEnd = 18.dp)
private val ActionShape = CutCornerShape(topStart = 10.dp, bottomEnd = 10.dp)

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
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
