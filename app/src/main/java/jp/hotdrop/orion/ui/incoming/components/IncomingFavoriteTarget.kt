package jp.hotdrop.orion.ui.incoming.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import jp.hotdrop.orion.ui.theme.OrionCyan
import jp.hotdrop.orion.ui.theme.OrionCyanMuted
import jp.hotdrop.orion.ui.theme.OrionTheme

@Composable
internal fun IncomingFavoriteTarget(
    title: String,
    isFavorite: Boolean,
    playbackEnabled: Boolean,
    onToggle: () -> Unit,
) {
    val capture = remember { Animatable(if (isFavorite) 1f else 0f) }
    val flash = remember { Animatable(0f) }
    val durationScale = rememberCoroutineScope().coroutineContext[MotionDurationScale]?.scaleFactor ?: 1f

    LaunchedEffect(isFavorite, playbackEnabled, durationScale) {
        flash.snapTo(0f)
        val target = if (isFavorite) 1f else 0f
        if (!playbackEnabled || durationScale == 0f) {
            capture.snapTo(target)
        } else if (capture.value != target) {
            capture.animateTo(
                targetValue = target,
                animationSpec = tween(if (isFavorite) 300 else 200))
            if (isFavorite) {
                flash.snapTo(1f)
                flash.animateTo(0f, tween(160))
            }
        }
    }
    FavoriteTargetContent(
        title = title,
        isFavorite = isFavorite,
        capture = { capture.value },
        flash = { flash.value },
        onToggle = onToggle
    )
}

@Composable
private fun FavoriteTargetContent(
    title: String,
    isFavorite: Boolean,
    capture: () -> Float,
    flash: () -> Float,
    onToggle: () -> Unit,
) {
    Column(
        modifier = Modifier
            .widthIn(min = 76.dp)
            .toggleable(
                value = isFavorite,
                role = Role.Checkbox,
                onValueChange = { onToggle() }
            )
            .semantics {
                contentDescription = "${title}のお気に入りを切り替え"
                stateDescription = if (isFavorite) "登録済み" else "未登録"
            }
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Canvas(Modifier.size(36.dp)) {
            val progress = capture()
            val inset = (3f + 5f * progress).dp.toPx()
            val arm = 7.dp.toPx()
            val color = OrionCyan.copy(alpha = 0.4f + 0.6f * progress)
            val stroke = (1.3f + flash()).dp.toPx()
            for (xSign in -1..1 step 2) {
                for (ySign in -1..1 step 2) {
                    val corner = Offset(
                        if (xSign < 0) inset else size.width - inset,
                        if (ySign < 0) inset else size.height - inset,
                    )
                    drawLine(color, corner, corner + Offset(-xSign * arm, 0f), stroke)
                    drawLine(color, corner, corner + Offset(0f, -ySign * arm), stroke)
                }
            }
            drawCircle(color, radius = (1.5f + progress).dp.toPx())
            if (flash() > 0f) {
                drawCircle(OrionCyan.copy(alpha = flash() * 0.2f), radius = 16.dp.toPx())
            }
        }
        Text(
            text = if (isFavorite) "MARKED" else "MARK",
            color = if (isFavorite) OrionCyan else OrionCyanMuted,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.graphicsLayer {
                alpha = if (isFavorite) capture() else 1f
            }.clearAndSetSemantics {},
        )
    }
}

@Preview(name = "Target / unmarked")
@Composable
private fun FavoriteTargetIdlePreview() {
    OrionTheme {
        FavoriteTargetContent(
            "資料",
            false,
            { 0f },
            { 0f },
            {}
        )
    }
}

@Preview(name = "Target / marked")
@Composable
private fun FavoriteTargetMarkedPreview() {
    OrionTheme {
        FavoriteTargetContent(
            "資料",
            true,
            { 1f },
            { 0f },
            {}
        )
    }
}

@Preview(name = "Target / releasing")
@Composable
private fun FavoriteTargetReleasingPreview() {
    OrionTheme {
        FavoriteTargetContent(
            "資料",
            false,
            { 0.5f },
            { 0f },
            {}
        )
    }
}
