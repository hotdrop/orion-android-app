package jp.hotdrop.orion.ui.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import jp.hotdrop.orion.ui.theme.OrionAmber
import jp.hotdrop.orion.ui.theme.OrionCyanMuted
import jp.hotdrop.orion.ui.theme.OrionPanelElevated
import jp.hotdrop.orion.ui.theme.OrionTextMuted
import jp.hotdrop.orion.ui.theme.OrionTheme

@Composable
fun SettingsDriveTarget(
    displayPath: String?,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    val targetText = when {
        isLoading -> stringResource(R.string.settings_drive_target_loading)
        displayPath == null -> stringResource(R.string.settings_drive_target_unset)
        else -> displayPath
    }
    val targetColor = if (displayPath == null && !isLoading) {
        OrionAmber
    } else {
        MaterialTheme.colorScheme.onBackground
    }

    Column(
        modifier = modifier
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

@Preview(showBackground = true)
@Composable
private fun SettingsDriveTargetConfiguredPreview() {
    OrionTheme {
        SettingsDriveTarget(
            displayPath = "ORION/Incoming",
            isLoading = false,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsDriveTargetUnsetPreview() {
    OrionTheme {
        SettingsDriveTarget(
            displayPath = null,
            isLoading = false,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsDriveTargetLoadingPreview() {
    OrionTheme {
        SettingsDriveTarget(
            displayPath = null,
            isLoading = true,
        )
    }
}
