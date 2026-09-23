package jp.hotdrop.orion.ui.intelligence.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import jp.hotdrop.orion.model.intelligence.normalizeKeyword
import jp.hotdrop.orion.ui.theme.OrionAmber
import jp.hotdrop.orion.ui.theme.OrionCyan
import jp.hotdrop.orion.ui.theme.OrionCyanMuted
import jp.hotdrop.orion.ui.theme.OrionPanel
import jp.hotdrop.orion.ui.theme.OrionPanelElevated
import jp.hotdrop.orion.ui.theme.OrionTextMuted
import jp.hotdrop.orion.ui.theme.OrionTheme

private val RECORD_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")

/**
 * 記録の時刻を端末のタイムゾーンで、一覧・詳細共通の書式に整える。
 */
fun recordDate(time: Long): String {
    val localTime = Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault())
    return RECORD_DATE_FORMAT.format(localTime)
}

/**
 * Personal Intelligence画面で使う、無効状態を持つ操作ボタン。
 */
@Composable
fun ConsoleAction(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 48.dp),
        shape = CutCornerShape(topStart = 8.dp, bottomEnd = 8.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = OrionCyan,
            disabledContentColor = OrionTextMuted,
        ),
        border = BorderStroke(1.dp, if (enabled) OrionCyan else OrionCyanMuted),
    ) {
        Text(text = label)
    }
}

/**
 * 一覧と関連記録で使用する、本文の抜粋とKeywordを表示するカード。
 */
@Composable
fun RecordPanel(
    label: String,
    body: String,
    keywords: List<String>,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onOpen,
        modifier = modifier.fillMaxWidth(),
        color = OrionPanelElevated,
        shape = CutCornerShape(topStart = 12.dp, bottomEnd = 12.dp),
        border = BorderStroke(1.dp, OrionCyanMuted),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = label, color = OrionCyan, style = MaterialTheme.typography.labelMedium)
            Text(text = body, maxLines = 4, overflow = TextOverflow.Ellipsis)
            KeywordLabels(keywords = keywords)
        }
    }
}

/**
 * 保存された表示名を維持し、幅に応じて折り返してKeywordを並べる。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KeywordLabels(keywords: List<String>) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        keywords.forEach { keyword ->
            Text(
                text = "[ $keyword ]",
                color = OrionCyan,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

/**
 * 状態メッセージと、再試行可能な場合だけ再試行ボタンを表示する。
 */
@Composable
fun ConsoleMessage(
    message: String,
    onRetry: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = message,
            color = if (onRetry != null) OrionAmber else OrionTextMuted
        )
        if (onRetry != null) {
            ConsoleAction(label = "RETRY", onClick = onRetry)
        }
    }
}

/**
 * 見出しと選択可能な全文を表示する。空の本文はダッシュで示す。
 */
@Composable
fun RecordText(label: String, text: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label,
            color = OrionCyan,
            style = MaterialTheme.typography.labelLarge
        )
        SelectionContainer {
            Text(
                text = text.ifBlank { "—" },
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

/**
 * Signal・Focusの本文とKeywordの入力欄。Focusの場合だけメモ欄を表示する。
 */
@Composable
fun RecordFields(
    body: String,
    note: String,
    keywords: String,
    focus: Boolean,
    enabled: Boolean,
    onBody: (String) -> Unit,
    onNote: (String) -> Unit,
    onKeywords: (String) -> Unit,
) {
    OutlinedTextField(
        value = body,
        onValueChange = onBody,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        label = { Text(if (focus) "ChatGPT Analysis *" else "Signal *") },
        minLines = 5,
        maxLines = 14,
    )
    if (focus) {
        OutlinedTextField(
            value = note,
            onValueChange = onNote,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            label = { Text("My Note") },
            minLines = 3,
            maxLines = 8,
        )
    }
    OutlinedTextField(
        value = keywords,
        onValueChange = onKeywords,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        label = { Text("Keywords — 1行に1つ") },
        minLines = 2,
        maxLines = 6,
        supportingText = { Text("Small LLMなど、スペースを含む語も1行で入力できます。") },
    )
}

/**
 * 候補を選択操作として表示する。正規化後に一致する選択済みの語は再選択させない。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KeywordCandidates(
    candidates: List<String>,
    selected: List<String>,
    onSelect: (String) -> Unit,
    enabled: Boolean,
) {
    if (candidates.isNotEmpty()) {
        Text(
            text = "KEYWORD CANDIDATES",
            color = OrionCyan
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            candidates.forEach { candidate ->
                val chosen = selected.any { keyword ->
                    normalizeKeyword(keyword) == normalizeKeyword(candidate)
                }
                FilterChip(
                    selected = chosen,
                    onClick = { onSelect(candidate) },
                    label = { Text(candidate) },
                    enabled = enabled && !chosen,
                )
            }
        }
    }
}

/**
 * 確認ダイアログの内容。Dialogの外側でもPreviewで直接表示できる。
 */
@Composable
fun ConfirmationContent(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    Column(
        modifier = Modifier
            .background(OrionPanel)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(text = title, color = OrionAmber, style = MaterialTheme.typography.titleLarge)
        Text(text = message)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ConsoleAction(label = "CANCEL", onClick = onDismiss)
            ConsoleAction(label = "CONFIRM", onClick = onConfirm)
        }
    }
}

@Preview
@Composable
private fun ComponentsPreview() {
    OrionTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                RecordPanel(
                    label = "SIGNAL 2026/09/23",
                    body = "生成ではなく選択するLLMが気になる",
                    keywords = listOf("Jev", "Small LLM"),
                    onOpen = {},
                )
                KeywordCandidates(
                    candidates = listOf("Jev", "RLCD"),
                    selected = listOf("Jev"),
                    onSelect = {},
                    enabled = true,
                )
                RecordText(label = "MY NOTE", text = "以前のSignalとの関連が見えた。")
                ConsoleAction(label = "SAVING…", onClick = {}, enabled = false)
                ConsoleMessage(message = "保存に失敗しました。", onRetry = {})
            }
        }
    }
}

@Preview
@Composable
private fun FieldsPreview() {
    OrionTheme {
        Surface {
            Column(modifier = Modifier.padding(16.dp)) {
                RecordFields(
                    body = "分析の回答",
                    note = "自分の考え",
                    keywords = "RLHF\nRLCD",
                    focus = true,
                    enabled = true,
                    onBody = {},
                    onNote = {},
                    onKeywords = {},
                )
            }
        }
    }
}

@Preview
@Composable
private fun ConfirmationPreview() {
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
