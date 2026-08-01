package jp.hotdrop.orion.ui.settings

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.auth.api.identity.AuthorizationResult
import jp.hotdrop.orion.data.incoming.GoogleDriveAuthorizationClient
import jp.hotdrop.orion.data.incoming.GoogleDriveRemoteDataSource
import jp.hotdrop.orion.data.settings.GoogleDriveTarget
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

internal const val SelectDriveFolderButtonTag = "select_drive_folder_button"
internal const val ClearDriveFolderButtonTag = "clear_drive_folder_button"

@Composable
fun SettingsRoute(
    settingsRepository: SettingsRepository,
    driveRemoteDataSource: GoogleDriveRemoteDataSource,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.factory(settingsRepository, driveRemoteDataSource),
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val authorizationClient = remember(context) { GoogleDriveAuthorizationClient(context) }

    fun acceptAuthorizationResult(result: AuthorizationResult) {
        val token = result.accessToken
        val pickedFolderId = result.tokenResponseParams
            ?.getString(GoogleDriveAuthorizationClient.PickedFileIdsParameter)
            ?.substringBefore(',')
            ?.takeIf(String::isNotBlank)
        if (token == null || pickedFolderId == null) {
            viewModel.reportFolderSelectionFailure()
        } else {
            viewModel.saveSelectedFolder(token, pickedFolderId)
        }
    }

    val authorizationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
    ) { activityResult ->
        if (activityResult.resultCode != Activity.RESULT_OK) {
            viewModel.reportFolderSelectionFailure()
            return@rememberLauncherForActivityResult
        }
        runCatching { authorizationClient.resultFromIntent(activityResult.data) }
            .onSuccess(::acceptAuthorizationResult)
            .onFailure { viewModel.reportFolderSelectionFailure() }
    }

    SettingsScreen(
        uiState = uiState,
        onSelectFolder = {
            if (viewModel.beginFolderSelection()) {
                authorizationClient.selectFolder()
                    .addOnSuccessListener { result ->
                        if (result.hasResolution()) {
                            val pendingIntent = result.pendingIntent
                            if (pendingIntent == null) {
                                viewModel.reportFolderSelectionFailure()
                            } else {
                                authorizationLauncher.launch(
                                    IntentSenderRequest.Builder(pendingIntent.intentSender).build(),
                                )
                            }
                        } else {
                            acceptAuthorizationResult(result)
                        }
                    }
                    .addOnFailureListener { viewModel.reportFolderSelectionFailure() }
            }
        },
        onClearFolder = viewModel::clearDriveTarget,
        modifier = modifier,
    )
}

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
        DriveTargetPanel(uiState, onSelectFolder, onClearFolder)
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
            Text("MODULE // CFG", color = OrionCyan, fontSize = 10.sp, letterSpacing = 1.4.sp)
            Text(
                "LOCAL CONFIGURATION NODE",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
            )
        }
        Text("ONLINE", color = OrionCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
            "GOOGLE DRIVE // TARGET DIRECTORY",
            color = OrionCyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Incoming Intelligenceを手動取得する基準フォルダを選択します。自動同期は行いません。",
            color = OrionTextMuted,
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.height(18.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, OrionCyanMuted.copy(alpha = 0.7f))
                .background(OrionPanelElevated.copy(alpha = 0.35f))
                .padding(14.dp),
        ) {
            Text("SELECTED TARGET", color = OrionTextMuted, fontSize = 9.sp, letterSpacing = 1.sp)
            Spacer(Modifier.height(5.dp))
            Text(
                uiState.driveTarget?.displayPath ?: "NOT CONFIGURED",
                color = if (uiState.driveTarget == null) OrionAmber else MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onSelectFolder,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .testTag(SelectDriveFolderButtonTag)
                .semantics { contentDescription = "Google Driveフォルダを選択" },
            enabled = uiState.canSelectFolder,
            shape = ActionShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = OrionCyan,
                contentColor = OrionDeepNavy,
                disabledContainerColor = OrionCyanMuted.copy(alpha = 0.35f),
            ),
        ) {
            if (uiState.isSelectingFolder) {
                CircularProgressIndicator(modifier = Modifier.height(18.dp), strokeWidth = 2.dp)
            } else {
                Text("[ SELECT DRIVE FOLDER ]", fontWeight = FontWeight.Bold)
            }
        }
        if (uiState.driveTarget != null) {
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = onClearFolder,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag(ClearDriveFolderButtonTag)
                    .semantics { contentDescription = "Google Driveフォルダ設定を解除" },
                enabled = uiState.canClear,
                shape = ActionShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = OrionAmber,
                    disabledContainerColor = Color.Transparent,
                ),
            ) {
                Text("[ CLEAR TARGET ]", fontWeight = FontWeight.Bold)
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
            .padding(16.dp)
            .semantics { contentDescription = "設定状態: ${status.message}" },
    ) {
        Text(status.code, color = status.color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(status.message, color = MaterialTheme.colorScheme.onBackground)
    }
}

private data class SettingsStatus(val code: String, val message: String, val color: Color)

private fun settingsStatus(uiState: SettingsUiState): SettingsStatus = when {
    uiState.isLoading -> SettingsStatus("CONFIG DATA // LOADING", "保存済み設定を読み込んでいます。", OrionCyan)
    uiState.isSelectingFolder -> SettingsStatus("DRIVE AUTH // ACTIVE", "Google Driveのフォルダ選択を完了してください。", OrionCyan)
    uiState.isClearing -> SettingsStatus("DRIVE TARGET // CLEARING", "接続設定を解除しています。", OrionCyan)
    uiState.feedback == SettingsFeedback.FolderSaved -> SettingsStatus("DRIVE TARGET // CONNECTED", "対象フォルダを保存しました。取得はIncoming画面から手動実行します。", OrionCyan)
    uiState.feedback == SettingsFeedback.Cleared -> SettingsStatus("DRIVE TARGET // CLEARED", "Google Driveフォルダの設定を解除しました。", OrionAmber)
    uiState.feedback == SettingsFeedback.SelectionFailed -> SettingsStatus("DRIVE AUTH // ERROR", "フォルダを設定できませんでした。再試行してください。", OrionError)
    uiState.feedback == SettingsFeedback.LoadFailed -> SettingsStatus("CONFIG DATA // ERROR", "保存済み設定を読み込めませんでした。", OrionError)
    uiState.driveTarget == null -> SettingsStatus("DRIVE TARGET // NOT CONFIGURED", "Google Driveフォルダは未設定です。", OrionAmber)
    else -> SettingsStatus("DRIVE TARGET // READY", "自動通信は行いません。Incoming画面のSYNCで取得します。", OrionCyan)
}

private val PanelShape = CutCornerShape(topStart = 18.dp, bottomEnd = 18.dp)
private val ActionShape = CutCornerShape(topStart = 10.dp, bottomEnd = 10.dp)

@Preview(showBackground = true, backgroundColor = 0xFF030812, widthDp = 393, heightDp = 620)
@Composable
private fun SettingsScreenUnsetPreview() {
    OrionTheme { SettingsScreen(SettingsUiState(isLoading = false), {}, {}) }
}

@Preview(showBackground = true, backgroundColor = 0xFF030812, widthDp = 393, heightDp = 620)
@Composable
private fun SettingsScreenConnectedPreview() {
    OrionTheme {
        SettingsScreen(
            SettingsUiState(
                driveTarget = GoogleDriveTarget("folder-id", "ORION/Incoming"),
                isLoading = false,
                feedback = SettingsFeedback.FolderSaved,
            ),
            {},
            {},
        )
    }
}
