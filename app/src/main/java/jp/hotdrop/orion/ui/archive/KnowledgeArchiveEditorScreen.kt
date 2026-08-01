package jp.hotdrop.orion.ui.archive

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import jp.hotdrop.orion.model.KnowledgeArchiveValidationError
import jp.hotdrop.orion.ui.archive.components.editor.KnowledgeArchiveEditorConfirmationDialog
import jp.hotdrop.orion.ui.archive.components.editor.KnowledgeArchiveEditorDeleteButton
import jp.hotdrop.orion.ui.archive.components.editor.KnowledgeArchiveEditorFeedback
import jp.hotdrop.orion.ui.archive.components.editor.KnowledgeArchiveEditorFields
import jp.hotdrop.orion.ui.archive.components.editor.KnowledgeArchiveEditorHeader
import jp.hotdrop.orion.ui.archive.components.editor.KnowledgeArchiveEditorLoadingIndicator
import jp.hotdrop.orion.ui.archive.components.editor.KnowledgeArchiveEditorSaveButton
import jp.hotdrop.orion.ui.archive.uistate.KnowledgeArchiveEditorUiState
import jp.hotdrop.orion.ui.theme.OrionDeepNavy
import jp.hotdrop.orion.ui.theme.OrionTheme

@Composable
fun KnowledgeArchiveEditorScreen(
    uiState: KnowledgeArchiveEditorUiState,
    onTitleChanged: (String) -> Unit,
    onUrlChanged: (String) -> Unit,
    onMemoChanged: (String) -> Unit,
    onSave: () -> Unit,
    onRequestDelete: () -> Unit,
    onDismissDiscard: () -> Unit,
    onConfirmDiscard: () -> Unit,
    onDismissDelete: () -> Unit,
    onConfirmDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OrionDeepNavy)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        KnowledgeArchiveEditorHeader(isEditing = uiState.entryId != null)
        if (uiState.isLoading) {
            KnowledgeArchiveEditorLoadingIndicator()
        } else {
            KnowledgeArchiveEditorFields(
                title = uiState.title,
                url = uiState.url,
                memo = uiState.memo,
                validationError = uiState.validationError,
                enabled = !uiState.isSaving && !uiState.isDeleting,
                onTitleChanged = onTitleChanged,
                onUrlChanged = onUrlChanged,
                onMemoChanged = onMemoChanged,
            )
            KnowledgeArchiveEditorFeedback(
                feedback = uiState.feedback,
                isSaving = uiState.isSaving,
                isDeleting = uiState.isDeleting,
                isDirty = uiState.isDirty,
            )
            KnowledgeArchiveEditorSaveButton(
                isSaving = uiState.isSaving,
                enabled = uiState.canSave,
                onClick = onSave,
            )
            if (uiState.entryId != null) {
                KnowledgeArchiveEditorDeleteButton(
                    isDeleting = uiState.isDeleting,
                    enabled = !uiState.isSaving && !uiState.isDeleting,
                    onClick = onRequestDelete,
                )
            }
        }
    }

    if (uiState.showDiscardConfirmation) {
        KnowledgeArchiveEditorConfirmationDialog(
            title = "DISCARD CHANGES?",
            message = "入力中の変更は保存されません。",
            confirmLabel = "DISCARD",
            onDismiss = onDismissDiscard,
            onConfirm = onConfirmDiscard,
        )
    }
    if (uiState.showDeleteConfirmation) {
        KnowledgeArchiveEditorConfirmationDialog(
            title = "DELETE RECORD?",
            message = "この記録は端末から完全に削除されます。",
            confirmLabel = "DELETE",
            onDismiss = onDismissDelete,
            onConfirm = onConfirmDelete,
            destructive = true,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun KnowledgeArchiveNewEditorPreview() {
    OrionTheme {
        KnowledgeArchiveEditorScreen(
            uiState = KnowledgeArchiveEditorUiState(
                title = "Composeの状態管理",
                url = "https://developer.android.com/develop/ui/compose/state",
                memo = "状態ホイスティングの例をあとで試す。",
            ),
            onTitleChanged = {},
            onUrlChanged = {},
            onMemoChanged = {},
            onSave = {},
            onRequestDelete = {},
            onDismissDiscard = {},
            onConfirmDiscard = {},
            onDismissDelete = {},
            onConfirmDelete = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun KnowledgeArchiveEditorErrorPreview() {
    OrionTheme {
        KnowledgeArchiveEditorScreen(
            uiState = KnowledgeArchiveEditorUiState(
                url = "example.com",
                validationError = KnowledgeArchiveValidationError.TitleRequired,
            ),
            onTitleChanged = {},
            onUrlChanged = {},
            onMemoChanged = {},
            onSave = {},
            onRequestDelete = {},
            onDismissDiscard = {},
            onConfirmDiscard = {},
            onDismissDelete = {},
            onConfirmDelete = {},
        )
    }
}
