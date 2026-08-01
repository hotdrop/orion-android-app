package jp.hotdrop.orion.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import jp.hotdrop.orion.navigation.OrionDestination
import jp.hotdrop.orion.ui.components.PlaceholderModule
import jp.hotdrop.orion.ui.theme.OrionTheme

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    PlaceholderModule(
        moduleName = OrionDestination.SettingsTitle,
        moduleCode = "CFG",
        modifier = modifier,
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF030812, widthDp = 393, heightDp = 620)
@Composable
private fun SettingsScreenPreview() {
    OrionTheme {
        SettingsScreen()
    }
}
