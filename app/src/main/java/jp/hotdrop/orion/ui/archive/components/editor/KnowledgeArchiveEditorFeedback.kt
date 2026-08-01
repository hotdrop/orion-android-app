package jp.hotdrop.orion.ui.archive.components.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import jp.hotdrop.orion.model.KnowledgeArchiveEditorFeedback
import jp.hotdrop.orion.ui.theme.OrionAmber
import jp.hotdrop.orion.ui.theme.OrionCyan
import jp.hotdrop.orion.ui.theme.OrionError
import jp.hotdrop.orion.ui.theme.OrionPanelElevated
import jp.hotdrop.orion.ui.theme.OrionTheme

@Composable
fun KnowledgeArchiveEditorFeedback(
    feedback: KnowledgeArchiveEditorFeedback,
    isSaving: Boolean,
    isDeleting: Boolean,
    isDirty: Boolean,
    modifier: Modifier = Modifier,
) {
    val (message, color) = editorFeedbackContent(
        feedback = feedback,
        isSaving = isSaving,
        isDeleting = isDeleting,
        isDirty = isDirty,
    )
    Text(
        text = message,
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, color.copy(alpha = 0.75f))
            .background(OrionPanelElevated.copy(alpha = 0.45f))
            .padding(14.dp),
        color = color,
        style = MaterialTheme.typography.bodyMedium,
    )
}

private fun editorFeedbackContent(
    feedback: KnowledgeArchiveEditorFeedback,
    isSaving: Boolean,
    isDeleting: Boolean,
    isDirty: Boolean,
): Pair<String, Color> = when (feedback) {
    KnowledgeArchiveEditorFeedback.LoadFailed ->
        "LOCAL RECORD // READ ERROR\n記録を読み込めませんでした。" to OrionError
    KnowledgeArchiveEditorFeedback.SaveFailed ->
        "LOCAL WRITE // ERROR\n保存できませんでした。入力内容を維持したまま再試行できます。" to OrionError
    KnowledgeArchiveEditorFeedback.DeleteFailed ->
        "LOCAL DELETE // ERROR\n削除できませんでした。再試行してください。" to OrionError
    KnowledgeArchiveEditorFeedback.None -> when {
        isSaving -> "LOCAL WRITE // PROCESSING" to OrionCyan
        isDeleting -> "LOCAL DELETE // PROCESSING" to OrionAmber
        isDirty -> "DRAFT BUFFER // UNSAVED CHANGES" to OrionAmber
        else -> "LOCAL RECORD // SYNCHRONIZED" to OrionCyan
    }
}

@Preview
@Composable
private fun KnowledgeArchiveEditorFeedbackPreview() {
    OrionTheme {
        KnowledgeArchiveEditorFeedback(
            feedback = KnowledgeArchiveEditorFeedback.None,
            isSaving = false,
            isDeleting = false,
            isDirty = true,
        )
    }
}

@Preview
@Composable
private fun KnowledgeArchiveEditorFeedbackErrorPreview() {
    OrionTheme {
        KnowledgeArchiveEditorFeedback(
            feedback = KnowledgeArchiveEditorFeedback.SaveFailed,
            isSaving = false,
            isDeleting = false,
            isDirty = true,
        )
    }
}
