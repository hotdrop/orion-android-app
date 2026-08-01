package jp.hotdrop.orion.ui.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.hotdrop.orion.R
import jp.hotdrop.orion.ui.theme.OrionCyan
import jp.hotdrop.orion.ui.theme.OrionCyanMuted
import jp.hotdrop.orion.ui.theme.OrionPanel
import jp.hotdrop.orion.ui.theme.OrionTextMuted
import jp.hotdrop.orion.ui.theme.OrionTheme

@Composable
fun SettingsDriveTargetPanel(
    displayPath: String?,
    isLoading: Boolean,
    isSelectingFolder: Boolean,
    canSelectFolder: Boolean,
    canClearTarget: Boolean,
    onSelectFolder: () -> Unit,
    onClearTarget: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = OrionCyanMuted,
                shape = CutCornerShape(topStart = 18.dp, bottomEnd = 18.dp)
            )
            .background(
                color = OrionPanel.copy(alpha = 0.82f),
                shape = CutCornerShape(topStart = 18.dp, bottomEnd = 18.dp)
            )
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
        SettingsDriveTarget(
            displayPath = displayPath,
            isLoading = isLoading,
        )
        Spacer(Modifier.height(16.dp))
        SettingsSelectDriveFolderButton(
            isSelecting = isSelectingFolder,
            enabled = canSelectFolder,
            onClick = onSelectFolder,
        )
        if (displayPath != null) {
            Spacer(Modifier.height(10.dp))
            SettingsClearDriveTargetButton(
                enabled = canClearTarget,
                onClick = onClearTarget,
            )
        }
    }
}

@Preview
@Composable
private fun SettingsDriveTargetPanelConfiguredPreview() {
    OrionTheme {
        SettingsDriveTargetPanel(
            displayPath = "ORION/Incoming",
            isLoading = false,
            isSelectingFolder = false,
            canSelectFolder = true,
            canClearTarget = true,
            onSelectFolder = {},
            onClearTarget = {},
        )
    }
}

@Preview
@Composable
private fun SettingsDriveTargetPanelSelectingPreview() {
    OrionTheme {
        SettingsDriveTargetPanel(
            displayPath = null,
            isLoading = false,
            isSelectingFolder = true,
            canSelectFolder = false,
            canClearTarget = false,
            onSelectFolder = {},
            onClearTarget = {},
        )
    }
}
