package jp.hotdrop.orion.ui.intelligence

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * 編集イベントをViewModelへ渡し、保存・破棄の完了時に画面を閉じる。
 */
@Composable
fun RecordEditorRoute(
    canDelete: Boolean,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RecordEditorViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.closed.collect { onClose() }
    }
    BackHandler(onBack = viewModel::back)

    RecordEditorScreen(
        state = state,
        canDelete = canDelete,
        onChange = viewModel::change,
        onSave = viewModel::save,
        onSuggest = viewModel::suggest,
        onRetry = viewModel::load,
        onConfirmation = viewModel::confirm,
        onDelete = viewModel::delete,
        onDiscard = viewModel::discard,
        modifier = modifier,
    )
}
