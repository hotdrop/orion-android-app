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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import jp.hotdrop.orion.data.settings.SettingsRepository
import jp.hotdrop.orion.ui.theme.OrionAmber
import jp.hotdrop.orion.ui.theme.OrionCyan
import jp.hotdrop.orion.ui.theme.OrionCyanMuted
import jp.hotdrop.orion.ui.theme.OrionDeepNavy
import jp.hotdrop.orion.ui.theme.OrionError
import jp.hotdrop.orion.ui.theme.OrionPanel
import jp.hotdrop.orion.ui.theme.OrionPanelElevated
import jp.hotdrop.orion.ui.theme.OrionTextMuted
import jp.hotdrop.orion.ui.theme.OrionTheme

internal const val GoogleDrivePathInputTag = "google_drive_path_input"
internal const val SaveSettingsButtonTag = "save_settings_button"

@Composable
fun SettingsRoute(
    settingsRepository: SettingsRepository,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.factory(settingsRepository),
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsScreen(
        uiState = uiState,
        onGoogleDrivePathChanged = viewModel::onGoogleDrivePathChanged,
        onSave = viewModel::save,
        modifier = modifier,
    )
}

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onGoogleDrivePathChanged: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OrionDeepNavy)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SettingsModuleHeader()
        DriveTargetPanel(
            uiState = uiState,
            onGoogleDrivePathChanged = onGoogleDrivePathChanged,
            onSave = onSave,
        )
        SettingsStatusPanel(uiState = uiState)
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
                text = "MODULE // CFG",
                color = OrionCyan,
                fontSize = 10.sp,
                letterSpacing = 1.4.sp,
            )
            Text(
                text = "LOCAL CONFIGURATION NODE",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
            )
        }
        Text(
            text = "ONLINE",
            color = OrionCyan,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.4.sp,
        )
    }
}

@Composable
private fun DriveTargetPanel(
    uiState: SettingsUiState,
    onGoogleDrivePathChanged: (String) -> Unit,
    onSave: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = OrionCyanMuted,
                shape = CutCornerShape(topStart = 18.dp, bottomEnd = 18.dp),
            )
            .background(
                color = OrionPanel.copy(alpha = 0.82f),
                shape = CutCornerShape(topStart = 18.dp, bottomEnd = 18.dp),
            )
            .padding(18.dp),
    ) {
        Text(
            text = "GOOGLE DRIVE // TARGET DIRECTORY",
            color = OrionCyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Incoming Intelligenceを取得する基準フォルダを指定します。",
            color = OrionTextMuted,
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(18.dp))
        OutlinedTextField(
            value = uiState.googleDrivePath,
            onValueChange = onGoogleDrivePathChanged,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(GoogleDrivePathInputTag)
                .semantics { contentDescription = "Google Driveフォルダパス" },
            enabled = !uiState.isLoading && !uiState.isSaving,
            label = { Text("DRIVE PATH") },
            placeholder = { Text("ORION/Incoming") },
            supportingText = {
                Text("例: ORION/Incoming  //  空欄を保存すると設定を解除")
            },
            singleLine = true,
            shape = CutCornerShape(topStart = 10.dp, bottomEnd = 10.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (uiState.canSave) onSave()
                },
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = OrionCyan,
                unfocusedBorderColor = OrionCyanMuted,
                focusedLabelColor = OrionCyan,
                unfocusedLabelColor = OrionTextMuted,
                cursorColor = OrionCyan,
                focusedContainerColor = OrionPanelElevated.copy(alpha = 0.45f),
                unfocusedContainerColor = OrionPanelElevated.copy(alpha = 0.25f),
                disabledContainerColor = OrionPanelElevated.copy(alpha = 0.2f),
            ),
        )
        Spacer(modifier = Modifier.height(18.dp))
        Button(
            onClick = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .testTag(SaveSettingsButtonTag)
                .semantics { contentDescription = "Google Driveパスを保存" },
            enabled = uiState.canSave,
            shape = CutCornerShape(topStart = 10.dp, bottomEnd = 10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = OrionCyan,
                contentColor = OrionDeepNavy,
                disabledContainerColor = OrionCyanMuted.copy(alpha = 0.35f),
                disabledContentColor = OrionTextMuted,
            ),
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.height(18.dp),
                    color = OrionDeepNavy,
                    strokeWidth = 2.dp,
                )
            } else {
                Text(
                    text = "[ SAVE CONFIG ]",
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                )
            }
        }
    }
}

