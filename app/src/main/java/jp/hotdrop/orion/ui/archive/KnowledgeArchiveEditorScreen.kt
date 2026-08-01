package jp.hotdrop.orion.ui.archive

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import jp.hotdrop.orion.data.archive.KnowledgeArchiveRepository
import jp.hotdrop.orion.data.archive.KnowledgeArchiveValidationError
import jp.hotdrop.orion.ui.theme.OrionAmber
import jp.hotdrop.orion.ui.theme.OrionCyan
import jp.hotdrop.orion.ui.theme.OrionCyanMuted
import jp.hotdrop.orion.ui.theme.OrionDeepNavy
import jp.hotdrop.orion.ui.theme.OrionError
import jp.hotdrop.orion.ui.theme.OrionPanel
import jp.hotdrop.orion.ui.theme.OrionPanelElevated
import jp.hotdrop.orion.ui.theme.OrionTextMuted
import jp.hotdrop.orion.ui.theme.OrionTheme

internal const val ArchiveTitleInputTag = "archive_title_input"
internal const val ArchiveUrlInputTag = "archive_url_input"
internal const val ArchiveMemoInputTag = "archive_memo_input"
internal const val SaveArchiveEntryButtonTag = "save_archive_entry_button"
internal const val DeleteArchiveEntryButtonTag = "delete_archive_entry_button"

@Composable
fun KnowledgeArchiveEditorRoute(
    repository: KnowledgeArchiveRepository,
    entryId: Long?,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: KnowledgeArchiveEditorViewModel = viewModel(
        key = "knowledge_archive_editor_${entryId ?: "new"}",
        factory = KnowledgeArchiveEditorViewModel.factory(repository, entryId),
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                KnowledgeArchiveEditorEvent.Close -> onClose()
            }
        }
    }

    BackHandler(onBack = viewModel::requestBack)

    KnowledgeArchiveEditorScreen(
        uiState = uiState,
        onTitleChanged = viewModel::onTitleChanged,
        onUrlChanged = viewModel::onUrlChanged,
        onMemoChanged = viewModel::onMemoChanged,
        onSave = viewModel::save,
        onRequestDelete = viewModel::requestDelete,
        onDismissDiscard = viewModel::dismissDiscardConfirmation,
        onConfirmDiscard = viewModel::discardAndClose,
        onDismissDelete = viewModel::dismissDeleteConfirmation,
        onConfirmDelete = viewModel::confirmDelete,
        modifier = modifier,
    )
}

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
        EditorModuleHeader(isEditing = uiState.entryId != null)
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = OrionCyan,
                strokeWidth = 2.dp,
            )
        } else {
            EditorFieldsPanel(
                uiState = uiState,
                onTitleChanged = onTitleChanged,
                onUrlChanged = onUrlChanged,
                onMemoChanged = onMemoChanged,
            )
            EditorFeedbackPanel(uiState)
            Button(
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .testTag(SaveArchiveEntryButtonTag)
                    .semantics { contentDescription = "Knowledge Archiveの記録を保存" },
                enabled = uiState.canSave,
                shape = CutCornerShape(topStart = 10.dp, bottomEnd = 10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OrionCyan,
                    contentColor = OrionDeepNavy,
                    disabledContainerColor = OrionCyanMuted.copy(alpha = 0.35f),
                    disabledContentColor = OrionTextMuted,
                ),
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(18.dp),
                        color = OrionDeepNavy,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("[ COMMIT TO ARCHIVE ]", fontWeight = FontWeight.Bold, letterSpacing = 1.1.sp)
                }
            }
            if (uiState.entryId != null) {
                Button(
                    onClick = onRequestDelete,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .testTag(DeleteArchiveEntryButtonTag)
                        .semantics { contentDescription = "Knowledge Archiveの記録を削除" },
                    enabled = !uiState.isSaving && !uiState.isDeleting,
                    shape = CutCornerShape(topStart = 10.dp, bottomEnd = 10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OrionError.copy(alpha = 0.16f),
                        contentColor = OrionError,
                        disabledContainerColor = OrionPanelElevated,
                        disabledContentColor = OrionTextMuted,
                    ),
                ) {
                    Text(
                        if (uiState.isDeleting) "DELETING..." else "[ DELETE RECORD ]",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.1.sp,
                    )
                }
            }
        }
    }

    if (uiState.showDiscardConfirmation) {
        OrionConfirmationDialog(
            title = "DISCARD CHANGES?",
            message = "入力中の変更は保存されません。",
            confirmLabel = "DISCARD",
            onDismiss = onDismissDiscard,
            onConfirm = onConfirmDiscard,
        )
    }
    if (uiState.showDeleteConfirmation) {
        OrionConfirmationDialog(
            title = "DELETE RECORD?",
            message = "この記録は端末から完全に削除されます。",
            confirmLabel = "DELETE",
            onDismiss = onDismissDelete,
            onConfirm = onConfirmDelete,
            destructive = true,
        )
    }
}

