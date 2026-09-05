package jp.hotdrop.orion.ui.incoming

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import jp.hotdrop.orion.ui.theme.OrionCyan
import jp.hotdrop.orion.ui.theme.OrionTheme

@Composable
fun IncomingMemoScreen(
    state: IncomingMemoUiState,
    onMemoChanged: (String) -> Unit,
    onSave: () -> Unit,
    onRetry: () -> Unit,
    onDismissDiscard: () -> Unit,
    onConfirmDiscard: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("PERSONAL NOTE", color = OrionCyan, style = MaterialTheme.typography.labelLarge)
        if (state.isLoading) {
            CircularProgressIndicator(color = OrionCyan)
        } else {
            Text(state.title, style = MaterialTheme.typography.titleLarge)
            if (state.canSave) {
                OutlinedTextField(
                    value = state.memo, onValueChange = onMemoChanged,
                    label = { Text("概要メモ") }, minLines = 6,
                    enabled = !state.isSaving, modifier = Modifier.fillMaxWidth(),
                    shape = CutCornerShape(topStart = 12.dp, bottomEnd = 12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrionCyan,
                        unfocusedBorderColor = jp.hotdrop.orion.ui.theme.OrionCyanMuted,
                        cursorColor = OrionCyan,
                    ),
                )
                TextButton(onClick = onSave, enabled = !state.isSaving, modifier = Modifier.fillMaxWidth()) {
                    Text(if (state.isSaving) "SAVING…" else "SAVE", color = OrionCyan)
                }
            }
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            if (!state.canSave) TextButton(onClick = onRetry) { Text("RETRY") }
        }
    }
    if (state.showDiscard) {
        AlertDialog(
            onDismissRequest = onDismissDiscard,
            title = { Text("変更を破棄しますか？") },
            text = { Text("未保存の概要メモは失われます。") },
            confirmButton = { TextButton(onClick = onConfirmDiscard) { Text("破棄して戻る") } },
            dismissButton = { TextButton(onClick = onDismissDiscard) { Text("編集を続ける") } },
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, fontScale = 1.6f)
@Composable
private fun IncomingMemoPreview() {
    OrionTheme {
        IncomingMemoScreen(
            state = IncomingMemoUiState(title = "Jetpack Composeのパフォーマンス調査", memo = "再コンポーズの測定方法と改善の手順。\n次の実装時に参照する。", isLoading = false, canSave = true),
            onMemoChanged = {}, onSave = {}, onRetry = {}, onDismissDiscard = {}, onConfirmDiscard = {},
        )
    }
}
