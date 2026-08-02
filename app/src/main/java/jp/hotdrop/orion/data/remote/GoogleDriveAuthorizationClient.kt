package jp.hotdrop.orion.data.remote

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
            .setRequestedScopes(listOf(DriveMetadataReadonlyScope))
            .setOptOutIncludingGrantedScopes(true)
            .build(),
    )

    fun resultFromIntent(intent: Intent?): AuthorizationResult =
        client.getAuthorizationResultFromIntent(intent)

    companion object {
        private val DriveMetadataReadonlyScope =
            Scope("https://www.googleapis.com/auth/drive.metadata.readonly")
    }
}
