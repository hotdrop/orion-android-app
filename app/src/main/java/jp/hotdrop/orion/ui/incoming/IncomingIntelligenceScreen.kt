package jp.hotdrop.orion.ui.incoming

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import jp.hotdrop.orion.navigation.OrionTopLevelDestination
import jp.hotdrop.orion.ui.components.PlaceholderModule
import jp.hotdrop.orion.ui.theme.OrionTheme

@Composable
fun IncomingIntelligenceScreen(modifier: Modifier = Modifier) {
    PlaceholderModule(
        moduleName = OrionTopLevelDestination.Incoming.title,
        moduleCode = OrionTopLevelDestination.Incoming.code,
        modifier = modifier,
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF030812, widthDp = 393, heightDp = 620)
@Composable
private fun IncomingIntelligenceScreenPreview() {
    OrionTheme {
        IncomingIntelligenceScreen()
    }
}
