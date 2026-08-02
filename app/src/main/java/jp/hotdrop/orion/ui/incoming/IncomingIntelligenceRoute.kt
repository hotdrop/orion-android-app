package jp.hotdrop.orion.ui.incoming

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.core.net.toUri
import jp.hotdrop.orion.ui.drive.GoogleDriveAuthorizationResult
import jp.hotdrop.orion.ui.drive.rememberGoogleDriveAuthorization

@Composable
fun IncomingIntelligenceRoute(
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: IncomingIntelligenceViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val authorizeDrive = rememberGoogleDriveAuthorization { result ->
        when (result) {
            is GoogleDriveAuthorizationResult.Authorized -> viewModel.synchronize(result.accessToken)
            GoogleDriveAuthorizationResult.Cancelled -> viewModel.reportAuthorizationFailure()
            is GoogleDriveAuthorizationResult.Failed -> viewModel.reportAuthorizationFailure()
        }
    }

    IncomingIntelligenceScreen(
        uiState = uiState,
        onSync = {
            if (viewModel.beginSynchronization()) {
                authorizeDrive()
            }
        },
        onOpenSettings = onOpenSettings,
        onOpenDocument = { document ->
            if (openExternalDocument(context, document.webUrl)) {
                viewModel.markDocumentOpened(document.id)
            }
        },
        modifier = modifier,
    )
}

private fun openExternalDocument(context: Context, url: String): Boolean = try {
    context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    true
} catch (_: ActivityNotFoundException) {
    false
} catch (_: SecurityException) {
    false
}
