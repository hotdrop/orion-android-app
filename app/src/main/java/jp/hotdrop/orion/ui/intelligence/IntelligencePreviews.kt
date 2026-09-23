package jp.hotdrop.orion.ui.intelligence

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import jp.hotdrop.orion.model.intelligence.RelatedFocus
import jp.hotdrop.orion.ui.intelligence.components.ConfirmationContent
import jp.hotdrop.orion.ui.theme.OrionTheme

private val LONG_TEXT = "LLMを生成器ではなく判定器として使う考え方が面白い。以前の観測とのつながりを考える。\n".repeat(20)

/**
 * 一覧・詳細の読み込み、空、エラー、関連あり・なし、長文を確認する。
 */
class ArchivePreviewStates : PreviewParameterProvider<ArchiveUiState> {
    override val values = sequenceOf(
        ArchiveUiState(),
        ArchiveUiState(
            loading = false,
        ),
        ArchiveUiState(
            loading = false,
            error = "記録を読み込めませんでした。",
        ),
        ArchiveUiState(
            archive = previewArchive,
            loading = false,
        ),
        ArchiveUiState(
            archive = previewArchive.copy(
                hasDraft = true,
                focuses = (1L..3L).map { id ->
                    previewArchive.focuses.first().copy(id = id, analysis = LONG_TEXT)
                },
                links = (1L..3L).map { id ->
                    RelatedFocus(signalId = 1, focusId = id, generatedAt = 1790121600000)
                },
            ),
            loading = false,
        ),
        ArchiveUiState(
            archive = previewArchive.copy(
                links = emptyList(),
            ),
            loading = false,
        ),
    )
}

/**
 * 編集画面の入力、保存中、復元、エラーを確認する。
 */
class EditorPreviewStates : PreviewParameterProvider<RecordEditorState> {
    override val values = sequenceOf(
        RecordEditorState(
            loading = true,
            loaded = false,
        ),
        RecordEditorState(
            loaded = false,
            error = "記録を読み込めませんでした。",
        ),
        RecordEditorState(),
        RecordEditorState(
            error = "本文を入力してください。",
        ),
        RecordEditorState(
            input = RecordInput(
                body = LONG_TEXT,
                keywords = "Small LLM\nRLCD\nJev",
            ),
        ),
        RecordEditorState(
            focus = true,
            input = RecordInput(
                body = LONG_TEXT,
                note = "自分の考え",
                keywords = "RLCD",
            ),
            candidates = listOf("RLCD", "Jev"),
        ),
        RecordEditorState(
            busy = true,
            input = RecordInput(
                body = "保存または削除中",
            ),
        ),
        RecordEditorState(
            input = RecordInput(
                body = "入力は保持されています。",
            ),
            error = "保存または削除に失敗しました。再試行してください。",
        ),
    )
}

/**
 * 分析の各段階と自動保存・確定中・失敗状態を確認する。
 */
class AnalysisPreviewStates : PreviewParameterProvider<AnalysisState> {
    override val values = sequenceOf(
        AnalysisState(),
        AnalysisState(
            loading = false,
        ),
        AnalysisState(
            loading = false,
            error = "分析を開始できませんでした。",
        ),
        AnalysisState(
            draft = previewDraft,
            loading = false,
        ),
        AnalysisState(
            draft = previewDraft.copy(
                stage = 1,
            ),
            loading = false,
        ),
        AnalysisState(
            draft = previewDraft.copy(
                stage = 1,
                analysis = LONG_TEXT,
                note = "復元されたメモ",
                keywords = "Jev\nRLCD",
            ),
            loading = false,
        ),
        AnalysisState(
            draft = previewDraft.copy(
                stage = 1,
                analysis = "保存中の分析",
            ),
            loading = false,
            busy = true,
        ),
        AnalysisState(
            draft = previewDraft.copy(
                stage = 1,
                analysis = "入力中の分析",
            ),
            loading = false,
            savingDraft = true,
        ),
        AnalysisState(
            draft = previewDraft.copy(
                stage = 1,
                analysis = "入力を保持",
            ),
            loading = false,
            error = "下書きの保存に失敗しました。",
        ),
        AnalysisState(
            draft = previewDraft.copy(
                stage = 1,
            ),
            loading = false,
            error = "ChatGPT Analysisを入力してください。",
        ),
    )
}

@Preview(widthDp = 360, heightDp = 800)
@Composable
private fun SignalStatesPreview(
    @PreviewParameter(ArchivePreviewStates::class) state: ArchiveUiState,
) {
    OrionTheme {
        Surface {
            ArchiveScreen(
                state = state,
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

@Preview(widthDp = 360, heightDp = 800)
@Composable
private fun FocusStatesPreview(
    @PreviewParameter(ArchivePreviewStates::class) state: ArchiveUiState,
) {
    OrionTheme {
        Surface {
            ArchiveScreen(
                state = state,
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

@Preview(widthDp = 360, heightDp = 800)
@Composable
private fun SignalDetailStatesPreview(
    @PreviewParameter(ArchivePreviewStates::class) state: ArchiveUiState,
) {
    OrionTheme {
        Surface {
            RecordDetailScreen(
                state = state,
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

@Preview(widthDp = 360, heightDp = 800)
@Composable
private fun FocusDetailStatesPreview(
    @PreviewParameter(ArchivePreviewStates::class) state: ArchiveUiState,
) {
    OrionTheme {
        Surface {
            RecordDetailScreen(
                state = state,
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

@Preview(widthDp = 360, heightDp = 800)
@Composable
private fun EditorStatesPreview(
    @PreviewParameter(EditorPreviewStates::class) state: RecordEditorState,
) {
    OrionTheme {
        Surface {
            RecordEditorScreen(
                state = state,
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

@Preview(widthDp = 360, heightDp = 800)
@Composable
private fun AnalysisStatesPreview(
    @PreviewParameter(AnalysisPreviewStates::class) state: AnalysisState,
) {
    OrionTheme {
        Surface {
            AnalysisScreen(
                state = state,
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

@Preview(widthDp = 360)
@Composable
private fun DeleteRecordConfirmationPreview() {
    OrionTheme {
        Surface {
            ConfirmationContent(
                title = "DELETE RECORD?",
                message = "この記録を端末から削除します。",
                onDismiss = {},
                onConfirm = {},
            )
        }
    }
}

@Preview(widthDp = 360)
@Composable
private fun DiscardChangesConfirmationPreview() {
    OrionTheme {
        Surface {
            ConfirmationContent(
                title = "DISCARD CHANGES?",
                message = "未保存の変更を破棄します。",
                onDismiss = {},
                onConfirm = {},
            )
        }
    }
}

@Preview(widthDp = 360)
@Composable
private fun DiscardAnalysisConfirmationPreview() {
    OrionTheme {
        Surface {
            ConfirmationContent(
                title = "DISCARD ANALYSIS?",
                message = "下書きを破棄します。Signalは残り、分析対象は進みません。",
                onDismiss = {},
                onConfirm = {},
            )
        }
    }
}

@Preview(widthDp = 360, heightDp = 800, fontScale = 1.6f)
@Composable
private fun LargeFontPreview() {
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
