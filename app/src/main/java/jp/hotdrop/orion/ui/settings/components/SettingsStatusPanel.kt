package jp.hotdrop.orion.ui.settings.components

import androidx.annotation.StringRes
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.hotdrop.orion.R
import jp.hotdrop.orion.ui.settings.SettingsStatusTone
import jp.hotdrop.orion.ui.theme.OrionAmber
import jp.hotdrop.orion.ui.theme.OrionCyan
import jp.hotdrop.orion.ui.theme.OrionError
import jp.hotdrop.orion.ui.theme.OrionPanelElevated
import jp.hotdrop.orion.ui.theme.OrionTheme

@Composable
internal fun SettingsStatusPanel(
    @StringRes codeRes: Int,
    @StringRes messageRes: Int,
    tone: SettingsStatusTone,
    modifier: Modifier = Modifier,
) {
    val statusColor = tone.toColor()
    val statusCode = stringResource(codeRes)
    val statusMessage = stringResource(messageRes)
    val statusDescription = stringResource(
        R.string.settings_status_content_description,
        statusMessage,
    )

    Column(
        modifier = modifier
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

@Preview
@Composable
private fun SettingsStatusPanelReadyPreview() {
    OrionTheme {
        SettingsStatusPanel(
            codeRes = R.string.settings_status_ready_code,
            messageRes = R.string.settings_status_ready_message,
            tone = SettingsStatusTone.Normal,
        )
    }
}

@Preview
@Composable
private fun SettingsStatusPanelErrorPreview() {
    OrionTheme {
        SettingsStatusPanel(
            codeRes = R.string.settings_status_clear_failed_code,
            messageRes = R.string.settings_status_clear_failed_message,
            tone = SettingsStatusTone.Error,
        )
    }
}
