package jp.hotdrop.orion.ui.archive.components.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import jp.hotdrop.orion.model.KnowledgeArchiveEntry
import jp.hotdrop.orion.ui.theme.OrionTheme

internal const val ArchiveEntryListTag = "archive_entry_list"

@Composable
fun KnowledgeArchiveEntryList(
    entries: List<KnowledgeArchiveEntry>,
    onEditEntry: (Long) -> Unit,
    onOpenUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag(ArchiveEntryListTag),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(
            items = entries,
            key = KnowledgeArchiveEntry::id,
        ) { entry ->
            KnowledgeArchiveEntryCard(
                entryId = entry.id,
                title = entry.title,
                memo = entry.memo,
                onEdit = { onEditEntry(entry.id) },
                onOpenUrl = { onOpenUrl(entry.url) },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun KnowledgeArchiveEntryListPreview() {
    OrionTheme {
        KnowledgeArchiveEntryList(
            entries = listOf(
                KnowledgeArchiveEntry(
                    id = 12,
                    title = "Composeのパフォーマンスを安定させるための長い技術記事タイトル",
                    url = "https://developer.android.com/compose",
                    memo = "再コンポーズの境界と安定性について。",
                    createdAt = 1,
                    updatedAt = 2,
                ),
            ),
            onEditEntry = {},
            onOpenUrl = {},
        )
    }
}
