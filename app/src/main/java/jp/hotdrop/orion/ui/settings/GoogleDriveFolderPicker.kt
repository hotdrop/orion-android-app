package jp.hotdrop.orion.ui.settings

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

internal sealed interface GoogleDriveFolderPickerResult {
    data class Selected(val accessToken: String, val folderId: String) : GoogleDriveFolderPickerResult

    data object Cancelled : GoogleDriveFolderPickerResult

    data class Failed(val cause: Throwable) : GoogleDriveFolderPickerResult
}

@Composable
internal fun rememberGoogleDriveFolderPicker(
    onResult: (GoogleDriveFolderPickerResult) -> Unit,
): () -> Unit {
    val applicationContext = LocalContext.current.applicationContext
    val authorizationClient = remember(applicationContext) {
        GoogleDriveAuthorizationClient(applicationContext)
    }
    val currentOnResult = rememberUpdatedState(onResult)

    fun deliver(result: GoogleDriveFolderPickerResult) {
        currentOnResult.value(result)
    }

    fun acceptAuthorizationResult(result: AuthorizationResult) {
        deliver(result.toFolderPickerResult())
    }

    val authorizationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
    ) { activityResult ->
        if (activityResult.resultCode != Activity.RESULT_OK) {
            deliver(GoogleDriveFolderPickerResult.Cancelled)
            return@rememberLauncherForActivityResult
        }

        runCatching { authorizationClient.resultFromIntent(activityResult.data) }
            .onSuccess(::acceptAuthorizationResult)
            .onFailure { deliver(GoogleDriveFolderPickerResult.Failed(it)) }
    }

    return remember(authorizationClient, authorizationLauncher) {
        {
            runCatching { authorizationClient.selectFolder() }
                .onSuccess { task ->
                    task.addOnSuccessListener { result ->
                        runCatching {
                            if (result.hasResolution()) {
                                val pendingIntent = checkNotNull(result.pendingIntent) {
                                    "Google Drive authorization resolution is missing its PendingIntent"
                                }
                                authorizationLauncher.launch(
                                    IntentSenderRequest.Builder(pendingIntent.intentSender).build(),
                                )
                            } else {
                                acceptAuthorizationResult(result)
                            }
                        }.onFailure { deliver(GoogleDriveFolderPickerResult.Failed(it)) }
                    }.addOnFailureListener {
                        deliver(GoogleDriveFolderPickerResult.Failed(it))
                    }
                }
                .onFailure { deliver(GoogleDriveFolderPickerResult.Failed(it)) }
        }
    }
}

private fun AuthorizationResult.toFolderPickerResult(): GoogleDriveFolderPickerResult {
    val token = accessToken?.takeIf(String::isNotBlank)
    val folderId = tokenResponseParams
        ?.getString(GoogleDriveAuthorizationClient.PICKED_FILE_IDS_PARAMETER)
        ?.substringBefore(',')
        ?.takeIf(String::isNotBlank)

    return if (token != null && folderId != null) {
        GoogleDriveFolderPickerResult.Selected(token, folderId)
    } else {
        GoogleDriveFolderPickerResult.Failed(
            IllegalStateException("Google Drive folder picker returned an incomplete result"),
        )
    }
}
