package jp.hotdrop.orion.ui.incoming.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.hotdrop.orion.ui.theme.OrionCyan
import jp.hotdrop.orion.ui.theme.OrionCyanMuted
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.time.Duration.Companion.milliseconds

@Composable
internal fun IncomingFieldNote(
    title: String,
    memo: String,
    playbackEnabled: Boolean,
    onEdit: () -> Unit,
) {
    Column(
        Modifier.fillMaxWidth()
            .border(1.dp, OrionCyanMuted.copy(alpha = 0.45f))
            .clickable(role = Role.Button, onClick = onEdit)
            .semantics {
                contentDescription = "${title}の概要メモを編集"
                text = AnnotatedString(memo.ifBlank { "ADD NOTE" })
            }
            .padding(10.dp),
    ) {
        Text(
            "FIELD NOTE",
            color = OrionCyanMuted,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(bottom = 6.dp).clearAndSetSemantics {},
        )
        val density = LocalDensity.current
        val lineHeight = with(density) { NOTE_LINE_HEIGHT.toPx() }
        val style = MaterialTheme.typography.bodyMedium.copy(
            color = if (memo.isBlank()) OrionCyan else MaterialTheme.colorScheme.onBackground,
            lineHeight = NOTE_LINE_HEIGHT,
            platformStyle = PlatformTextStyle(includeFontPadding = false),
            lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None),
        )
        val measurer = rememberTextMeasurer()
        val durationScale = rememberCoroutineScope().coroutineContext[MotionDurationScale]?.scaleFactor ?: 1f
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val width = constraints.maxWidth
            val layout = remember(memo, style, width, density, measurer) {
                measurer.measure(memo.ifBlank { "ADD NOTE" }, style, constraints = Constraints(maxWidth = width))
            }
            val cycleHeight = layout.size.height + lineHeight
            val offset = remember(layout) { Animatable(0f) }
            val shouldScroll = layout.lineCount > NOTE_VISIBLE_LINES && playbackEnabled && durationScale > 0f
            LaunchedEffect(layout, shouldScroll, durationScale) {
                offset.snapTo(0f)
                if (shouldScroll) {
                    delay(NOTE_INITIAL_PAUSE_MILLIS.milliseconds)
                    val duration = (cycleHeight / lineHeight * NOTE_LINE_SCROLL_MILLIS).toInt().coerceAtLeast(1)
                    while (isActive) {
                        offset.animateTo(cycleHeight, tween(duration, easing = LinearEasing))
                        offset.snapTo(0f)
                    }
                }
            }
            Canvas(Modifier.fillMaxWidth().height(with(density) { (lineHeight * NOTE_VISIBLE_LINES).toDp() })) {
                clipRect {
                    val y = if (shouldScroll) offset.value else 0f
                    drawText(layout, topLeft = Offset(0f, -y))
                    if (shouldScroll) drawText(layout, topLeft = Offset(0f, cycleHeight - y))
                }
            }
        }
    }
}

private val NOTE_LINE_HEIGHT = 20.sp
private const val NOTE_VISIBLE_LINES = 2
private const val NOTE_INITIAL_PAUSE_MILLIS = 1_000L
private const val NOTE_LINE_SCROLL_MILLIS = 2_000
