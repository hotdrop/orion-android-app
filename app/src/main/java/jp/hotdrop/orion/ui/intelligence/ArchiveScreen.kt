package jp.hotdrop.orion.ui.intelligence

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import jp.hotdrop.orion.model.intelligence.FocusRecord
import jp.hotdrop.orion.model.intelligence.IntelligenceArchive
import jp.hotdrop.orion.model.intelligence.RelatedFocus
import jp.hotdrop.orion.model.intelligence.SignalRecord
import jp.hotdrop.orion.ui.intelligence.components.ConsoleAction
import jp.hotdrop.orion.ui.intelligence.components.ConsoleMessage
import jp.hotdrop.orion.ui.intelligence.components.RecordPanel
import jp.hotdrop.orion.ui.intelligence.components.recordDate
import jp.hotdrop.orion.ui.theme.OrionTextMuted
import jp.hotdrop.orion.ui.theme.OrionTheme

/**
 * SignalとFocusの一覧を切り替え、作成・分析・詳細表示への操作を通知する。
 */
@Composable
fun ArchiveScreen(
    state: ArchiveUiState,
    focusTab: Boolean,
    onTab: (Boolean) -> Unit,
    onNewSignal: () -> Unit,
    onAnalysis: () -> Unit,
    onSignal: (Long) -> Unit,
    onFocus: (Long) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isEmpty = if (focusTab) {
        state.archive.focuses.isEmpty()
    } else {
        state.archive.signals.isEmpty()
    }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = !focusTab,
                    onClick = { onTab(false) },
                    label = { Text("SIGNAL") },
                )
                FilterChip(
                    selected = focusTab,
                    onClick = { onTab(true) },
                    label = { Text("FOCUS") },
                )
            }
            Text(
                text = if (focusTab) "関心と思考を俯瞰した記録" else "何に反応したかを残す",
                color = OrionTextMuted,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!focusTab) {
                    ConsoleAction(
                        label = "NEW SIGNAL",
                        onClick = onNewSignal,
                        modifier = Modifier.weight(1f),
                    )
                }
                ConsoleAction(
                    label = if (state.archive.hasDraft) "RESUME ANALYSIS" else "FOCUS ANALYSIS",
                    onClick = onAnalysis,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        if (state.loading) {
            item { ConsoleMessage(message = "LOADING…") }
        }
        state.error?.let { error ->
            item { ConsoleMessage(message = error, onRetry = onRetry) }
        }
        if (!state.loading && state.error == null && isEmpty) {
            item {
                ConsoleMessage(
                    message = if (focusTab) {
                        "Focusはまだありません。Signalを記録し、分析を始めましょう。"
                    } else {
                        "気になったことを、自分の言葉で記録しましょう。"
                    },
                )
            }
        }
        if (focusTab) {
            items(items = state.archive.focuses, key = { it.id }) { focus ->
                RecordPanel(
                    label = "FOCUS ${recordDate(focus.generatedAt)}",
                    body = focus.analysis,
                    keywords = focus.keywords,
                    onOpen = { onFocus(focus.id) },
                )
            }
        } else {
            items(items = state.archive.signals, key = { it.id }) { signal ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    RecordPanel(
                        label = recordDate(signal.createdAt),
                        body = signal.body,
                        keywords = signal.keywords,
                        onOpen = { onSignal(signal.id) },
                    )
                    state.archive.links
                        .filter { it.signalId == signal.id }
                        .forEach { link ->
                            ConsoleAction(
                                label = "↳ FOCUS ${recordDate(link.generatedAt)}",
                                onClick = { onFocus(link.focusId) },
                            )
                        }
                }
            }
        }
    }
}

internal val previewArchive = IntelligenceArchive(
    signals = listOf(
        SignalRecord(
            id = 1,
            body = "LLMを生成器ではなく判定器として使う考え方が面白い",
            createdAt = 1790121600000,
            updatedAt = 1790121600000,
            keywords = listOf("Jev", "RLCD"),
        ),
    ),
    focuses = listOf(
        FocusRecord(
            id = 1,
            analysis = "生成ではなく選択するという視点が共通しています。\n\n小型LLMと学習手法の関連を追ってみましょう。",
            note = "RLCDとつながる視点はなかった。",
            generatedAt = 1790121600000,
            updatedAt = 1790121600000,
            keywords = listOf("RLCD", "Small LLM"),
        ),
    ),
    links = listOf(
        RelatedFocus(
            signalId = 1,
            focusId = 1,
            generatedAt = 1790121600000,
        ),
    ),
)

@Preview
@Composable
private fun SignalListPreview() {
    OrionTheme {
        Surface {
            ArchiveScreen(
                state = ArchiveUiState(
                    archive = previewArchive,
                    loading = false,
                ),
                focusTab = false,
                onTab = {},
                onNewSignal = {},
                onAnalysis = {},
                onSignal = {},
                onFocus = {},
                onRetry = {},
            )
        }
    }
}

@Preview
@Composable
private fun FocusListPreview() {
    OrionTheme {
        Surface {
            ArchiveScreen(
                state = ArchiveUiState(
                    archive = previewArchive.copy(
                        hasDraft = true,
                    ),
                    loading = false,
                ),
                focusTab = true,
                onTab = {},
                onNewSignal = {},
                onAnalysis = {},
                onSignal = {},
                onFocus = {},
                onRetry = {},
            )
        }
    }
}

@Preview
@Composable
private fun EmptyArchivePreview() {
    OrionTheme {
        Surface {
            ArchiveScreen(
                state = ArchiveUiState(
                    loading = false,
                ),
                focusTab = false,
                onTab = {},
                onNewSignal = {},
                onAnalysis = {},
                onSignal = {},
                onFocus = {},
                onRetry = {},
            )
        }
    }
}

@Preview
@Composable
private fun LoadingArchivePreview() {
    OrionTheme {
        Surface {
            ArchiveScreen(
                state = ArchiveUiState(),
                focusTab = false,
                onTab = {},
                onNewSignal = {},
                onAnalysis = {},
                onSignal = {},
                onFocus = {},
                onRetry = {},
            )
        }
    }
}

@Preview
@Composable
private fun ErrorArchivePreview() {
    OrionTheme {
        Surface {
            ArchiveScreen(
                state = ArchiveUiState(
                    loading = false,
                    error = "読込エラー",
                ),
                focusTab = false,
                onTab = {},
                onNewSignal = {},
                onAnalysis = {},
                onSignal = {},
                onFocus = {},
                onRetry = {},
            )
        }
    }
}