@Composable
private fun SettingsStatusPanel(uiState: SettingsUiState) {
    val status = settingsStatus(uiState)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, status.color.copy(alpha = 0.75f))
            .background(OrionPanelElevated.copy(alpha = 0.48f))
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .semantics { contentDescription = "設定状態: ${status.message}" },
    ) {
        Text(
            text = status.code,
            color = status.color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = status.message,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

private data class SettingsStatus(
    val code: String,
    val message: String,
    val color: Color,
)

private fun settingsStatus(uiState: SettingsUiState): SettingsStatus = when {
    uiState.isLoading -> SettingsStatus(
        code = "CONFIG DATA // LOADING",
        message = "保存済み設定を読み込んでいます。",
        color = OrionCyan,
    )
    uiState.isSaving -> SettingsStatus(
        code = "LOCAL WRITE // PROCESSING",
        message = "Google Driveパスを端末へ保存しています。",
        color = OrionCyan,
    )
    uiState.feedback == SettingsFeedback.Saved -> SettingsStatus(
        code = "LOCAL WRITE // COMPLETE",
        message = "Google Driveパスを保存しました。",
        color = OrionCyan,
    )
    uiState.feedback == SettingsFeedback.Cleared -> SettingsStatus(
        code = "DRIVE TARGET // CLEARED",
        message = "Google Driveパスの設定を解除しました。",
        color = OrionAmber,
    )
    uiState.feedback == SettingsFeedback.SaveFailed -> SettingsStatus(
        code = "LOCAL WRITE // ERROR",
        message = "保存できませんでした。入力内容を維持したまま再試行できます。",
        color = OrionError,
    )
    uiState.feedback == SettingsFeedback.LoadFailed -> SettingsStatus(
        code = "CONFIG DATA // ERROR",
        message = "保存済み設定を読み込めませんでした。パスを入力して再保存できます。",
        color = OrionError,
    )
    uiState.savedGoogleDrivePath == null -> SettingsStatus(
        code = "DRIVE TARGET // NOT CONFIGURED",
        message = "Google Driveフォルダは未設定です。",
        color = OrionAmber,
    )
    else -> SettingsStatus(
        code = "DRIVE TARGET // LOCAL CACHE READY",
        message = "保存済みのGoogle Driveフォルダを使用します。",
        color = OrionCyan,
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF030812, widthDp = 393, heightDp = 620)
@Composable
private fun SettingsScreenUnsetPreview() {
    OrionTheme {
        SettingsScreen(
            uiState = SettingsUiState(isLoading = false),
            onGoogleDrivePathChanged = {},
            onSave = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF030812, widthDp = 393, heightDp = 620)
@Composable
private fun SettingsScreenSavedPreview() {
    OrionTheme {
        SettingsScreen(
            uiState = SettingsUiState(
                googleDrivePath = "ORION/Incoming",
                savedGoogleDrivePath = "ORION/Incoming",
                isLoading = false,
                feedback = SettingsFeedback.Saved,
            ),
            onGoogleDrivePathChanged = {},
            onSave = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF030812, widthDp = 393, heightDp = 620)
@Composable
private fun SettingsScreenSavingPreview() {
    OrionTheme {
        SettingsScreen(
            uiState = SettingsUiState(
                googleDrivePath = "ORION/Incoming",
                isLoading = false,
                isSaving = true,
            ),
            onGoogleDrivePathChanged = {},
            onSave = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF030812, widthDp = 393, heightDp = 620)
@Composable
private fun SettingsScreenErrorPreview() {
    OrionTheme {
        SettingsScreen(
            uiState = SettingsUiState(
                googleDrivePath = "ORION/Incoming",
                savedGoogleDrivePath = "ORION/Old",
                isLoading = false,
                feedback = SettingsFeedback.SaveFailed,
            ),
            onGoogleDrivePathChanged = {},
            onSave = {},
        )
    }
}
