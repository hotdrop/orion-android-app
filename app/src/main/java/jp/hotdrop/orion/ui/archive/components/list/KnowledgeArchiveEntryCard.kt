package jp.hotdrop.orion.ui.archive.components.list

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
fun KnowledgeArchiveEntryCard(
    entryId: Long,
    title: String,
    memo: String,
    onEdit: () -> Unit,
    onOpenUrl: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, OrionCyanMuted.copy(alpha = 0.75f), CutCornerShape(topStart = 14.dp, bottomEnd = 14.dp))
            .background(OrionPanelElevated.copy(alpha = 0.55f), CutCornerShape(topStart = 14.dp, bottomEnd = 14.dp))
            .clickable(role = Role.Button, onClick = onEdit)
            .semantics { contentDescription = "${title}の記録を編集" }
            .padding(16.dp),
    ) {
        Text(
            text = "ARCHIVE RECORD // ${entryId.toString().padStart(4, '0')}",
            color = OrionCyan,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.1.sp,
        )
        Spacer(modifier = Modifier.height(7.dp))
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        if (memo.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = memo,
                color = OrionTextMuted,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ArchiveCardAction(
                label = "OPEN LINK",
                accessibilityLabel = "${title}のURLを開く",
                onClick = onOpenUrl,
                modifier = Modifier.weight(1f),
            )
            ArchiveCardAction(
                label = "EDIT",
                accessibilityLabel = "${title}を編集",
                onClick = onEdit,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ArchiveCardAction(
    label: String,
    accessibilityLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .heightIn(min = 48.dp)
            .border(1.dp, OrionCyanMuted, CutCornerShape(topStart = 6.dp, bottomEnd = 6.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = accessibilityLabel },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "[ $label ]",
            color = OrionCyan,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
        )
    }
}

@Preview
@Composable
private fun ArchiveCardActionPreview() {
    OrionTheme {
        ArchiveCardAction(
            label = "OPEN LINK",
            accessibilityLabel = "記事のURLを開く",
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun KnowledgeArchiveEntryCardPreview() {
    OrionTheme {
        KnowledgeArchiveEntryCard(
            entryId = 1,
            title = "テスト",
            memo = "テストメモ",
            onEdit = {},
            onOpenUrl = {},
        )
    }
}
