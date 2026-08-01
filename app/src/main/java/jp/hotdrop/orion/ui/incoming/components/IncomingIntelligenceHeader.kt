package jp.hotdrop.orion.ui.incoming.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.hotdrop.orion.ui.theme.OrionCyan
import jp.hotdrop.orion.ui.theme.OrionCyanMuted
import jp.hotdrop.orion.ui.theme.OrionDeepNavy
import jp.hotdrop.orion.ui.theme.OrionPanel
import jp.hotdrop.orion.ui.theme.OrionTextMuted
import jp.hotdrop.orion.ui.theme.OrionTheme

internal const val IncomingSyncButtonTag = "incoming_sync_button"

@Composable
fun IncomingIntelligenceHeader(
    documentCount: Int,
    newDocumentCount: Int,
    isSyncing: Boolean,
    syncEnabled: Boolean,
    onSync: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = OrionCyanMuted,
                shape = CutCornerShape(topStart = 16.dp, bottomEnd = 16.dp)
            )
            .background(
                color = OrionPanel.copy(alpha = 0.82f),
                shape = CutCornerShape(topStart = 16.dp, bottomEnd = 16.dp)
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "MODULE // IN",
                color = OrionCyan,
                fontSize = 10.sp,
                letterSpacing = 1.4.sp,
            )
            Text(
                text = "SIGNAL ACQUISITION GRID",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.1.sp,
            )
            Text(
                text = "$documentCount SIGNALS // $newDocumentCount NEW",
                color = OrionTextMuted,
                fontSize = 10.sp,
                letterSpacing = 1.sp,
            )
        }
        Button(
            onClick = onSync,
            modifier = Modifier
                .heightIn(min = 48.dp)
                .testTag(IncomingSyncButtonTag)
                .semantics { contentDescription = "Incoming Intelligenceを同期" },
            enabled = syncEnabled,
            shape = CutCornerShape(topStart = 8.dp, bottomEnd = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = OrionCyan,
                contentColor = OrionDeepNavy,
                disabledContainerColor = OrionCyanMuted.copy(alpha = 0.35f),
                disabledContentColor = OrionTextMuted,
            ),
        ) {
            if (isSyncing) {
                CircularProgressIndicator(
                    modifier = Modifier.height(18.dp),
                    color = OrionDeepNavy,
                    strokeWidth = 2.dp,
                )
            } else {
                Text("[ SYNC ]", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
        }
    }
}

@Preview
@Composable
private fun IncomingIntelligenceHeaderReadyPreview() {
    OrionTheme {
        IncomingIntelligenceHeader(
            documentCount = 12,
            newDocumentCount = 3,
            isSyncing = false,
            syncEnabled = true,
            onSync = {},
        )
    }
}

@Preview
@Composable
private fun IncomingIntelligenceHeaderSyncingPreview() {
    OrionTheme {
        IncomingIntelligenceHeader(
            documentCount = 12,
            newDocumentCount = 3,
            isSyncing = true,
            syncEnabled = false,
            onSync = {},
        )
    }
}
