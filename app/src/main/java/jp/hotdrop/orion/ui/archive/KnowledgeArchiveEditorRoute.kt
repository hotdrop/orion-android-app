package jp.hotdrop.orion.ui.archive

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun KnowledgeArchiveEditorRoute(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: KnowledgeArchiveEditorViewModel = hiltViewModel(),
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
