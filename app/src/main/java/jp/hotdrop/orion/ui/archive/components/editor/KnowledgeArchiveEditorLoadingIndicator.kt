package jp.hotdrop.orion.ui.archive.components.editor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import jp.hotdrop.orion.ui.theme.OrionCyan
import jp.hotdrop.orion.ui.theme.OrionTheme

@Composable
fun KnowledgeArchiveEditorLoadingIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            color = OrionCyan,
            strokeWidth = 2.dp,
        )
    }
}

@Preview
@Composable
private fun KnowledgeArchiveEditorLoadingIndicatorPreview() {
    OrionTheme {
        KnowledgeArchiveEditorLoadingIndicator()
    }
}
