package jp.hotdrop.orion.ui.incoming.memo

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun IncomingMemoRoute(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: IncomingMemoViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) { viewModel.events.collect { onClose() } }
    BackHandler(onBack = viewModel::requestBack)

    IncomingMemoScreen(
        state = state, onMemoChanged = viewModel::changeMemo, onSave = viewModel::save,
        onRetry = viewModel::load, onDismissDiscard = viewModel::dismissDiscard,
        onConfirmDiscard = viewModel::close, modifier = modifier,
    )
}
