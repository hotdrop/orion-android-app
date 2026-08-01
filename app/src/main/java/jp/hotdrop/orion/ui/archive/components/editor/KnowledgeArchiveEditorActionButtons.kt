package jp.hotdrop.orion.ui.archive.components.editor

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.hotdrop.orion.ui.theme.OrionCyan
import jp.hotdrop.orion.ui.theme.OrionCyanMuted
import jp.hotdrop.orion.ui.theme.OrionDeepNavy
import jp.hotdrop.orion.ui.theme.OrionError
import jp.hotdrop.orion.ui.theme.OrionPanelElevated
import jp.hotdrop.orion.ui.theme.OrionTextMuted
import jp.hotdrop.orion.ui.theme.OrionTheme

internal const val SaveArchiveEntryButtonTag = "save_archive_entry_button"
internal const val DeleteArchiveEntryButtonTag = "delete_archive_entry_button"

@Composable
fun KnowledgeArchiveEditorSaveButton(
    isSaving: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .testTag(SaveArchiveEntryButtonTag)
            .semantics { contentDescription = "Knowledge Archiveの記録を保存" },
        enabled = enabled,
        shape = CutCornerShape(topStart = 10.dp, bottomEnd = 10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = OrionCyan,
            contentColor = OrionDeepNavy,
            disabledContainerColor = OrionCyanMuted.copy(alpha = 0.35f),
            disabledContentColor = OrionTextMuted,
        ),
    ) {
        if (isSaving) {
            CircularProgressIndicator(
                modifier = Modifier.height(18.dp),
                color = OrionDeepNavy,
                strokeWidth = 2.dp,
            )
        } else {
            Text("[ COMMIT TO ARCHIVE ]", fontWeight = FontWeight.Bold, letterSpacing = 1.1.sp)
        }
    }
}

@Composable
fun KnowledgeArchiveEditorDeleteButton(
    isDeleting: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .testTag(DeleteArchiveEntryButtonTag)
            .semantics { contentDescription = "Knowledge Archiveの記録を削除" },
        enabled = enabled,
        shape = CutCornerShape(topStart = 10.dp, bottomEnd = 10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = OrionError.copy(alpha = 0.16f),
            contentColor = OrionError,
            disabledContainerColor = OrionPanelElevated,
            disabledContentColor = OrionTextMuted,
        ),
    ) {
        Text(
            if (isDeleting) "DELETING..." else "[ DELETE RECORD ]",
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.1.sp,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun KnowledgeArchiveEditorSaveButtonPreview() {
    OrionTheme {
        KnowledgeArchiveEditorSaveButton(
            isSaving = false,
            enabled = true,
            onClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun KnowledgeArchiveEditorDeleteButtonPreview() {
    OrionTheme {
        KnowledgeArchiveEditorDeleteButton(
            isDeleting = false,
            enabled = true,
            onClick = {},
        )
    }
}
