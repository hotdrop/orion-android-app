package jp.hotdrop.orion.ui.archive

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import jp.hotdrop.orion.model.KnowledgeArchiveEntry
import jp.hotdrop.orion.ui.archive.components.KnowledgeArchiveCard
import jp.hotdrop.orion.ui.archive.uistate.KnowledgeArchiveUiState
import jp.hotdrop.orion.ui.theme.OrionAmber
import jp.hotdrop.orion.ui.theme.OrionCyan
import jp.hotdrop.orion.ui.theme.OrionCyanMuted
import jp.hotdrop.orion.ui.theme.OrionDeepNavy
import jp.hotdrop.orion.ui.theme.OrionError
import jp.hotdrop.orion.ui.theme.OrionPanel
import jp.hotdrop.orion.ui.theme.OrionPanelElevated
import jp.hotdrop.orion.ui.theme.OrionTextMuted
import jp.hotdrop.orion.ui.theme.OrionTheme

internal const val NewArchiveEntryButtonTag = "new_archive_entry_button"
internal const val ArchiveEntryListTag = "archive_entry_list"

@Composable
fun KnowledgeArchiveRoute(
    onCreateEntry: () -> Unit,
    onEditEntry: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: KnowledgeArchiveViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    KnowledgeArchiveScreen(
        uiState = uiState,
        onCreateEntry = onCreateEntry,
        onEditEntry = onEditEntry,
        onOpenUrl = { url ->
            if (!openExternalUrl(context, url)) viewModel.reportUrlOpenFailure()
        },
        onDismissUrlError = viewModel::clearUrlOpenFailure,
        modifier = modifier,
    )
}

@Composable
fun KnowledgeArchiveScreen(
    uiState: KnowledgeArchiveUiState,
    onCreateEntry: () -> Unit,
    onEditEntry: (Long) -> Unit,
    onOpenUrl: (String) -> Unit,
    onDismissUrlError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OrionDeepNavy)
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        ArchiveModuleHeader(
            entryCount = uiState.entries.size,
            onCreateEntry = onCreateEntry,
        )
        Spacer(modifier = Modifier.height(14.dp))

        if (uiState.urlOpenFailed) {
            ArchiveErrorPanel(
                message = "URLを開けるアプリが見つかりませんでした。",
                onDismiss = onDismissUrlError,
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        when {
            uiState.isLoading -> ArchiveLoadingState()
            uiState.loadFailed -> ArchiveLoadErrorState()
            uiState.entries.isEmpty() -> ArchiveEmptyState()
            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag(ArchiveEntryListTag),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(
                    items = uiState.entries,
                    key = KnowledgeArchiveEntry::id,
                ) { entry ->
                    KnowledgeArchiveCard(
                        entry = entry,
                        onEdit = { onEditEntry(entry.id) },
                        onOpenUrl = { onOpenUrl(entry.url) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ArchiveModuleHeader(
    entryCount: Int,
    onCreateEntry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, OrionCyanMuted, CutCornerShape(topStart = 16.dp, bottomEnd = 16.dp))
            .background(OrionPanel.copy(alpha = 0.82f), CutCornerShape(topStart = 16.dp, bottomEnd = 16.dp))
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "MODULE // KA",
                    color = OrionCyan,
                    fontSize = 10.sp,
                    letterSpacing = 1.4.sp,
                )
                Text(
                    text = "LOCAL KNOWLEDGE NODE",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.1.sp,
                )
                Text(
                    text = "$entryCount RECORDS // LOCAL ONLY",
                    color = OrionTextMuted,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp,
                )
            }
            Button(
                onClick = onCreateEntry,
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag(NewArchiveEntryButtonTag)
                    .semantics { contentDescription = "新しい記録を追加" },
                shape = CutCornerShape(topStart = 8.dp, bottomEnd = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OrionCyan,
                    contentColor = OrionDeepNavy,
                ),
            ) {
                Text("[ NEW ]", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
        }
    }
}

@Composable
private fun ArchiveLoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = OrionCyan, strokeWidth = 2.dp)
            Spacer(modifier = Modifier.height(12.dp))
            Text("LOCAL INDEX // LOADING", color = OrionCyan, letterSpacing = 1.2.sp)
        }
    }
}

@Composable
private fun ArchiveEmptyState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .border(1.dp, OrionCyanMuted.copy(alpha = 0.65f))
            .background(OrionPanelElevated.copy(alpha = 0.35f))
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "NO KNOWLEDGE RECORDS",
                color = OrionAmber,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.3.sp,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "気になった技術記事のURLとメモを記録してください。",
                color = OrionTextMuted,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun ArchiveLoadErrorState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "LOCAL INDEX // READ ERROR\n保存済みデータを読み込めませんでした。",
            color = OrionError,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun ArchiveErrorPanel(
    message: String,
    onDismiss: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, OrionError)
            .background(OrionPanelElevated.copy(alpha = 0.5f))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(message, modifier = Modifier.weight(1f), color = OrionError)
        Text(
            text = "[ ACK ]",
            modifier = Modifier
                .heightIn(min = 48.dp)
                .clickable(role = Role.Button, onClick = onDismiss)
                .padding(horizontal = 12.dp, vertical = 14.dp),
            color = OrionCyan,
            fontWeight = FontWeight.Bold,
        )
    }
}

private fun openExternalUrl(context: Context, url: String): Boolean = try {
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    true
} catch (_: ActivityNotFoundException) {
    false
} catch (_: SecurityException) {
    false
}

@Preview(showBackground = true)
@Composable
private fun KnowledgeArchiveEmptyPreview() {
    OrionTheme {
        KnowledgeArchiveScreen(
            uiState = KnowledgeArchiveUiState(isLoading = false),
            onCreateEntry = {},
            onEditEntry = {},
            onOpenUrl = {},
            onDismissUrlError = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun KnowledgeArchivePopulatedPreview() {
    OrionTheme {
        KnowledgeArchiveScreen(
            uiState = KnowledgeArchiveUiState(
                entries = listOf(
                    KnowledgeArchiveEntry(
                        id = 12,
                        title = "Composeのパフォーマンスを安定させるための長い技術記事タイトル",
                        url = "https://developer.android.com/compose",
                        memo = "再コンポーズの境界と安定性について。次の画面設計で試したい。",
                        createdAt = 1,
                        updatedAt = 2,
                    ),
                ),
                isLoading = false,
            ),
            onCreateEntry = {},
            onEditEntry = {},
            onOpenUrl = {},
            onDismissUrlError = {},
        )
    }
}
