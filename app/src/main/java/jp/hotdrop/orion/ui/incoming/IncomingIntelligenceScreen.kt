package jp.hotdrop.orion.ui.incoming

import jp.hotdrop.orion.ui.theme.OrionTextMuted
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
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
    onToggleFavorite: (String) -> Unit = {},
    onFavoritesOnlyChanged: (Boolean) -> Unit = {},
    onEditMemo: (String) -> Unit = {},
    onRequestDelete: (IncomingIntelligenceDocument) -> Unit = {},
    onDismissDelete: () -> Unit = {},
    onConfirmDelete: () -> Unit = {},
) {
    val status = uiState.toStatusPresentation()
    val visibleDocuments = if (uiState.favoritesOnly) uiState.documents.filter { it.isFavorite } else uiState.documents
    uiState.pendingDelete?.let { document ->
        AlertDialog(
            onDismissRequest = onDismissDelete,
            title = { Text("ローカル記録を削除") },
            text = {
                Column {
                    Text("「${document.title}」のお気に入りとメモを削除します。Driveの原本は削除しません。")
                    uiState.actionErrorMessage?.let { Text(it) }
                }
            },
            confirmButton = { TextButton(onClick = onConfirmDelete, enabled = !uiState.isDeleting) { Text("削除") } },
            dismissButton = { TextButton(onClick = onDismissDelete, enabled = !uiState.isDeleting) { Text("キャンセル") } },
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OrionDeepNavy)
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        IncomingIntelligenceHeader(
            lastSyncedAtLabel = uiState.lastSyncedAtLabel,
            isSyncing = uiState.isSyncing,
            syncEnabled = uiState.isDriveConfigured && !uiState.isSyncing,
            onSync = onSync,
        )
        Spacer(modifier = Modifier.height(12.dp))

        uiState.actionErrorMessage?.let { message ->
            IncomingIntelligenceStatusPanel(
                code = "LOCAL // ERROR", description = message, tone = IncomingIntelligenceStatusTone.Error,
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
        status?.let { presentation ->
            IncomingIntelligenceStatusPanel(
                code = presentation.code,
                description = presentation.description,
                tone = presentation.tone,
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FilterChip(
                selected = !uiState.favoritesOnly,
                onClick = { onFavoritesOnlyChanged(false) },
                label = { Text("ALL") }
            )
            FilterChip(
                selected = uiState.favoritesOnly,
                onClick = { onFavoritesOnlyChanged(true) },
                label = { Text("MARK") }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        when {
            !uiState.isDriveConfigured && uiState.documents.isEmpty() -> IncomingIntelligenceDriveNotConfigured(
                onOpenSettings = onOpenSettings,
            )

            uiState.favoritesOnly && visibleDocuments.isEmpty() -> Text("お気に入りの資料はありません。", color = OrionTextMuted)
            uiState.documents.isEmpty() && uiState.isSyncing -> IncomingIntelligenceInitialSync()
            uiState.documents.isEmpty() -> IncomingIntelligenceNoDocuments(onSync = onSync)
            else -> IncomingIntelligenceDocumentList(
                documents = visibleDocuments,
                onToggleFavorite = onToggleFavorite,
                onEditMemo = onEditMemo,
                onRequestDelete = onRequestDelete,
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
        isFavorite = true,
        memo = "設計判断と評価方法が詳しい。次の調査で参照する。\n検索精度を改善する手順と、比較の観点を整理している。",
        isSyncTarget = false,
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
@Preview(showBackground = true, widthDp = 393, heightDp = 852, fontScale = 1.6f)
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
