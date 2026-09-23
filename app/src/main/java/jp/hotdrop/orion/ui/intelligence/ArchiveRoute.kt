package jp.hotdrop.orion.ui.intelligence

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * 一覧の監視とタブ選択の復元を担当し、画面外への遷移を呼び出し元へ渡す。
 */
@Composable
fun ArchiveRoute(
    onNewSignal: () -> Unit,
    onAnalysis: () -> Unit,
    onSignal: (Long) -> Unit,
    onFocus: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ArchiveViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var focusTab by rememberSaveable { mutableStateOf(false) }

    ArchiveScreen(
        state = state,
        focusTab = focusTab,
        onTab = { focusTab = it },
        onNewSignal = onNewSignal,
        onAnalysis = onAnalysis,
        onSignal = onSignal,
        onFocus = onFocus,
        onRetry = viewModel::reload,
        modifier = modifier,
    )
}
