package jp.hotdrop.orion.ui.incoming.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.hotdrop.orion.ui.theme.OrionAmber
import jp.hotdrop.orion.ui.theme.OrionCyan
import jp.hotdrop.orion.ui.theme.OrionPanelElevated
import jp.hotdrop.orion.ui.theme.OrionTextMuted
import jp.hotdrop.orion.ui.theme.OrionTheme

@Composable
fun IncomingIntelligenceDriveNotConfigured(
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IncomingIntelligenceEmptyPanel(
        code = "DRIVE TARGET // NOT CONFIGURED",
        message = "SettingsでGoogle Driveの対象フォルダを設定してください。",
        toneColor = OrionAmber,
        modifier = modifier,
    ) {
        IncomingIntelligenceOutlineAction(
            label = "OPEN CONFIG",
            accessibilityLabel = "Settingsを開いてGoogle Driveを設定",
            onClick = onOpenSettings,
        )
    }
}

@Composable
fun IncomingIntelligenceInitialSync(modifier: Modifier = Modifier) {
    IncomingIntelligenceEmptyPanel(
        code = "SIGNAL SCAN // IN PROGRESS",
        message = "Google Driveから技術情報を取得しています。",
        toneColor = OrionCyan,
        modifier = modifier,
    ) {
        CircularProgressIndicator(color = OrionCyan, strokeWidth = 2.dp)
    }
}

@Composable
fun IncomingIntelligenceNoDocuments(
    onSync: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IncomingIntelligenceEmptyPanel(
        code = "NO SIGNALS DETECTED",
        message = "対象フォルダにGoogleドキュメントがありません。",
        toneColor = OrionAmber,
        modifier = modifier,
    ) {
        IncomingIntelligenceOutlineAction(
            label = "SCAN AGAIN",
            accessibilityLabel = "Google Driveを再同期",
            onClick = onSync,
        )
    }
}

@Composable
private fun IncomingIntelligenceEmptyPanel(
    code: String,
    message: String,
    toneColor: Color,
    modifier: Modifier = Modifier,
    action: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .border(1.dp, toneColor.copy(alpha = 0.65f), IncomingEmptyStateShape)
            .background(OrionPanelElevated.copy(alpha = 0.35f), IncomingEmptyStateShape)
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = code,
                color = toneColor,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                color = OrionTextMuted,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(18.dp))
            action()
        }
    }
}

@Composable
private fun IncomingIntelligenceOutlineAction(
    label: String,
    accessibilityLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .heightIn(min = 48.dp)
            .border(1.dp, OrionCyan, IncomingEmptyActionShape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = accessibilityLabel }
            .padding(horizontal = 18.dp),
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

private val IncomingEmptyStateShape = CutCornerShape(topStart = 14.dp, bottomEnd = 14.dp)
private val IncomingEmptyActionShape = CutCornerShape(topStart = 8.dp, bottomEnd = 8.dp)

@Preview
@Composable
private fun IncomingIntelligenceDriveNotConfiguredPreview() {
    OrionTheme { IncomingIntelligenceDriveNotConfigured(onOpenSettings = {}) }
}

@Preview
@Composable
private fun IncomingIntelligenceInitialSyncPreview() {
    OrionTheme { IncomingIntelligenceInitialSync() }
}

@Preview
@Composable
private fun IncomingIntelligenceNoDocumentsPreview() {
    OrionTheme { IncomingIntelligenceNoDocuments(onSync = {}) }
}

@Preview
@Composable
private fun IncomingIntelligenceEmptyPanelPreview() {
    OrionTheme {
        IncomingIntelligenceEmptyPanel(
            code = "EMPTY STATE // PREVIEW",
            message = "代表的な空状態の表示です。",
            toneColor = OrionAmber,
        ) {
            IncomingIntelligenceOutlineAction(
                label = "ACTION",
                accessibilityLabel = "アクション",
                onClick = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun IncomingIntelligenceOutlineActionPreview() {
    OrionTheme {
        IncomingIntelligenceOutlineAction(
            label = "SCAN AGAIN",
            accessibilityLabel = "Google Driveを再同期",
            onClick = {},
        )
    }
}
