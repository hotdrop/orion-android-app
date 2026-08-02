package jp.hotdrop.orion.ui.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import jp.hotdrop.orion.R
import jp.hotdrop.orion.ui.settings.uistate.DriveFolderBrowserUiState
import jp.hotdrop.orion.ui.settings.uistate.DriveFolderItem
import jp.hotdrop.orion.ui.theme.OrionAmber
import jp.hotdrop.orion.ui.theme.OrionCyan
import jp.hotdrop.orion.ui.theme.OrionCyanMuted
import jp.hotdrop.orion.ui.theme.OrionDeepNavy
import jp.hotdrop.orion.ui.theme.OrionPanelElevated
import jp.hotdrop.orion.ui.theme.OrionText
import jp.hotdrop.orion.ui.theme.OrionTextMuted
import jp.hotdrop.orion.ui.theme.OrionTheme

internal const val DriveFolderBrowserDialogTag = "drive_folder_browser_dialog"
internal const val ConfirmDriveFolderButtonTag = "confirm_drive_folder_button"
internal const val NavigateUpDriveFolderButtonTag = "navigate_up_drive_folder_button"
internal const val DriveFolderItemTagPrefix = "drive_folder_item_"

@Composable
fun DriveFolderBrowserDialog(
    state: DriveFolderBrowserUiState,
    isSaving: Boolean,
    onOpenFolder: (DriveFolderItem) -> Unit,
    onNavigateUp: () -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        DriveFolderBrowserContent(
            state = state,
            isSaving = isSaving,
            onOpenFolder = onOpenFolder,
            onNavigateUp = onNavigateUp,
            onConfirm = onConfirm,
            onCancel = onCancel,
        )
    }
}

@Composable
private fun DriveFolderBrowserContent(
    state: DriveFolderBrowserUiState,
    isSaving: Boolean,
    onOpenFolder: (DriveFolderItem) -> Unit,
    onNavigateUp: () -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 32.dp)
            .fillMaxWidth()
            .heightIn(min = 420.dp, max = 680.dp)
            .border(1.dp, OrionCyanMuted, FolderBrowserShape)
            .background(OrionPanelElevated, FolderBrowserShape)
            .padding(18.dp)
            .testTag(DriveFolderBrowserDialogTag),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
            Text(
                text = stringResource(R.string.settings_folder_browser_title),
                color = OrionCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.1.sp,
            )
            Text(
                text = stringResource(R.string.settings_folder_browser_path_label),
                color = OrionTextMuted,
                fontSize = 10.sp,
                letterSpacing = 1.sp,
            )
        val currentFolderDescription = stringResource(
            R.string.settings_folder_browser_current_folder,
            state.currentPath,
        )
        Text(
            text = state.currentPath,
            modifier = Modifier.semantics { contentDescription = currentFolderDescription },
            color = OrionText,
            style = MaterialTheme.typography.titleMedium,
        )
            TextButton(
                onClick = onNavigateUp,
                modifier = Modifier.testTag(NavigateUpDriveFolderButtonTag),
                enabled = state.canNavigateUp && !state.isLoading && !isSaving,
                colors = ButtonDefaults.textButtonColors(contentColor = OrionAmber),
            ) {
                Text(stringResource(R.string.settings_folder_browser_up))
            }

            when {
                state.isLoading -> Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator(color = OrionCyan)
                    Text(
                        text = stringResource(R.string.settings_folder_browser_loading),
                        modifier = Modifier.padding(top = 12.dp),
                        color = OrionTextMuted,
                    )
                }

                state.folders.isEmpty() -> Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = stringResource(R.string.settings_folder_browser_empty),
                        color = OrionTextMuted,
                    )
                }

                else -> LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(state.folders, key = DriveFolderItem::id) { folder ->
                        val openDescription = stringResource(
                            R.string.settings_folder_browser_open_folder,
                            folder.name,
                        )
                        TextButton(
                            onClick = { onOpenFolder(folder) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("$DriveFolderItemTagPrefix${folder.id}")
                                .semantics { contentDescription = openDescription },
                            colors = ButtonDefaults.textButtonColors(contentColor = OrionText),
                        ) {
                            Text(
                                text = "> ${folder.name}",
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TextButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    enabled = !isSaving,
                    colors = ButtonDefaults.textButtonColors(contentColor = OrionTextMuted),
                ) {
                    Text(stringResource(R.string.settings_folder_browser_cancel))
                }
                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .weight(1.6f)
                        .testTag(ConfirmDriveFolderButtonTag),
                    enabled = !state.isLoading && !isSaving,
                    shape = CutCornerShape(topStart = 8.dp, bottomEnd = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OrionCyan,
                        contentColor = OrionDeepNavy,
                        disabledContainerColor = OrionCyanMuted.copy(alpha = 0.35f),
                        disabledContentColor = OrionTextMuted,
                    ),
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            color = OrionDeepNavy,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.settings_folder_browser_confirm),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
    }
}

private val FolderBrowserShape = CutCornerShape(topStart = 20.dp, bottomEnd = 20.dp)

@Preview(showBackground = true, backgroundColor = 0xFF030812)
@Composable
private fun DriveFolderBrowserDialogPreview() {
    OrionTheme {
        DriveFolderBrowserContent(
            state = DriveFolderBrowserUiState(
                currentFolderId = "root",
                currentPath = "My Drive/技術資料",
                folders = listOf(
                    DriveFolderItem("android", "Android"),
                    DriveFolderItem("research", "Research"),
                ),
                isLoading = false,
                canNavigateUp = true,
            ),
            isSaving = false,
            onOpenFolder = {},
            onNavigateUp = {},
            onConfirm = {},
            onCancel = {},
        )
    }
}
