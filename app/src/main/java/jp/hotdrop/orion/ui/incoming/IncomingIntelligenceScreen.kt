package jp.hotdrop.orion.ui.incoming

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.hotdrop.orion.model.IncomingIntelligenceDocument
import jp.hotdrop.orion.ui.theme.OrionAmber
import jp.hotdrop.orion.ui.theme.OrionCyan
import jp.hotdrop.orion.ui.theme.OrionCyanMuted
import jp.hotdrop.orion.ui.theme.OrionDeepNavy
import jp.hotdrop.orion.ui.theme.OrionError
import jp.hotdrop.orion.ui.theme.OrionPanel
import jp.hotdrop.orion.ui.theme.OrionPanelElevated
import jp.hotdrop.orion.ui.theme.OrionTextMuted
import jp.hotdrop.orion.ui.theme.OrionTheme

internal const val IncomingSyncButtonTag = "incoming_sync_button"
internal const val IncomingDocumentListTag = "incoming_document_list"

@Composable
fun IncomingIntelligenceScreen(
    uiState: IncomingIntelligenceUiState,
    onSync: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenDocument: (IncomingIntelligenceDocument) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OrionDeepNavy)
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        IncomingModuleHeader(
            uiState = uiState,
            onSync = onSync,
        )
        Spacer(modifier = Modifier.height(12.dp))

        IncomingStatusPanel(uiState = uiState)
        Spacer(modifier = Modifier.height(12.dp))

        when {
            !uiState.isDriveConfigured -> DriveNotConfiguredState(onOpenSettings = onOpenSettings)
            uiState.documents.isEmpty() && uiState.isSyncing -> InitialSyncState()
            uiState.documents.isEmpty() -> NoDocumentsState(onSync = onSync)
            else -> IncomingDocumentList(
                documents = uiState.documents,
                onOpenDocument = onOpenDocument,
            )
        }
    }
}

@Composable
private fun IncomingModuleHeader(
    uiState: IncomingIntelligenceUiState,
    onSync: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, OrionCyanMuted, ModuleShape)
            .background(OrionPanel.copy(alpha = 0.82f), ModuleShape)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "MODULE // IN",
                color = OrionCyan,
                fontSize = 10.sp,
                letterSpacing = 1.4.sp,
            )
            Text(
                text = "SIGNAL ACQUISITION GRID",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.1.sp,
            )
            Text(
                text = "${uiState.documents.size} SIGNALS // ${uiState.documents.count { it.isNew }} NEW",
                color = OrionTextMuted,
                fontSize = 10.sp,
                letterSpacing = 1.sp,
            )
        }
        Button(
            onClick = onSync,
            modifier = Modifier
                .heightIn(min = 48.dp)
                .testTag(IncomingSyncButtonTag)
                .semantics { contentDescription = "Incoming Intelligenceを同期" },
            enabled = uiState.isDriveConfigured && !uiState.isSyncing,
            shape = ActionShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = OrionCyan,
                contentColor = OrionDeepNavy,
                disabledContainerColor = OrionCyanMuted.copy(alpha = 0.35f),
                disabledContentColor = OrionTextMuted,
            ),
        ) {
            if (uiState.isSyncing) {
                CircularProgressIndicator(
                    modifier = Modifier.height(18.dp),
                    color = OrionDeepNavy,
                    strokeWidth = 2.dp,
                )
            } else {
                Text("[ SYNC ]", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
        }
    }
}

@Composable
private fun IncomingStatusPanel(uiState: IncomingIntelligenceUiState) {
    val status = incomingStatus(uiState)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, status.color.copy(alpha = 0.7f))
            .background(OrionPanelElevated.copy(alpha = 0.42f))
            .padding(horizontal = 14.dp, vertical = 11.dp)
            .semantics { contentDescription = "同期状態: ${status.description}" },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = status.code,
                color = status.color,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
            )
            Text(
                text = status.description,
                color = OrionTextMuted,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        uiState.lastSyncedAtLabel?.let { label ->
            Text(
                text = "LAST // $label",
                color = OrionTextMuted,
                fontSize = 9.sp,
                letterSpacing = 0.8.sp,
            )
        }
    }
}

@Composable
private fun IncomingDocumentList(
    documents: List<IncomingIntelligenceDocument>,
    onOpenDocument: (IncomingIntelligenceDocument) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag(IncomingDocumentListTag),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(items = documents, key = IncomingIntelligenceDocument::id) { document ->
            IncomingDocumentCard(
                document = document,
                onClick = { onOpenDocument(document) },
            )
        }
    }
}

