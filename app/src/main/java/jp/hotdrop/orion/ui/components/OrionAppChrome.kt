package jp.hotdrop.orion.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CutCornerShape
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
import jp.hotdrop.orion.navigation.OrionTopLevelDestination
import jp.hotdrop.orion.ui.theme.OrionCyan
import jp.hotdrop.orion.ui.theme.OrionCyanMuted
import jp.hotdrop.orion.ui.theme.OrionPanel
import jp.hotdrop.orion.ui.theme.OrionTextMuted
import jp.hotdrop.orion.ui.theme.OrionTheme

@Composable
fun OrionHeader(
    title: String,
    isShowingBackNavigation: Boolean,
    onSettingsClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(OrionPanel)
            .border(width = 1.dp, color = OrionCyanMuted.copy(alpha = 0.55f))
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OrionSystemStatus()
            OrionHeaderAction(
                label = if (isShowingBackNavigation) "RETURN" else "CONFIG",
                accessibilityLabel = if (isShowingBackNavigation) "前の画面へ戻る" else "Settingsを開く",
                onClick = if (isShowingBackNavigation) onBackClick else onSettingsClick,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = title,
            modifier = Modifier.semantics { contentDescription = "現在の画面: $title" },
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.6.sp,
        )
    }
}

@Composable
private fun OrionSystemStatus() {
    Column {
        Text(
            text = "O R I O N",
            color = OrionCyan,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 4.sp,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(OrionCyan),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "SYSTEM ONLINE // LOCAL NODE",
                color = OrionTextMuted,
                fontSize = 10.sp,
                letterSpacing = 1.sp,
            )
        }
    }
}

@Composable
private fun OrionHeaderAction(
    label: String,
    accessibilityLabel: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .heightIn(min = 48.dp)
            .border(1.dp, OrionCyan, CutCornerShape(topStart = 8.dp, bottomEnd = 8.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = accessibilityLabel }
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "[ $label ]",
            color = OrionCyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
        )
    }
}

@Composable
fun OrionBottomNavigation(
    selectedDestination: OrionTopLevelDestination,
    onDestinationSelected: (OrionTopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(OrionPanel)
            .border(width = 1.dp, color = OrionCyanMuted.copy(alpha = 0.55f))
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        OrionTopLevelDestination.entries.forEach { destination ->
            OrionNavigationItem(
                destination = destination,
                selected = destination == selectedDestination,
                onClick = { onDestinationSelected(destination) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun OrionNavigationItem(
    destination: OrionTopLevelDestination,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (selected) OrionCyan else OrionCyanMuted.copy(alpha = 0.45f)
    val contentColor = if (selected) OrionCyan else OrionTextMuted
    val backgroundColor = if (selected) OrionCyan.copy(alpha = 0.1f) else Color.Transparent

    Row(
        modifier = modifier
            .heightIn(min = 56.dp)
            .background(backgroundColor, CutCornerShape(topStart = 10.dp, bottomEnd = 10.dp))
            .border(1.dp, borderColor, CutCornerShape(topStart = 10.dp, bottomEnd = 10.dp))
            .selectable(selected = selected, onClick = onClick, role = Role.Tab)
            .semantics { contentDescription = "${destination.title}を開く" }
            .padding(PaddingValues(horizontal = 12.dp, vertical = 8.dp)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .border(1.dp, contentColor, CutCornerShape(4.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = destination.code,
                color = contentColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(modifier = Modifier.width(9.dp))
        Text(
            text = destination.navigationLabel,
            color = contentColor,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            letterSpacing = 1.sp,
            maxLines = 1,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF030812, widthDp = 393)
@Composable
private fun OrionHeaderPreview() {
    OrionTheme {
        OrionHeader(
            title = OrionTopLevelDestination.Incoming.title,
            isShowingBackNavigation = false,
            onSettingsClick = {},
            onBackClick = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF030812, widthDp = 393)
@Composable
private fun OrionBottomNavigationPreview() {
    OrionTheme {
        OrionBottomNavigation(
            selectedDestination = OrionTopLevelDestination.Incoming,
            onDestinationSelected = {},
        )
    }
}
