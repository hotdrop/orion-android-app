package jp.hotdrop.orion.ui.drive

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.auth.api.identity.AuthorizationResult
import jp.hotdrop.orion.data.remote.GoogleDriveAuthorizationClient

sealed interface GoogleDriveAuthorizationResult {
    data class Authorized(val accessToken: String) : GoogleDriveAuthorizationResult

    data object Cancelled : GoogleDriveAuthorizationResult

    data class Failed(val cause: Throwable) : GoogleDriveAuthorizationResult
}

@Composable
fun rememberGoogleDriveAuthorization(
    onResult: (GoogleDriveAuthorizationResult) -> Unit,
): () -> Unit {
    val applicationContext = LocalContext.current.applicationContext
    val authorizationClient = remember(applicationContext) {
        GoogleDriveAuthorizationClient(applicationContext)
    }
    val currentOnResult = rememberUpdatedState(onResult)

    fun deliver(result: GoogleDriveAuthorizationResult) {
        currentOnResult.value(result)
    }

    fun acceptAuthorizationResult(result: AuthorizationResult) {
        val accessToken = result.accessToken?.takeIf(String::isNotBlank)
        if (accessToken == null) {
            deliver(
                GoogleDriveAuthorizationResult.Failed(
                    IllegalStateException("Google Drive authorization returned no access token"),
                ),
            )
        } else {
            deliver(GoogleDriveAuthorizationResult.Authorized(accessToken))
        }
    }

    val authorizationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
    ) { activityResult ->
        if (activityResult.resultCode != Activity.RESULT_OK) {
            deliver(GoogleDriveAuthorizationResult.Cancelled)
            return@rememberLauncherForActivityResult
        }

        runCatching { authorizationClient.resultFromIntent(activityResult.data) }
            .onSuccess(::acceptAuthorizationResult)
            .onFailure { deliver(GoogleDriveAuthorizationResult.Failed(it)) }
    }

    return remember(authorizationClient, authorizationLauncher) {
        {
            authorizationClient.authorizeAccess()
                .addOnSuccessListener { result ->
                    runCatching {
                        if (result.hasResolution()) {
                            val pendingIntent = checkNotNull(result.pendingIntent) {
                                "Google Drive authorization resolution has no PendingIntent"
                            }
                            authorizationLauncher.launch(
                                IntentSenderRequest.Builder(pendingIntent.intentSender).build(),
                            )
                        } else {
                            acceptAuthorizationResult(result)
                        }
                    }.onFailure { deliver(GoogleDriveAuthorizationResult.Failed(it)) }
                }
                .addOnFailureListener { deliver(GoogleDriveAuthorizationResult.Failed(it)) }
        }
    }
}
