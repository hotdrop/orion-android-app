package jp.hotdrop.orion.ui.archive

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun KnowledgeArchiveRoute(
    onCreateEntry: () -> Unit,
    onEditEntry: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: KnowledgeArchiveViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    KnowledgeArchiveScreen(
        uiState = uiState,
        onCreateEntry = onCreateEntry,
        onEditEntry = onEditEntry,
        onOpenUrl = { url ->
            if (!openExternalUrl(context, url)) viewModel.reportUrlOpenFailure()
        },
        onDismissUrlError = viewModel::clearUrlOpenFailure,
        modifier = modifier,
    )
}

private fun openExternalUrl(context: Context, url: String): Boolean = try {
    context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    true
} catch (_: ActivityNotFoundException) {
    false
} catch (_: SecurityException) {
    false
}
