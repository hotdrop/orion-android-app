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
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
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
    playbackEnabled: Boolean = false,
) {
    val signalColor = if (isNew) OrionCyan else OrionCyanMuted

    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.dp,
                signalColor.copy(alpha = 0.8f),
                CutCornerShape(topStart = 14.dp, bottomEnd = 14.dp)
            )
            .background(
                OrionPanelElevated.copy(alpha = 0.55f),
                CutCornerShape(topStart = 14.dp, bottomEnd = 14.dp)
            )
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    text = if (isNew) "NEW SIGNAL" else "READ SIGNAL",
                    color = signalColor,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.1.sp,
                )
                Text(
                    text = updatedAtLabel,
                    color = OrionTextMuted,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            IncomingFavoriteTarget(
                title = title,
                isFavorite = isFavorite,
                playbackEnabled = playbackEnabled,
                onToggle = onToggleFavorite,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                .clickable(role = Role.Button, onClick = onClick),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(12.dp))
        IncomingFieldNote(
            title = title,
            memo = memo,
            playbackEnabled = playbackEnabled,
            onEdit = onEditMemo,
        )
        if (!isSyncTarget) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("同期対象外", color = OrionTextMuted, modifier = Modifier.weight(1f))
                TextButton(onClick = onDelete) { Text("REMOVE", color = OrionCyan) }
            }
        }
    }
}

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

@Preview(name = "Marked / short note")
@Composable
private fun IncomingDocumentMarkedPreview() {
    OrionTheme {
        IncomingIntelligenceDocumentCard(
            title = "Compose Rendering Report",
            updatedAtLabel = "09/05 12:30",
            isNew = false,
            onClick = {},
            isFavorite = true,
            memo = "次の実装で参照する。"
        )
    }
}

@Preview(name = "Long note / motion disabled")
@Composable
private fun IncomingDocumentLongNotePreview() {
    OrionTheme {
        IncomingIntelligenceDocumentCard(
            title = "Jetpack Composeの描画パフォーマンスを安定させるための実践ガイド",
            updatedAtLabel = "09/05 12:30",
            isNew = true,
            onClick = {},
            isFavorite = true,
            memo = "再コンポーズの測定方法と改善の手順を調査する。\n\n描画処理を分離する。\n実機でフレーム時間を確認する。"
        )
    }
}

@Preview(name = "Orphan / two lines", widthDp = 360)
@Composable
private fun IncomingDocumentOrphanPreview() {
    OrionTheme {
        IncomingIntelligenceDocumentCard(
            title = "保存しておく調査記録", updatedAtLabel = "09/04 08:00", isNew = false,
            onClick = {}, isSyncTarget = false, memo = "ローカル記録を保持。\n後で確認する。",
        )
    }
}

@Preview(name = "Interactive / capture and scrolling log", widthDp = 360)
@Composable
private fun IncomingDocumentMotionPreview() {
    var isFavorite by remember { mutableStateOf(false) }
    OrionTheme {
        IncomingIntelligenceDocumentCard(
            title = "Compose Rendering Report",
            updatedAtLabel = "09/05 12:30",
            isNew = true,
            onClick = {},
            isFavorite = isFavorite,
            onToggleFavorite = { isFavorite = !isFavorite },
            memo = "01 描画の責務を分離する。\n02 フレーム時間を計測する。\n03 次の調査対象を捕捉する。",
            playbackEnabled = true,
        )
    }
}