@Composable
private fun IncomingDocumentCard(
    document: IncomingIntelligenceDocument,
    onClick: () -> Unit,
) {
    val signalColor = if (document.isNew) OrionCyan else OrionCyanMuted
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, signalColor.copy(alpha = 0.8f), CardShape)
            .background(OrionPanelElevated.copy(alpha = 0.55f), CardShape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics {
                contentDescription = "${document.title}をGoogleドキュメントで開く"
            }
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (document.isNew) "NEW SIGNAL // UNREAD" else "ARCHIVED SIGNAL // CACHED",
                modifier = Modifier.weight(1f),
                color = signalColor,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.1.sp,
            )
            Text(
                text = document.updatedAtLabel,
                color = OrionTextMuted,
                fontSize = 9.sp,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = document.title,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "PATH // ${document.relativePath}",
                modifier = Modifier.weight(1f),
                color = OrionTextMuted,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "[ OPEN ]",
                color = OrionCyan,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
            )
        }
    }
}

@Composable
private fun DriveNotConfiguredState(onOpenSettings: () -> Unit) {
    IncomingEmptyPanel(
        code = "DRIVE TARGET // NOT CONFIGURED",
        message = "SettingsでGoogle Driveの対象フォルダを設定してください。",
        color = OrionAmber,
    ) {
        IncomingOutlineAction(
            label = "OPEN CONFIG",
            accessibilityLabel = "Settingsを開いてGoogle Driveを設定",
            onClick = onOpenSettings,
        )
    }
}

@Composable
private fun InitialSyncState() {
    IncomingEmptyPanel(
        code = "SIGNAL SCAN // IN PROGRESS",
        message = "Google Driveから技術情報を取得しています。",
        color = OrionCyan,
    ) {
        CircularProgressIndicator(color = OrionCyan, strokeWidth = 2.dp)
    }
}

@Composable
private fun NoDocumentsState(onSync: () -> Unit) {
    IncomingEmptyPanel(
        code = "NO SIGNALS DETECTED",
        message = "対象フォルダにGoogleドキュメントがありません。",
        color = OrionAmber,
    ) {
        IncomingOutlineAction(
            label = "SCAN AGAIN",
            accessibilityLabel = "Google Driveを再同期",
            onClick = onSync,
        )
    }
}

@Composable
private fun IncomingEmptyPanel(
    code: String,
    message: String,
    color: Color,
    action: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .border(1.dp, color.copy(alpha = 0.65f), CardShape)
            .background(OrionPanelElevated.copy(alpha = 0.35f), CardShape)
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = code,
                color = color,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                color = OrionTextMuted,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(18.dp))
            action()
        }
    }
}

@Composable
private fun IncomingOutlineAction(
    label: String,
    accessibilityLabel: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .heightIn(min = 48.dp)
            .border(1.dp, OrionCyan, ActionShape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = accessibilityLabel }
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "[ $label ]",
            color = OrionCyan,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
        )
    }
}

private data class IncomingStatus(
    val code: String,
    val description: String,
    val color: Color,
)

private fun incomingStatus(uiState: IncomingIntelligenceUiState): IncomingStatus = when {
    !uiState.isDriveConfigured -> IncomingStatus(
        code = "UPLINK // STANDBY",
        description = "同期先が未設定です。",
        color = OrionAmber,
    )
    uiState.isSyncing -> IncomingStatus(
        code = "UPLINK // RECEIVING",
        description = "同期中です。保存済みの信号は引き続き参照できます。",
        color = OrionCyan,
    )
    uiState.errorMessage != null -> IncomingStatus(
        code = "UPLINK // ERROR",
        description = uiState.errorMessage,
        color = OrionError,
    )
    uiState.isOffline -> IncomingStatus(
        code = "UPLINK // OFFLINE CACHE",
        description = "オフラインのため、最後に取得した信号を表示しています。",
        color = OrionAmber,
    )
    else -> IncomingStatus(
        code = "UPLINK // READY",
        description = "Google Drive同期チャネルは待機中です。",
        color = OrionCyan,
    )
}

private val ModuleShape = CutCornerShape(topStart = 16.dp, bottomEnd = 16.dp)
private val CardShape = CutCornerShape(topStart = 14.dp, bottomEnd = 14.dp)
private val ActionShape = CutCornerShape(topStart = 8.dp, bottomEnd = 8.dp)

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
