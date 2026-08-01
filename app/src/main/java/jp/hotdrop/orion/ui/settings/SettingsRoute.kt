package jp.hotdrop.orion.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import jp.hotdrop.orion.data.remote.GoogleDriveRemoteDataSource
import jp.hotdrop.orion.data.SettingsRepository

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
    val selectDriveFolder = rememberGoogleDriveFolderPicker { result ->
        when (result) {
            is GoogleDriveFolderPickerResult.Selected -> viewModel.saveSelectedFolder(
                accessToken = result.accessToken,
                folderId = result.folderId,
            )

            GoogleDriveFolderPickerResult.Cancelled -> viewModel.cancelFolderSelection()
            is GoogleDriveFolderPickerResult.Failed -> {
                viewModel.reportFolderSelectionFailure(result.cause)
            }
        }
    }

    SettingsScreen(
        uiState = uiState,
        onSelectFolder = {
            if (viewModel.beginFolderSelection()) {
                selectDriveFolder()
            }
        },
        onClearFolder = viewModel::clearDriveTarget,
        modifier = modifier,
    )
}
