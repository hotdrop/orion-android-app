package jp.hotdrop.orion.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val OrionColorScheme = darkColorScheme(
    primary = OrionCyan,
    onPrimary = OrionDeepNavy,
    primaryContainer = OrionPanelElevated,
    onPrimaryContainer = OrionText,
    secondary = OrionAmber,
    onSecondary = OrionDeepNavy,
    background = OrionDeepNavy,
    onBackground = OrionText,
    surface = OrionPanel,
    onSurface = OrionText,
    surfaceVariant = OrionPanelElevated,
    onSurfaceVariant = OrionTextMuted,
    outline = OrionCyanMuted,
    error = OrionError,
)

@Composable
fun OrionTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = OrionColorScheme,
        typography = Typography,
        content = content,
    )
}
