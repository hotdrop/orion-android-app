package jp.hotdrop.orion.ui.settings.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import jp.hotdrop.orion.R
import jp.hotdrop.orion.ui.theme.OrionAmber
import jp.hotdrop.orion.ui.theme.OrionCyan
import jp.hotdrop.orion.ui.theme.OrionCyanMuted
import jp.hotdrop.orion.ui.theme.OrionDeepNavy
import jp.hotdrop.orion.ui.theme.OrionTextMuted
import jp.hotdrop.orion.ui.theme.OrionTheme

internal const val SelectDriveFolderButtonTag = "select_drive_folder_button"
internal const val ClearDriveFolderButtonTag = "clear_drive_folder_button"

@Composable
fun SettingsSelectDriveFolderButton(
    isSelecting: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val actionDescription = stringResource(R.string.settings_select_folder_content_description)
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .testTag(SelectDriveFolderButtonTag)
            .semantics { contentDescription = actionDescription },
        enabled = enabled,
        shape = SettingsActionShape,
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
fun SettingsClearDriveTargetButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val actionDescription = stringResource(R.string.settings_clear_folder_content_description)
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .testTag(ClearDriveFolderButtonTag)
            .semantics { contentDescription = actionDescription },
        enabled = enabled,
        shape = SettingsActionShape,
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

private val SettingsActionShape = CutCornerShape(topStart = 10.dp, bottomEnd = 10.dp)

@Preview(showBackground = true)
@Composable
private fun SettingsSelectDriveFolderButtonPreview() {
    OrionTheme {
        SettingsSelectDriveFolderButton(
            isSelecting = false,
            enabled = true,
            onClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsSelectDriveFolderButtonSelectingPreview() {
    OrionTheme {
        SettingsSelectDriveFolderButton(
            isSelecting = true,
            enabled = false,
            onClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsClearDriveTargetButtonPreview() {
    OrionTheme {
        SettingsClearDriveTargetButton(
            enabled = true,
            onClick = {},
        )
    }
}
