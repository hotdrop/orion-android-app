package jp.hotdrop.orion.ui.archive.components.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.hotdrop.orion.ui.theme.OrionCyan
import jp.hotdrop.orion.ui.theme.OrionCyanMuted
import jp.hotdrop.orion.ui.theme.OrionPanel
import jp.hotdrop.orion.ui.theme.OrionTheme

@Composable
fun KnowledgeArchiveEditorHeader(
    isEditing: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, OrionCyanMuted, CutCornerShape(topStart = 16.dp, bottomEnd = 16.dp))
            .background(OrionPanel.copy(alpha = 0.82f), CutCornerShape(topStart = 16.dp, bottomEnd = 16.dp))
            .padding(16.dp),
    ) {
        Text(
            text = if (isEditing) "ARCHIVE CONTROL // EDIT" else "ARCHIVE CONTROL // NEW",
            color = OrionCyan,
            fontSize = 10.sp,
            letterSpacing = 1.4.sp,
        )
        Text(
            text = if (isEditing) "MODIFY KNOWLEDGE RECORD" else "CAPTURE NEW INTELLIGENCE",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.1.sp,
        )
    }
}

@Preview
@Composable
private fun KnowledgeArchiveEditorHeaderEditingPreview() {
    OrionTheme {
        KnowledgeArchiveEditorHeader(
            isEditing = true,
        )
    }
}

@Preview
@Composable
private fun KnowledgeArchiveEditorHeaderCreatingPreview() {
    OrionTheme {
        KnowledgeArchiveEditorHeader(
            isEditing = false,
        )
    }
}
