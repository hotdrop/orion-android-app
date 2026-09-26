package jp.hotdrop.orion.ui.intelligence.analysis

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * 分析画面にクリップボード操作と、保存・破棄後の画面遷移を接続する。
 */
@Composable
fun AnalysisRoute(
    onClose: (Long?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AnalysisViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var copied by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.closed.collect(onClose)
    }
    BackHandler(onBack = viewModel::back)

    AnalysisScreen(
        state = state,
        copied = copied,
        onCopy = { prompt ->
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("ORION Focus Analysis", prompt))
            copied = true
        },
        onStage = viewModel::stage,
        onChange = viewModel::change,
        onSuggest = viewModel::suggest,
        onSave = viewModel::save,
        onRetry = viewModel::load,
        onRetryDraft = viewModel::retryDraft,
        onConfirmDiscard = viewModel::confirmDiscard,
        onDiscard = viewModel::discard,
        modifier = modifier,
    )
}
