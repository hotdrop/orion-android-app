package jp.hotdrop.orion.ui.incoming.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.hotdrop.orion.ui.incoming.IncomingIntelligenceStatusTone
import jp.hotdrop.orion.ui.theme.OrionAmber
import jp.hotdrop.orion.ui.theme.OrionCyan
import jp.hotdrop.orion.ui.theme.OrionError
import jp.hotdrop.orion.ui.theme.OrionPanelElevated
import jp.hotdrop.orion.ui.theme.OrionTextMuted
import jp.hotdrop.orion.ui.theme.OrionTheme

@Composable
internal fun IncomingIntelligenceStatusPanel(
    code: String,
    description: String,
    tone: IncomingIntelligenceStatusTone,
    lastSyncedAtLabel: String?,
    modifier: Modifier = Modifier,
) {
    val statusColor = tone.toColor()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, statusColor.copy(alpha = 0.7f))
            .background(OrionPanelElevated.copy(alpha = 0.42f))
            .padding(horizontal = 14.dp, vertical = 11.dp)
            .semantics { contentDescription = "同期状態: $description" },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = code,
                color = statusColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
            )
            Text(
                text = description,
                color = OrionTextMuted,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        lastSyncedAtLabel?.let { label ->
            Text(
                text = "LAST // $label",
                color = OrionTextMuted,
                fontSize = 9.sp,
                letterSpacing = 0.8.sp,
            )
        }
    }
}

private fun IncomingIntelligenceStatusTone.toColor(): Color = when (this) {
    IncomingIntelligenceStatusTone.Normal -> OrionCyan
    IncomingIntelligenceStatusTone.Warning -> OrionAmber
    IncomingIntelligenceStatusTone.Error -> OrionError
}

@Preview
@Composable
private fun IncomingIntelligenceStatusPanelReadyPreview() {
    OrionTheme {
        IncomingIntelligenceStatusPanel(
            code = "UPLINK // READY",
            description = "Google Drive同期チャネルは待機中です。",
            tone = IncomingIntelligenceStatusTone.Normal,
            lastSyncedAtLabel = "08/01 09:45",
        )
    }
}

@Preview
@Composable
private fun IncomingIntelligenceStatusPanelErrorPreview() {
    OrionTheme {
        IncomingIntelligenceStatusPanel(
            code = "UPLINK // ERROR",
            description = "認証を確認してから再試行してください。",
            tone = IncomingIntelligenceStatusTone.Error,
            lastSyncedAtLabel = "07/31 23:10",
        )
    }
}
