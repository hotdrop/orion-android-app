package jp.hotdrop.orion.ui.archive.components.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import jp.hotdrop.orion.ui.theme.OrionAmber
import jp.hotdrop.orion.ui.theme.OrionCyan
import jp.hotdrop.orion.ui.theme.OrionError
import jp.hotdrop.orion.ui.theme.OrionPanel
import jp.hotdrop.orion.ui.theme.OrionTheme

@Composable
fun KnowledgeArchiveEditorConfirmationDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    destructive: Boolean = false,
) {
    Dialog(onDismissRequest = onDismiss) {
        KnowledgeArchiveEditorConfirmationDialogContent(
            title = title,
            message = message,
            confirmLabel = confirmLabel,
            onDismiss = onDismiss,
            onConfirm = onConfirm,
            destructive = destructive,
        )
    }
}

@Composable
private fun KnowledgeArchiveEditorConfirmationDialogContent(
    title: String,
    message: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    destructive: Boolean,
    modifier: Modifier = Modifier,
) {
    val actionColor = if (destructive) OrionError else OrionAmber

    Surface(
        modifier = modifier.widthIn(min = 280.dp, max = 560.dp),
        color = OrionPanel,
        shape = CutCornerShape(topStart = 18.dp, bottomEnd = 18.dp),
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = title,
                color = actionColor,
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = message,
                modifier = Modifier.padding(top = 16.dp),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onDismiss) {
                    Text("[ CANCEL ]", color = OrionCyan)
                }
                TextButton(onClick = onConfirm) {
                    Text("[ $confirmLabel ]", color = actionColor)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun KnowledgeArchiveEditorConfirmationDialogPreview() {
    OrionTheme {
        KnowledgeArchiveEditorConfirmationDialogContent(
            title = "DISCARD CHANGES?",
            message = "入力中の変更は保存されません。",
            confirmLabel = "DISCARD",
            onDismiss = {},
            onConfirm = {},
            destructive = false,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun KnowledgeArchiveEditorDestructiveConfirmationDialogPreview() {
    OrionTheme {
        KnowledgeArchiveEditorConfirmationDialogContent(
            title = "DELETE RECORD?",
            message = "この記録は端末から完全に削除されます。",
            confirmLabel = "DELETE",
            onDismiss = {},
            onConfirm = {},
            destructive = true,
        )
    }
}
