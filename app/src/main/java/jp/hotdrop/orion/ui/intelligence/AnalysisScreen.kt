package jp.hotdrop.orion.ui.intelligence

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import jp.hotdrop.orion.model.intelligence.AnalysisDraft
import jp.hotdrop.orion.model.intelligence.appendKeyword
import jp.hotdrop.orion.model.intelligence.parseKeywords
import jp.hotdrop.orion.ui.intelligence.components.ConfirmationContent
import jp.hotdrop.orion.ui.intelligence.components.ConsoleAction
import jp.hotdrop.orion.ui.intelligence.components.ConsoleMessage
import jp.hotdrop.orion.ui.intelligence.components.KeywordCandidates
import jp.hotdrop.orion.ui.intelligence.components.RecordFields
import jp.hotdrop.orion.ui.intelligence.components.RecordText
import jp.hotdrop.orion.ui.intelligence.components.recordDate
import jp.hotdrop.orion.ui.theme.OrionCyan
import jp.hotdrop.orion.ui.theme.OrionTextMuted
import jp.hotdrop.orion.ui.theme.OrionTheme

/**
 * 分析プロンプトと結果入力を表示する。保存・画面遷移は呼び出し元へ通知する。
 */
@Composable
fun AnalysisScreen(
    state: AnalysisState,
    copied: Boolean,
    onCopy: (String) -> Unit,
    onStage: (Int) -> Unit,
    onChange: (String, String, String) -> Unit,
    onSuggest: () -> Unit,
    onSave: () -> Unit,
    onRetry: () -> Unit,
    onRetryDraft: () -> Unit,
    onConfirmDiscard: (Boolean) -> Unit,
    onDiscard: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = key(state.draft?.stage) { rememberScrollState() }
    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (state.loading) {
            ConsoleMessage(message = "LOADING…")
        }
        state.error?.let { error ->
            ConsoleMessage(
                message = error,
                onRetry = if (state.draft == null) onRetry else onRetryDraft,
            )
        }
        val draft = state.draft
        if (!state.loading && draft == null && state.error == null) {
            ConsoleMessage(message = "新しいSignalはありません。Signalを追加すると分析を開始できます。")
        }
        if (draft != null) {
            Text(text = "FOCUS ANALYSIS ${recordDate(draft.startedAt)}", color = OrionCyan)
            Text(
                text = "対象 ${draft.signalCount}件 · 分析開始時の内容を固定しています。",
                color = OrionTextMuted,
            )
            val saveStatus = when {
                state.savingDraft -> "下書きを保存中…"
                state.error != null -> "下書きの保存状態を確認してください。"
                else -> "下書きは端末に保存されています。"
            }
            Text(text = saveStatus, color = OrionTextMuted)

            if (draft.stage == 0) {
                ConsoleAction(
                    label = if (copied) "COPIED" else "COPY PROMPT",
                    onClick = { onCopy(draft.prompt) },
                    enabled = !state.busy,
                    modifier = Modifier.fillMaxWidth(),
                )
                ConsoleAction(
                    label = "ENTER RESULT",
                    onClick = { onStage(1) },
                    enabled = !state.busy,
                    modifier = Modifier.fillMaxWidth(),
                )
                RecordText(label = "PROMPT", text = draft.prompt)
            } else {
                ConsoleAction(
                    label = "VIEW PROMPT",
                    onClick = { onStage(0) },
                    enabled = !state.busy,
                )
                RecordFields(
                    body = draft.analysis,
                    note = draft.note,
                    keywords = draft.keywords,
                    focus = true,
                    enabled = !state.busy,
                    onBody = { onChange(it, draft.note, draft.keywords) },
                    onNote = { onChange(draft.analysis, it, draft.keywords) },
                    onKeywords = { onChange(draft.analysis, draft.note, it) },
                )
                ConsoleAction(
                    label = "SUGGEST KEYWORDS",
                    onClick = onSuggest,
                    enabled = !state.busy,
                )
                KeywordCandidates(
                    candidates = state.candidates,
                    selected = parseKeywords(draft.keywords).map { it.display },
                    onSelect = { keyword ->
                        onChange(
                            draft.analysis,
                            draft.note,
                            appendKeyword(draft.keywords, keyword),
                        )
                    },
                    enabled = !state.busy,
                )
                ConsoleAction(
                    label = if (state.busy) "SAVING…" else "SAVE FOCUS",
                    onClick = onSave,
                    enabled = !state.busy,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            ConsoleAction(
                label = "DISCARD ANALYSIS",
                onClick = { onConfirmDiscard(true) },
                enabled = !state.busy,
            )
        }
    }
    if (state.confirmation) {
        Dialog(onDismissRequest = { onConfirmDiscard(false) }) {
            ConfirmationContent(
                title = "DISCARD ANALYSIS?",
                message = "分析下書きを破棄します。Signalは残り、次回の分析対象も進みません。",
                onDismiss = { onConfirmDiscard(false) },
                onConfirm = onDiscard,
            )
        }
    }
}


internal val previewDraft = AnalysisDraft(
    token = "preview",
    startedAt = 1790121600000,
    upperSignalId = 1,
    signalCount = 1,
    prompt = "私の関心や思考の方向性を分析してください。\n\nSignal 1\n生成ではなく選択するLLMが気になる\nKeywords: Jev / RLCD",
)

@Preview
@Composable
private fun PromptPreview() {
    OrionTheme {
        Surface {
            AnalysisScreen(
                state = AnalysisState(
                    draft = previewDraft,
                    loading = false,
                ),
                copied = false,
                onCopy = {},
                onStage = {},
                onChange = { _, _, _ -> },
                onSuggest = {},
                onSave = {},
                onRetry = {},
                onRetryDraft = {},
                onConfirmDiscard = {},
                onDiscard = {},
            )
        }
    }
}

@Preview
@Composable
private fun ResultPreview() {
    OrionTheme {
        Surface {
            AnalysisScreen(
                state = AnalysisState(
                    draft = previewDraft.copy(
                        stage = 1,
                        analysis = "学習と選択に共通の関心があります。",
                        note = "意外なつながりだった。",
                    ),
                    loading = false,
                    candidates = listOf("Jev", "RLCD"),
                ),
                copied = false,
                onCopy = {},
                onStage = {},
                onChange = { _, _, _ -> },
                onSuggest = {},
                onSave = {},
                onRetry = {},
                onRetryDraft = {},
                onConfirmDiscard = {},
                onDiscard = {},
            )
        }
    }
}

@Preview
@Composable
private fun NoSignalsPreview() {
    OrionTheme {
        Surface {
            AnalysisScreen(
                state = AnalysisState(
                    loading = false,
                ),
                copied = false,
                onCopy = {},
                onStage = {},
                onChange = { _, _, _ -> },
                onSuggest = {},
                onSave = {},
                onRetry = {},
                onRetryDraft = {},
                onConfirmDiscard = {},
                onDiscard = {},
            )
        }
    }
}

@Preview
@Composable
private fun AnalysisLoadingPreview() {
    OrionTheme {
        Surface {
            AnalysisScreen(
                state = AnalysisState(),
                copied = false,
                onCopy = {},
                onStage = {},
                onChange = { _, _, _ -> },
                onSuggest = {},
                onSave = {},
                onRetry = {},
                onRetryDraft = {},
                onConfirmDiscard = {},
                onDiscard = {},
            )
        }
    }
}

@Preview
@Composable
private fun AnalysisFailurePreview() {
    OrionTheme {
        Surface {
            AnalysisScreen(
                state = AnalysisState(
                    draft = previewDraft.copy(
                        stage = 1,
                    ),
                    loading = false,
                    error = "下書きを保存できませんでした。",
                ),
                copied = false,
                onCopy = {},
                onStage = {},
                onChange = { _, _, _ -> },
                onSuggest = {},
                onSave = {},
                onRetry = {},
                onRetryDraft = {},
                onConfirmDiscard = {},
                onDiscard = {},
            )
        }
    }
}
