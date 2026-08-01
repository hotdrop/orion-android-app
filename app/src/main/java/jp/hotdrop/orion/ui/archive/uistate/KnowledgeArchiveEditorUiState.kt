package jp.hotdrop.orion.ui.archive.uistate

import jp.hotdrop.orion.data.normalized
import jp.hotdrop.orion.model.KnowledgeArchiveDraft
import jp.hotdrop.orion.model.KnowledgeArchiveEditorFeedback
import jp.hotdrop.orion.model.KnowledgeArchiveValidationError

data class KnowledgeArchiveEditorUiState(
    val entryId: Long? = null,
    val title: String = "",
    val url: String = "",
    val memo: String = "",
    val originalDraft: KnowledgeArchiveDraft = KnowledgeArchiveDraft("", "", ""),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val validationError: KnowledgeArchiveValidationError? = null,
    val feedback: KnowledgeArchiveEditorFeedback = KnowledgeArchiveEditorFeedback.None,
    val showDiscardConfirmation: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
) {
    val draft: KnowledgeArchiveDraft
        get() = KnowledgeArchiveDraft(title = title, url = url, memo = memo)

    val isDirty: Boolean
        get() = draft.normalized() != originalDraft.normalized()

    val canSave: Boolean
        get() = !isLoading && !isSaving && !isDeleting && isDirty
}