package jp.hotdrop.orion.ui.incoming.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.hotdrop.orion.ui.theme.OrionCyan
import jp.hotdrop.orion.ui.theme.OrionCyanMuted
import jp.hotdrop.orion.ui.theme.OrionPanelElevated
import jp.hotdrop.orion.ui.theme.OrionTextMuted
import jp.hotdrop.orion.ui.theme.OrionTheme

@Composable
fun IncomingIntelligenceDocumentCard(
    title: String,
    updatedAtLabel: String,
    isNew: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFavorite: Boolean = false,
    memo: String = "",
    isSyncTarget: Boolean = true,
    onToggleFavorite: () -> Unit = {},
    onEditMemo: () -> Unit = {},
    onDelete: () -> Unit = {},
) {
    val signalColor = if (isNew) OrionCyan else OrionCyanMuted

    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, signalColor.copy(alpha = 0.8f), IncomingDocumentCardShape)
            .background(OrionPanelElevated.copy(alpha = 0.55f), IncomingDocumentCardShape)
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (isNew) "NEW SIGNAL // UNREAD" else "ARCHIVED SIGNAL // READ",
                modifier = Modifier.weight(1f),
                color = signalColor,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.1.sp,
            )
            Text(
                text = updatedAtLabel,
                color = OrionTextMuted,
                fontSize = 9.sp,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                .clickable(role = Role.Button, onClick = onClick)
                .semantics { contentDescription = "${title}を文書アプリで開く" },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconToggleButton(
                checked = isFavorite,
                onCheckedChange = { onToggleFavorite() },
                modifier = Modifier.semantics {
                    contentDescription = if (isFavorite) "${title}のお気に入りを解除" else "${title}をお気に入りに追加"
                },
            ) {
                Text(if (isFavorite) "★" else "☆", color = OrionCyan, fontSize = 24.sp)
            }
            if (!isSyncTarget) {
                Text("同期対象外", color = OrionTextMuted, modifier = Modifier.weight(1f))
                TextButton(onClick = onDelete) { Text("REMOVE", color = OrionCyan) }
            }
        }
        Text(
            text = memo.ifBlank { "ADD NOTE" },
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                .clickable(role = Role.Button, onClick = onEditMemo)
                .semantics { contentDescription = "${title}の概要メモを編集" }
                .padding(vertical = 8.dp),
            color = if (memo.isBlank()) OrionCyan else MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private val IncomingDocumentCardShape = CutCornerShape(topStart = 14.dp, bottomEnd = 14.dp)

@Preview
@Composable
private fun IncomingIntelligenceDocumentCardNewPreview() {
    OrionTheme {
        IncomingIntelligenceDocumentCard(
            title = "Jetpack Composeの描画パフォーマンスを安定させるための実践ガイド",
            updatedAtLabel = "08/01 09:42",
            isNew = true,
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun IncomingIntelligenceDocumentCardCachedPreview() {
    OrionTheme {
        IncomingIntelligenceDocumentCard(
            title = "Agentic RAG: Production Architecture Notes",
            updatedAtLabel = "07/29 22:16",
            isNew = false,
            onClick = {},
        )
    }
}
