package jp.hotdrop.orion.ui.intelligence

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import jp.hotdrop.orion.ui.intelligence.components.ConsoleAction
import jp.hotdrop.orion.ui.intelligence.components.ConsoleMessage
import jp.hotdrop.orion.ui.intelligence.components.KeywordLabels
import jp.hotdrop.orion.ui.intelligence.components.RecordPanel
import jp.hotdrop.orion.ui.intelligence.components.RecordText
import jp.hotdrop.orion.ui.intelligence.components.recordDate
import jp.hotdrop.orion.ui.theme.OrionCyan
import jp.hotdrop.orion.ui.theme.OrionTheme

/**
 * 記録の全文と、現在のKeywordを共有する記録へのリンクを表示する。
 */
@Composable
fun RecordDetailScreen(
    state: ArchiveUiState,
    focus: Boolean,
    id: Long,
    onEdit: () -> Unit,
    onSignal: (Long) -> Unit,
    onFocus: (Long) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val signal = state.archive.signals.find { it.id == id }
    val focusRecord = state.archive.focuses.find { it.id == id }
    val recordMissing = if (focus) focusRecord == null else signal == null

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        when {
            state.loading -> item { ConsoleMessage(message = "LOADING…") }
            state.error != null -> item {
                ConsoleMessage(message = state.error, onRetry = onRetry)
            }
            recordMissing -> item {
                ConsoleMessage(message = "記録が見つかりません。削除された可能性があります。")
            }
            else -> {
                val createdAt = if (focus) {
                    requireNotNull(focusRecord).generatedAt
                } else {
                    requireNotNull(signal).createdAt
                }
                val body = if (focus) requireNotNull(focusRecord).analysis else requireNotNull(signal).body
                val keywords = if (focus) {
                    requireNotNull(focusRecord).keywords
                } else {
                    requireNotNull(signal).keywords
                }
                item {
                    Text(text = recordDate(createdAt), color = OrionCyan)
                    ConsoleAction(label = "EDIT", onClick = onEdit)
                }
                item {
                    RecordText(
                        label = if (focus) "CHATGPT ANALYSIS" else "SIGNAL",
                        text = body,
                    )
                }
                if (focus) {
                    item { RecordText(label = "MY NOTE", text = requireNotNull(focusRecord).note) }
                }
                item { KeywordLabels(keywords = keywords) }
                item {
                    Text(
                        text = if (focus) "RELATED SIGNALS" else "RELATED FOCUS",
                        color = OrionCyan,
                    )
                }
                val links = state.archive.links.filter { link ->
                    if (focus) link.focusId == id else link.signalId == id
                }
                if (links.isEmpty()) {
                    item { ConsoleMessage(message = "共通Keywordを持つ記録はまだありません。") }
                }
                if (focus) {
                    val relatedSignals = state.archive.signals.filter { signalItem ->
                        links.any { it.signalId == signalItem.id }
                    }
                    items(items = relatedSignals, key = { it.id }) { linked ->
                        RecordPanel(
                            label = "SIGNAL ${recordDate(linked.createdAt)}",
                            body = linked.body,
                            keywords = linked.keywords,
                            onOpen = { onSignal(linked.id) },
                        )
                    }
                } else {
                    val relatedFocuses = state.archive.focuses.filter { focusItem ->
                        links.any { it.focusId == focusItem.id }
                    }
                    items(items = relatedFocuses, key = { it.id }) { linked ->
                        RecordPanel(
                            label = "FOCUS ${recordDate(linked.generatedAt)}",
                            body = linked.analysis,
                            keywords = linked.keywords,
                            onOpen = { onFocus(linked.id) },
                        )
                    }
                }
            }
        }
    }
}


@Preview
@Composable
private fun FocusDetailPreview() {
    OrionTheme {
        Surface {
            RecordDetailScreen(
                state = ArchiveUiState(
                    archive = previewArchive,
                    loading = false,
                ),
                focus = true,
                id = 1,
                onEdit = {},
                onSignal = {},
                onFocus = {},
                onRetry = {},
            )
        }
    }
}

@Preview
@Composable
private fun SignalDetailPreview() {
    OrionTheme {
        Surface {
            RecordDetailScreen(
                state = ArchiveUiState(
                    archive = previewArchive,
                    loading = false,
                ),
                focus = false,
                id = 1,
                onEdit = {},
                onSignal = {},
                onFocus = {},
                onRetry = {},
            )
        }
    }
}
