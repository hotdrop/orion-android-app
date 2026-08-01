package jp.hotdrop.orion.data.incoming

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task

class GoogleDriveAuthorizationClient(context: Context) {
    private val client = Identity.getAuthorizationClient(context)

    fun authorizeAccess(): Task<AuthorizationResult> = client.authorize(
        AuthorizationRequest.builder()
            .setRequestedScopes(listOf(DriveFileScope))
            .build(),
    )

    fun selectFolder(): Task<AuthorizationResult> = client.authorize(
        AuthorizationRequest.builder()
            .setRequestedScopes(listOf(DriveFileScope))
            .setOptOutIncludingGrantedScopes(true)
            .setPrompt(AuthorizationRequest.Prompt.CONSENT or AuthorizationRequest.Prompt.SELECT_ACCOUNT)
            .addResourceParameter(
                AuthorizationRequest.ResourceParameter.PICKER_OAUTH_TRIGGER,
                "true",
            )
            .addResourceParameter(
                AuthorizationRequest.ResourceParameter.PICKER_ALLOW_FOLDER_SELECTION,
                "true",
            )
            .addResourceParameter(
                AuthorizationRequest.ResourceParameter.PICKER_MIMETYPES,
                GoogleDriveFolderMimeType,
            )
            .build(),
    )

    fun resultFromIntent(intent: Intent?): AuthorizationResult =
        client.getAuthorizationResultFromIntent(intent)

    companion object {
        const val PickedFileIdsParameter = "picked_file_ids"
        private val DriveFileScope = Scope("https://www.googleapis.com/auth/drive.file")
    }
}
