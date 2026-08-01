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
    relativePath: String,
    isNew: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val signalColor = if (isNew) OrionCyan else OrionCyanMuted

    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, signalColor.copy(alpha = 0.8f), IncomingDocumentCardShape)
            .background(OrionPanelElevated.copy(alpha = 0.55f), IncomingDocumentCardShape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = "${title}をGoogleドキュメントで開く" }
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (isNew) "NEW SIGNAL // UNREAD" else "ARCHIVED SIGNAL // CACHED",
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
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "PATH // $relativePath",
                modifier = Modifier.weight(1f),
                color = OrionTextMuted,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "[ OPEN ]",
                color = OrionCyan,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
            )
        }
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
            relativePath = "Android/Compose/Weekly",
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
            relativePath = "AI/RAG/Research/Long/Nested/Path",
            isNew = false,
            onClick = {},
        )
    }
}
