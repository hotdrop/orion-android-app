package jp.hotdrop.orion.ui.archive

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
import jp.hotdrop.orion.model.KnowledgeArchiveEntry
import jp.hotdrop.orion.ui.archive.components.list.KnowledgeArchiveEntryList
import jp.hotdrop.orion.ui.archive.components.list.KnowledgeArchiveListEmpty
import jp.hotdrop.orion.ui.archive.components.list.KnowledgeArchiveListErrorPanel
import jp.hotdrop.orion.ui.archive.components.list.KnowledgeArchiveListHeader
import jp.hotdrop.orion.ui.archive.components.list.KnowledgeArchiveListLoadError
import jp.hotdrop.orion.ui.archive.components.list.KnowledgeArchiveListLoading
import jp.hotdrop.orion.ui.archive.uistate.KnowledgeArchiveUiState
import jp.hotdrop.orion.ui.theme.OrionDeepNavy
import jp.hotdrop.orion.ui.theme.OrionTheme

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
        KnowledgeArchiveListHeader(
            entryCount = uiState.entries.size,
            onCreateEntry = onCreateEntry,
        )
        Spacer(modifier = Modifier.height(14.dp))

        if (uiState.urlOpenFailed) {
            KnowledgeArchiveListErrorPanel(
                message = "URLを開けるアプリが見つかりませんでした。",
                onDismiss = onDismissUrlError,
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        when {
            uiState.isLoading -> KnowledgeArchiveListLoading()
            uiState.loadFailed -> KnowledgeArchiveListLoadError()
            uiState.entries.isEmpty() -> KnowledgeArchiveListEmpty()
            else -> KnowledgeArchiveEntryList(
                entries = uiState.entries,
                onEditEntry = onEditEntry,
                onOpenUrl = onOpenUrl,
            )
        }
    }
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
