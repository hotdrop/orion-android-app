package jp.hotdrop.orion.ui.incoming.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.hotdrop.orion.ui.theme.OrionCyan
import jp.hotdrop.orion.ui.theme.OrionCyanMuted
import jp.hotdrop.orion.ui.theme.OrionTextMuted
import jp.hotdrop.orion.ui.theme.OrionTheme

@Composable
fun IncomingIntelligenceHeader(
    lastSyncedAtLabel: String?,
    isSyncing: Boolean,
    syncEnabled: Boolean,
    onSync: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 76.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "SIGNAL ACQUISITION GRID",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "LAST ${lastSyncedAtLabel ?: "--"}",
                style = MaterialTheme.typography.bodyMedium,
                color = OrionTextMuted
            )
        }
        IncomingSyncReactor(
            isSyncing = isSyncing,
            enabled = syncEnabled,
            onClick = onSync,
        )
    }
}

@Composable
private fun IncomingSyncReactor(
    isSyncing: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val signalColor = if (enabled || isSyncing) OrionCyan else OrionCyanMuted

    Box(
        modifier = modifier
            .size(76.dp)
            .clip(CircleShape)
            .clickable(
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .semantics { contentDescription = "Incoming Intelligenceを同期" },
        contentAlignment = Alignment.Center,
    ) {
        SyncReactorRings(
            color = signalColor,
            active = enabled || isSyncing,
            modifier = Modifier.fillMaxSize(),
        )
        if (isSyncing) {
            CircularProgressIndicator(
                modifier = Modifier.size(52.dp),
                color = OrionCyan,
                trackColor = Color.Transparent,
                strokeWidth = 2.dp,
            )
        }
        Text(
            text = "SYNC",
            color = signalColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
        )
    }
}

@Composable
private fun SyncReactorRings(
    color: Color,
    active: Boolean,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val outerRadius = size.minDimension / 2f - 1.dp.toPx()
        val innerRadius = outerRadius * 0.64f
        val alpha = if (active) 0.9f else 0.38f

        drawCircle(
            color = color.copy(alpha = alpha * 0.28f),
            radius = outerRadius,
            center = center,
            style = Stroke(width = 1.dp.toPx()),
        )
        drawCircle(
            color = color.copy(alpha = alpha * 0.42f),
            radius = innerRadius,
            center = center,
            style = Stroke(width = 1.dp.toPx()),
        )
        for (startAngle in floatArrayOf(-82f, 8f, 98f, 188f)) {
            drawArc(
                color = color.copy(alpha = alpha),
                startAngle = startAngle,
                sweepAngle = 42f,
                useCenter = false,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
            )
        }

        val tickInset = 5.dp.toPx()
        val tickLength = 7.dp.toPx()
        drawLine(
            color = color.copy(alpha = alpha * 0.7f),
            start = Offset(center.x, tickInset),
            end = Offset(center.x, tickInset + tickLength),
            strokeWidth = 1.dp.toPx(),
        )
        drawLine(
            color = color.copy(alpha = alpha * 0.7f),
            start = Offset(center.x, size.height - tickInset),
            end = Offset(center.x, size.height - tickInset - tickLength),
            strokeWidth = 1.dp.toPx(),
        )
    }
}

@Preview
@Composable
private fun IncomingIntelligenceHeaderReadyPreview() {
    OrionTheme {
        IncomingIntelligenceHeader(
            lastSyncedAtLabel = "08/01 09:45",
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
            lastSyncedAtLabel = "08/01 09:45",
            isSyncing = true,
            syncEnabled = false,
            onSync = {},
        )
    }
}

@Preview
@Composable
private fun IncomingIntelligenceHeaderDisabledPreview() {
    OrionTheme {
        IncomingIntelligenceHeader(
            lastSyncedAtLabel = null,
            isSyncing = false,
            syncEnabled = false,
            onSync = {},
        )
    }
}
