package jp.hotdrop.orion.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CutCornerShape
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

@Preview(showBackground = true)
@Composable
private fun OrionBottomNavigationPreview() {
    OrionTheme {
        OrionBottomNavigation(
            selectedDestination = OrionTopLevelDestination.Incoming,
            onDestinationSelected = {},
        )
    }
}