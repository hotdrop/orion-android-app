package jp.hotdrop.orion.ui.incoming

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.auth.api.identity.AuthorizationResult
import jp.hotdrop.orion.data.incoming.GoogleDriveAuthorizationClient
import jp.hotdrop.orion.data.incoming.IncomingIntelligenceRepository
import jp.hotdrop.orion.data.settings.SettingsRepository
import androidx.core.net.toUri

@Composable
fun IncomingIntelligenceRoute(
    settingsRepository: SettingsRepository,
    incomingRepository: IncomingIntelligenceRepository,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: IncomingIntelligenceViewModel = viewModel(
        factory = IncomingIntelligenceViewModel.factory(settingsRepository, incomingRepository),
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val authorizationClient = remember(context) { GoogleDriveAuthorizationClient(context) }

    fun acceptAuthorizationResult(result: AuthorizationResult) {
        val accessToken = result.accessToken
        if (accessToken == null) {
            viewModel.reportAuthorizationFailure()
        } else {
            viewModel.synchronize(accessToken)
        }
    }

    val authorizationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
    ) { activityResult ->
        if (activityResult.resultCode != Activity.RESULT_OK) {
            viewModel.reportAuthorizationFailure()
            return@rememberLauncherForActivityResult
        }
        runCatching { authorizationClient.resultFromIntent(activityResult.data) }
            .onSuccess(::acceptAuthorizationResult)
            .onFailure { viewModel.reportAuthorizationFailure() }
    }

    IncomingIntelligenceScreen(
        uiState = uiState,
        onSync = {
            if (viewModel.beginSynchronization()) {
                authorizationClient.authorizeAccess()
                    .addOnSuccessListener { result ->
                        if (result.hasResolution()) {
                            val pendingIntent = result.pendingIntent
                            if (pendingIntent == null) {
                                viewModel.reportAuthorizationFailure()
                            } else {
                                authorizationLauncher.launch(
                                    IntentSenderRequest.Builder(pendingIntent.intentSender).build(),
                                )
                            }
                        } else {
                            acceptAuthorizationResult(result)
                        }
                    }
                    .addOnFailureListener { viewModel.reportAuthorizationFailure() }
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
