package jp.hotdrop.orion.ui.intelligence

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * 記録の監視と戻る操作を詳細画面へ接続する。
 */
@Composable
fun RecordDetailRoute(
    focus: Boolean,
    id: Long,
    onEdit: () -> Unit,
    onSignal: (Long) -> Unit,
    onFocus: (Long) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ArchiveViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    BackHandler(onBack = onBack)

    RecordDetailScreen(
        state = state,
        focus = focus,
        id = id,
        onEdit = onEdit,
        onSignal = onSignal,
        onFocus = onFocus,
        onRetry = viewModel::reload,
        modifier = modifier,
    )
}
