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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import jp.hotdrop.orion.model.intelligence.appendKeyword
import jp.hotdrop.orion.model.intelligence.parseKeywords
import jp.hotdrop.orion.ui.intelligence.components.ConfirmationContent
import jp.hotdrop.orion.ui.intelligence.components.ConsoleAction
import jp.hotdrop.orion.ui.intelligence.components.ConsoleMessage
import jp.hotdrop.orion.ui.intelligence.components.KeywordCandidates
import jp.hotdrop.orion.ui.intelligence.components.RecordFields
import jp.hotdrop.orion.ui.theme.OrionTheme

/**
 * Signal・Focusの編集欄と、削除・未保存入力の破棄確認を表示する。
 */
@Composable
fun RecordEditorScreen(
    state: RecordEditorState,
    canDelete: Boolean,
    onChange: (RecordInput) -> Unit,
    onSave: () -> Unit,
    onSuggest: () -> Unit,
    onRetry: () -> Unit,
    onConfirmation: (String?) -> Unit,
    onDelete: () -> Unit,
    onDiscard: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (state.loading) {
            ConsoleMessage(message = "LOADING…")
        }
        state.error?.let { error ->
            ConsoleMessage(
                message = error,
                onRetry = if (!state.loaded) onRetry else null,
            )
        }
        if (!state.loading && state.loaded) {
            val input = state.input
            RecordFields(
                body = input.body,
                note = input.note,
                keywords = input.keywords,
                focus = state.focus,
                enabled = !state.busy,
                onBody = { onChange(input.copy(body = it)) },
                onNote = { onChange(input.copy(note = it)) },
                onKeywords = { onChange(input.copy(keywords = it)) },
            )
            if (state.focus) {
                ConsoleAction(
                    label = "SUGGEST KEYWORDS",
                    onClick = onSuggest,
                    enabled = !state.busy,
                )
                KeywordCandidates(
                    candidates = state.candidates,
                    selected = parseKeywords(input.keywords).map { it.display },
                    onSelect = { keyword ->
                        onChange(input.copy(keywords = appendKeyword(input.keywords, keyword)))
                    },
                    enabled = !state.busy,
                )
            }
            ConsoleAction(
                label = if (state.busy) "WORKING…" else "SAVE",
                onClick = onSave,
                enabled = !state.busy,
                modifier = Modifier.fillMaxWidth(),
            )
            if (canDelete) {
                ConsoleAction(
                    label = "DELETE",
                    onClick = { onConfirmation("delete") },
                    enabled = !state.busy,
                )
            }
        }
    }
    state.confirmation?.let { confirmation ->
        val isDelete = confirmation == "delete"
        Dialog(onDismissRequest = { onConfirmation(null) }) {
            ConfirmationContent(
                title = if (isDelete) "DELETE RECORD?" else "DISCARD CHANGES?",
                message = if (isDelete) {
                    "この記録を端末から削除します。"
                } else {
                    "未保存の変更を破棄します。"
                },
                onDismiss = { onConfirmation(null) },
                onConfirm = if (isDelete) onDelete else onDiscard,
            )
        }
    }
}


@Preview
@Composable
private fun SignalEditorPreview() {
    OrionTheme {
        Surface {
            RecordEditorScreen(
                state = RecordEditorState(
                    input = RecordInput(
                        body = "気になること",
                        keywords = "RLCD",
                    ),
                ),
                canDelete = true,
                onChange = {},
                onSave = {},
                onSuggest = {},
                onRetry = {},
                onConfirmation = {},
                onDelete = {},
                onDiscard = {},
            )
        }
    }
}

@Preview
@Composable
private fun FocusEditorPreview() {
    OrionTheme {
        Surface {
            RecordEditorScreen(
                state = RecordEditorState(
                    focus = true,
                    input = RecordInput(
                        body = "分析結果",
                        note = "自分の考え",
                        keywords = "RLCD",
                    ),
                    candidates = listOf("Jev", "RLCD"),
                ),
                canDelete = true,
                onChange = {},
                onSave = {},
                onSuggest = {},
                onRetry = {},
                onConfirmation = {},
                onDelete = {},
                onDiscard = {},
            )
        }
    }
}

@Preview
@Composable
private fun EditorLoadingPreview() {
    OrionTheme {
        Surface {
            RecordEditorScreen(
                state = RecordEditorState(
                    loading = true,
                    loaded = false,
                ),
                canDelete = false,
                onChange = {},
                onSave = {},
                onSuggest = {},
                onRetry = {},
                onConfirmation = {},
                onDelete = {},
                onDiscard = {},
            )
        }
    }
}

@Preview
@Composable
private fun EditorErrorPreview() {
    OrionTheme {
        Surface {
            RecordEditorScreen(
                state = RecordEditorState(
                    error = "本文を入力してください。",
                ),
                canDelete = false,
                onChange = {},
                onSave = {},
                onSuggest = {},
                onRetry = {},
                onConfirmation = {},
                onDelete = {},
                onDiscard = {},
            )
        }
    }
}

@Preview
@Composable
private fun EditorSavingPreview() {
    OrionTheme {
        Surface {
            RecordEditorScreen(
                state = RecordEditorState(
                    busy = true,
                    input = RecordInput(
                        body = "記録中",
                    ),
                ),
                canDelete = true,
                onChange = {},
                onSave = {},
                onSuggest = {},
                onRetry = {},
                onConfirmation = {},
                onDelete = {},
                onDiscard = {},
            )
        }
    }
}
