package jp.hotdrop.orion.ui.incoming

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import jp.hotdrop.orion.model.IncomingIntelligenceDocument
import jp.hotdrop.orion.ui.incoming.components.IncomingIntelligenceDocumentList
import jp.hotdrop.orion.ui.incoming.components.IncomingIntelligenceDriveNotConfigured
import jp.hotdrop.orion.ui.incoming.components.IncomingIntelligenceHeader
import jp.hotdrop.orion.ui.incoming.components.IncomingIntelligenceInitialSync
import jp.hotdrop.orion.ui.incoming.components.IncomingIntelligenceNoDocuments
import jp.hotdrop.orion.ui.incoming.components.IncomingIntelligenceStatusPanel
import jp.hotdrop.orion.ui.incoming.uistate.IncomingIntelligenceUiState
import jp.hotdrop.orion.ui.theme.OrionDeepNavy
import jp.hotdrop.orion.ui.theme.OrionTheme

@Composable
fun IncomingIntelligenceScreen(
    uiState: IncomingIntelligenceUiState,
    onSync: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenDocument: (IncomingIntelligenceDocument) -> Unit,
    modifier: Modifier = Modifier,
) {
    val status = uiState.toStatusPresentation()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OrionDeepNavy)
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        IncomingIntelligenceHeader(
            documentCount = uiState.documents.size,
            newDocumentCount = uiState.documents.count(IncomingIntelligenceDocument::isNew),
            isSyncing = uiState.isSyncing,
            syncEnabled = uiState.isDriveConfigured && !uiState.isSyncing,
            onSync = onSync,
        )
        Spacer(modifier = Modifier.height(12.dp))

        IncomingIntelligenceStatusPanel(
            code = status.code,
            description = status.description,
            tone = status.tone,
            lastSyncedAtLabel = uiState.lastSyncedAtLabel,
        )
        Spacer(modifier = Modifier.height(12.dp))

        when {
            !uiState.isDriveConfigured -> IncomingIntelligenceDriveNotConfigured(
                onOpenSettings = onOpenSettings,
            )

            uiState.documents.isEmpty() && uiState.isSyncing -> IncomingIntelligenceInitialSync()
            uiState.documents.isEmpty() -> IncomingIntelligenceNoDocuments(onSync = onSync)
            else -> IncomingIntelligenceDocumentList(
                documents = uiState.documents,
                onOpenDocument = onOpenDocument,
            )
        }
    }
}

private val PreviewDocuments = listOf(
    IncomingIntelligenceDocument(
        id = "compose-performance",
        title = "Jetpack Composeの描画パフォーマンスを安定させるための実践ガイド",
        updatedAtLabel = "08/01 09:42",
        relativePath = "Android/Compose/Weekly",
        webUrl = "https://docs.google.com/document/d/compose-performance",
        isNew = true,
    ),
    IncomingIntelligenceDocument(
        id = "agentic-rag",
        title = "Agentic RAG: Production Architecture Notes",
        updatedAtLabel = "07/29 22:16",
        relativePath = "AI/RAG/Research/Long/Nested/Path",
        webUrl = "https://docs.google.com/document/d/agentic-rag",
        isNew = false,
    ),
)

@Preview(showBackground = true, backgroundColor = 0xFF030812, widthDp = 393, heightDp = 620)
@Composable
private fun IncomingIntelligenceNotConfiguredPreview() {
    OrionTheme {
        IncomingIntelligenceScreen(
            uiState = IncomingIntelligenceUiState(),
            onSync = {},
            onOpenSettings = {},
            onOpenDocument = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF030812, widthDp = 393, heightDp = 620)
@Composable
private fun IncomingIntelligencePopulatedPreview() {
    OrionTheme {
        IncomingIntelligenceScreen(
            uiState = IncomingIntelligenceUiState(
                isDriveConfigured = true,
                documents = PreviewDocuments,
                lastSyncedAtLabel = "08/01 09:45",
            ),
            onSync = {},
            onOpenSettings = {},
            onOpenDocument = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF030812, widthDp = 393, heightDp = 620)
@Composable
private fun IncomingIntelligenceOfflinePreview() {
    OrionTheme {
        IncomingIntelligenceScreen(
            uiState = IncomingIntelligenceUiState(
                isDriveConfigured = true,
                documents = PreviewDocuments,
                isOffline = true,
                lastSyncedAtLabel = "07/31 23:10",
            ),
            onSync = {},
            onOpenSettings = {},
            onOpenDocument = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF030812, widthDp = 393, heightDp = 620)
@Composable
private fun IncomingIntelligenceSyncingPreview() {
    OrionTheme {
        IncomingIntelligenceScreen(
            uiState = IncomingIntelligenceUiState(
                isDriveConfigured = true,
                documents = PreviewDocuments,
                isSyncing = true,
                lastSyncedAtLabel = "08/01 09:45",
            ),
            onSync = {},
            onOpenSettings = {},
            onOpenDocument = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF030812, widthDp = 393, heightDp = 620)
@Composable
private fun IncomingIntelligenceErrorPreview() {
    OrionTheme {
        IncomingIntelligenceScreen(
            uiState = IncomingIntelligenceUiState(
                isDriveConfigured = true,
                documents = PreviewDocuments,
                errorMessage = "認証を確認してから再試行してください。",
                lastSyncedAtLabel = "07/31 23:10",
            ),
            onSync = {},
            onOpenSettings = {},
            onOpenDocument = {},
        )
    }
}