@Composable
private fun EditorModuleHeader(isEditing: Boolean) {
    Column(
        modifier = Modifier
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

@Composable
private fun EditorFieldsPanel(
    uiState: KnowledgeArchiveEditorUiState,
    onTitleChanged: (String) -> Unit,
    onUrlChanged: (String) -> Unit,
    onMemoChanged: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, OrionCyanMuted, CutCornerShape(topStart = 16.dp, bottomEnd = 16.dp))
            .background(OrionPanel.copy(alpha = 0.72f), CutCornerShape(topStart = 16.dp, bottomEnd = 16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        OutlinedTextField(
            value = uiState.title,
            onValueChange = onTitleChanged,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(ArchiveTitleInputTag)
                .semantics { contentDescription = "記事タイトル" },
            enabled = !uiState.isSaving && !uiState.isDeleting,
            label = { Text("TITLE // REQUIRED") },
            isError = uiState.validationError == KnowledgeArchiveValidationError.TitleRequired,
            supportingText = if (uiState.validationError == KnowledgeArchiveValidationError.TitleRequired) {
                { Text("タイトルを入力してください。") }
            } else {
                null
            },
            singleLine = true,
            shape = CutCornerShape(topStart = 10.dp, bottomEnd = 10.dp),
            colors = archiveTextFieldColors(),
        )
        OutlinedTextField(
            value = uiState.url,
            onValueChange = onUrlChanged,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(ArchiveUrlInputTag)
                .semantics { contentDescription = "記事URL" },
            enabled = !uiState.isSaving && !uiState.isDeleting,
            label = { Text("URL // HTTP OR HTTPS") },
            placeholder = { Text("https://example.com/article") },
            isError = uiState.validationError == KnowledgeArchiveValidationError.UrlRequired ||
                uiState.validationError == KnowledgeArchiveValidationError.UrlInvalid,
            supportingText = when (uiState.validationError) {
                KnowledgeArchiveValidationError.UrlRequired -> ({ Text("URLを入力してください。") })
                KnowledgeArchiveValidationError.UrlInvalid -> ({ Text("httpまたはhttpsのURLを入力してください。") })
                else -> null
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            shape = CutCornerShape(topStart = 10.dp, bottomEnd = 10.dp),
            colors = archiveTextFieldColors(),
        )
        OutlinedTextField(
            value = uiState.memo,
            onValueChange = onMemoChanged,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 150.dp)
                .testTag(ArchiveMemoInputTag)
                .semantics { contentDescription = "記事についてのメモ" },
            enabled = !uiState.isSaving && !uiState.isDeleting,
            label = { Text("MEMO // OPTIONAL") },
            placeholder = { Text("気になった点、試したいこと、あとで深掘りする内容…") },
            minLines = 5,
            shape = CutCornerShape(topStart = 10.dp, bottomEnd = 10.dp),
            colors = archiveTextFieldColors(),
        )
    }
}

@Composable
private fun archiveTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = OrionCyan,
    unfocusedBorderColor = OrionCyanMuted,
    errorBorderColor = OrionError,
    focusedLabelColor = OrionCyan,
    unfocusedLabelColor = OrionTextMuted,
    cursorColor = OrionCyan,
    focusedContainerColor = OrionPanelElevated.copy(alpha = 0.45f),
    unfocusedContainerColor = OrionPanelElevated.copy(alpha = 0.25f),
    disabledContainerColor = OrionPanelElevated.copy(alpha = 0.2f),
)

@Composable
private fun EditorFeedbackPanel(uiState: KnowledgeArchiveEditorUiState) {
    val (message, color) = when (uiState.feedback) {
        KnowledgeArchiveEditorFeedback.LoadFailed ->
            "LOCAL RECORD // READ ERROR\n記録を読み込めませんでした。" to OrionError
        KnowledgeArchiveEditorFeedback.SaveFailed ->
            "LOCAL WRITE // ERROR\n保存できませんでした。入力内容を維持したまま再試行できます。" to OrionError
        KnowledgeArchiveEditorFeedback.DeleteFailed ->
            "LOCAL DELETE // ERROR\n削除できませんでした。再試行してください。" to OrionError
        KnowledgeArchiveEditorFeedback.None -> when {
            uiState.isSaving -> "LOCAL WRITE // PROCESSING" to OrionCyan
            uiState.isDeleting -> "LOCAL DELETE // PROCESSING" to OrionAmber
            uiState.isDirty -> "DRAFT BUFFER // UNSAVED CHANGES" to OrionAmber
            else -> "LOCAL RECORD // SYNCHRONIZED" to OrionCyan
        }
    }
    Text(
        text = message,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, color.copy(alpha = 0.75f))
            .background(OrionPanelElevated.copy(alpha = 0.45f))
            .padding(14.dp),
        color = color,
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
private fun OrionConfirmationDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    destructive: Boolean = false,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, color = if (destructive) OrionError else OrionAmber) },
        text = { Text(message, color = MaterialTheme.colorScheme.onSurface) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("[ $confirmLabel ]", color = if (destructive) OrionError else OrionAmber)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("[ CANCEL ]", color = OrionCyan)
            }
        },
        containerColor = OrionPanel,
        shape = CutCornerShape(topStart = 18.dp, bottomEnd = 18.dp),
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF030812, widthDp = 393, heightDp = 700)
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

@Preview(showBackground = true, backgroundColor = 0xFF030812, widthDp = 393, heightDp = 700)
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
