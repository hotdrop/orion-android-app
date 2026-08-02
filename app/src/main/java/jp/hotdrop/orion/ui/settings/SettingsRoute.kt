package jp.hotdrop.orion.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import jp.hotdrop.orion.ui.drive.GoogleDriveAuthorizationResult
import jp.hotdrop.orion.ui.drive.rememberGoogleDriveAuthorization

@Composable
fun SettingsRoute(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val authorizeDrive = rememberGoogleDriveAuthorization { result ->
        when (result) {
            is GoogleDriveAuthorizationResult.Authorized -> {
                viewModel.openFolderBrowser(result.accessToken)
            }
            GoogleDriveAuthorizationResult.Cancelled -> viewModel.cancelFolderSelection()
            is GoogleDriveAuthorizationResult.Failed -> {
                viewModel.reportFolderSelectionFailure(result.cause)
            }
        }
    }

    SettingsScreen(
        uiState = uiState,
        onSelectFolder = {
            if (viewModel.beginFolderSelection()) {
                authorizeDrive()
            }
        },
        onClearFolder = viewModel::clearDriveTarget,
        onOpenFolder = viewModel::openFolder,
        onNavigateToParentFolder = viewModel::navigateToParentFolder,
        onConfirmFolder = viewModel::saveCurrentFolder,
        onCancelFolderSelection = viewModel::cancelFolderSelection,
        modifier = modifier,
    )
}
