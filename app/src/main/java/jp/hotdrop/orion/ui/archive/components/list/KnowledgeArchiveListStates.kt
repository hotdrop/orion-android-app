package jp.hotdrop.orion.ui.archive.components.list

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.hotdrop.orion.ui.theme.OrionAmber
import jp.hotdrop.orion.ui.theme.OrionCyan
import jp.hotdrop.orion.ui.theme.OrionCyanMuted
import jp.hotdrop.orion.ui.theme.OrionError
import jp.hotdrop.orion.ui.theme.OrionPanelElevated
import jp.hotdrop.orion.ui.theme.OrionTextMuted
import jp.hotdrop.orion.ui.theme.OrionTheme

@Composable
fun KnowledgeArchiveListLoading(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = OrionCyan, strokeWidth = 2.dp)
            Spacer(modifier = Modifier.height(12.dp))
            Text("LOCAL INDEX // LOADING", color = OrionCyan, letterSpacing = 1.2.sp)
        }
    }
}

@Composable
fun KnowledgeArchiveListEmpty(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .border(1.dp, OrionCyanMuted.copy(alpha = 0.65f))
            .background(OrionPanelElevated.copy(alpha = 0.35f))
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "NO KNOWLEDGE RECORDS",
                color = OrionAmber,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.3.sp,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "気になった技術記事のURLとメモを記録してください。",
                color = OrionTextMuted,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
fun KnowledgeArchiveListLoadError(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "LOCAL INDEX // READ ERROR\n保存済みデータを読み込めませんでした。",
            color = OrionError,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
fun KnowledgeArchiveListErrorPanel(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, OrionError)
            .background(OrionPanelElevated.copy(alpha = 0.5f))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(message, modifier = Modifier.weight(1f), color = OrionError)
        Text(
            text = "[ ACK ]",
            modifier = Modifier
                .heightIn(min = 48.dp)
                .clickable(role = Role.Button, onClick = onDismiss)
                .padding(horizontal = 12.dp, vertical = 14.dp),
            color = OrionCyan,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Preview
@Composable
private fun KnowledgeArchiveListLoadingPreview() {
    OrionTheme { KnowledgeArchiveListLoading() }
}

@Preview
@Composable
private fun KnowledgeArchiveListEmptyPreview() {
    OrionTheme { KnowledgeArchiveListEmpty() }
}

@Preview
@Composable
private fun KnowledgeArchiveListLoadErrorPreview() {
    OrionTheme { KnowledgeArchiveListLoadError() }
}

@Preview
@Composable
private fun KnowledgeArchiveListErrorPanelPreview() {
    OrionTheme {
        KnowledgeArchiveListErrorPanel(
            message = "URLを開けるアプリが見つかりませんでした。",
            onDismiss = {},
        )
    }
}
