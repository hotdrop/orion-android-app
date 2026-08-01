package jp.hotdrop.orion.ui.authentication

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.hotdrop.orion.ui.theme.OrionAmber
import jp.hotdrop.orion.ui.theme.OrionCyan
import jp.hotdrop.orion.ui.theme.OrionCyanMuted
import jp.hotdrop.orion.ui.theme.OrionDeepNavy
import jp.hotdrop.orion.ui.theme.OrionError
import jp.hotdrop.orion.ui.theme.OrionPanel
import jp.hotdrop.orion.ui.theme.OrionText
import jp.hotdrop.orion.ui.theme.OrionTextMuted
import jp.hotdrop.orion.ui.theme.OrionTheme
import kotlin.math.min

const val AUTHENTICATION_GATE_TAG = "authentication_gate"
const val AUTHENTICATION_PRIMARY_ACTION_TAG = "authentication_primary_action"

@Composable
fun OrionSecureRoot(
    uiState: AuthenticationUiState,
    onAuthenticationRequested: () -> Unit,
    onUnlockAnimationFinished: () -> Unit,
    onOpenSecuritySettings: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val isUnlocked = uiState == AuthenticationUiState.Unlocked

    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = if (isUnlocked) {
                Modifier.fillMaxSize()
            } else {
                Modifier
                    .fillMaxSize()
                    .clearAndSetSemantics { }
            },
        ) {
            content()
        }

        if (!isUnlocked) {
            AuthenticationGate(
                uiState = uiState,
                onAuthenticationRequested = onAuthenticationRequested,
                onUnlockAnimationFinished = onUnlockAnimationFinished,
                onOpenSecuritySettings = onOpenSecuritySettings,
                onClose = onClose,
            )
        }
    }
}

@Composable
internal fun AuthenticationGate(
    uiState: AuthenticationUiState,
    onAuthenticationRequested: () -> Unit,
    onUnlockAnimationFinished: () -> Unit,
    onOpenSecuritySettings: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val phase = remember { Animatable(0f) }
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(uiState) {
        phase.snapTo(0f)
        when (uiState) {
            AuthenticationUiState.Booting -> {
                phase.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = BOOT_DURATION_MILLIS,
                        easing = FastOutSlowInEasing,
                    ),
                )
                onAuthenticationRequested()
            }

            AuthenticationUiState.AccessGranted -> {
                phase.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = UNLOCK_DURATION_MILLIS,
                        easing = FastOutSlowInEasing,
                    ),
                )
                onUnlockAnimationFinished()
            }

            else -> Unit
        }
    }

    BackHandler(onBack = onClose)

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag(AUTHENTICATION_GATE_TAG)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF01040A),
                        OrionDeepNavy,
                        Color(0xFF020A12),
                    ),
                ),
            )
            .semantics {
                contentDescription = "ORIONセキュアアクセス"
                stateDescription = uiState.accessibilityState()
            },
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {},
                ),
        )
        SecurityGrid()

        if (uiState == AuthenticationUiState.Booting) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .align(Alignment.TopCenter)
                    .graphicsLayer {
                        translationY = 900.dp.toPx() * phase.value
                    }
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, OrionCyan, Color.Transparent),
                        ),
                    ),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SecurityHeader()
            Spacer(modifier = Modifier.weight(1f))
            ScannerCore(
                uiState = uiState,
                progress = phase.value,
            )
            Spacer(modifier = Modifier.height(30.dp))
            SecurityStatus(uiState = uiState, progress = phase.value)
            Spacer(modifier = Modifier.weight(1f))
            SecurityFooter(
                uiState = uiState,
                onAuthenticationRequested = onAuthenticationRequested,
                onOpenSecuritySettings = onOpenSecuritySettings,
            )
        }
    }
}

@Composable
private fun SecurityHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = "O R I O N",
                color = OrionCyan,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp,
            )
            Text(
                text = "SECURE ACCESS NODE // 01",
                color = OrionTextMuted,
                fontSize = 9.sp,
                letterSpacing = 1.3.sp,
            )
        }
        Text(
            text = "[ LOCKED ]",
            color = OrionAmber,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
        )
    }
}

@Composable
private fun SecurityGrid() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val gridColor = OrionCyanMuted.copy(alpha = 0.12f)
        val step = 52.dp.toPx()
        var x = 0f
        while (x <= size.width) {
            drawLine(gridColor, start = androidx.compose.ui.geometry.Offset(x, 0f), end = androidx.compose.ui.geometry.Offset(x, size.height))
            x += step
        }
        var y = 0f
        while (y <= size.height) {
            drawLine(gridColor, start = androidx.compose.ui.geometry.Offset(0f, y), end = androidx.compose.ui.geometry.Offset(size.width, y))
            y += step
        }
    }
}

@Composable
private fun ScannerCore(
    uiState: AuthenticationUiState,
    progress: Float,
) {
    val accent = when (uiState) {
        is AuthenticationUiState.Error -> OrionError
        AuthenticationUiState.AccessGranted -> OrionCyan
        else -> OrionCyan
    }
    val successExpansion = if (uiState == AuthenticationUiState.AccessGranted) progress else 0f

    Canvas(
        modifier = Modifier
            .size(232.dp)
            .semantics { contentDescription = "本人認証スキャナー" },
    ) {
        val strokeWidth = 1.5.dp.toPx()
        val radius = min(size.width, size.height) / 2f
        val centerRadius = radius * 0.36f

        drawCircle(
            color = accent.copy(alpha = 0.08f + successExpansion * 0.10f),
            radius = centerRadius * (1f + successExpansion * 0.45f),
        )
        drawCircle(
            color = accent.copy(alpha = 0.55f),
            radius = radius * 0.72f,
            style = Stroke(width = strokeWidth),
        )
        drawCircle(
            color = accent.copy(alpha = 0.24f),
            radius = radius * (0.9f + successExpansion * 0.1f),
            style = Stroke(
                width = strokeWidth,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10.dp.toPx(), 8.dp.toPx())),
            ),
        )

        rotate(degrees = progress * 210f) {
            drawArc(
                color = accent,
                startAngle = -78f,
                sweepAngle = 112f,
                useCenter = false,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Square),
            )
            drawArc(
                color = accent.copy(alpha = 0.45f),
                startAngle = 102f,
                sweepAngle = 52f,
                useCenter = false,
                style = Stroke(width = strokeWidth),
            )
        }

        drawLine(
            color = accent.copy(alpha = 0.65f),
            start = androidx.compose.ui.geometry.Offset(center.x - radius, center.y),
            end = androidx.compose.ui.geometry.Offset(center.x - radius * 0.54f, center.y),
            strokeWidth = strokeWidth,
        )
        drawLine(
            color = accent.copy(alpha = 0.65f),
            start = androidx.compose.ui.geometry.Offset(center.x + radius * 0.54f, center.y),
            end = androidx.compose.ui.geometry.Offset(center.x + radius, center.y),
            strokeWidth = strokeWidth,
        )
        drawLine(
            color = accent.copy(alpha = 0.65f),
            start = androidx.compose.ui.geometry.Offset(center.x, center.y - radius),
            end = androidx.compose.ui.geometry.Offset(center.x, center.y - radius * 0.54f),
            strokeWidth = strokeWidth,
        )
        drawLine(
            color = accent.copy(alpha = 0.65f),
            start = androidx.compose.ui.geometry.Offset(center.x, center.y + radius * 0.54f),
            end = androidx.compose.ui.geometry.Offset(center.x, center.y + radius),
            strokeWidth = strokeWidth,
        )
    }
}

@Composable
private fun SecurityStatus(
    uiState: AuthenticationUiState,
    progress: Float,
) {
    val title: String
    val detail: String
    val color: Color
    when (uiState) {
        AuthenticationUiState.Locked -> {
            title = "SYSTEM LOCKED"
            detail = "SECURE CHANNEL STANDBY"
            color = OrionCyan
        }

        AuthenticationUiState.Booting -> {
            title = "SECURE BOOT"
            detail = when {
                progress < 0.34f -> "INITIALIZING LOCAL SECURITY CORE"
                progress < 0.68f -> "CALIBRATING IDENTITY SENSORS"
                else -> "BIOMETRIC CHANNEL READY"
            }
            color = OrionCyan
        }

        is AuthenticationUiState.Authenticating -> {
            title = "IDENTITY VERIFICATION"
            detail = if (uiState.failedAttempts == 0) {
                "AWAITING AUTHORIZATION SIGNAL"
            } else {
                "SIGNAL MISMATCH // RETRYING SCAN"
            }
            color = if (uiState.failedAttempts == 0) OrionCyan else OrionAmber
        }

        is AuthenticationUiState.Error -> {
            title = uiState.title
            detail = uiState.message
            color = OrionError
        }

        AuthenticationUiState.AccessGranted -> {
            title = "ACCESS GRANTED"
            detail = "IDENTITY CONFIRMED // WELCOME, COMMANDER"
            color = OrionCyan
        }

        AuthenticationUiState.Unlocked -> return
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            color = color,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.2.sp,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(9.dp))
        Text(
            text = detail,
            color = if (uiState is AuthenticationUiState.Error) OrionText else OrionTextMuted,
            fontSize = if (uiState is AuthenticationUiState.Error) 12.sp else 9.sp,
            letterSpacing = if (uiState is AuthenticationUiState.Error) 0.4.sp else 1.1.sp,
            lineHeight = 18.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SecurityFooter(
    uiState: AuthenticationUiState,
    onAuthenticationRequested: () -> Unit,
    onOpenSecuritySettings: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (uiState is AuthenticationUiState.Error) {
            val label = when (uiState.recoveryAction) {
                AuthenticationRecoveryAction.Retry -> "RETRY AUTHENTICATION"
                AuthenticationRecoveryAction.OpenSecuritySettings -> "OPEN DEVICE SECURITY"
            }
            CyberAction(
                label = label,
                modifier = Modifier.testTag(AUTHENTICATION_PRIMARY_ACTION_TAG),
                onClick = when (uiState.recoveryAction) {
                    AuthenticationRecoveryAction.Retry -> onAuthenticationRequested
                    AuthenticationRecoveryAction.OpenSecuritySettings -> onOpenSecuritySettings
                },
            )
            Spacer(modifier = Modifier.height(18.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "ENCRYPTION // LOCAL",
                color = OrionTextMuted,
                fontSize = 8.sp,
                letterSpacing = 1.sp,
            )
            Text(
                text = "NODE STATUS // ISOLATED",
                color = OrionTextMuted,
                fontSize = 8.sp,
                letterSpacing = 1.sp,
            )
        }
    }
}

@Composable
private fun CyberAction(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .border(1.dp, OrionCyan, CutCornerShape(topStart = 12.dp, bottomEnd = 12.dp))
            .background(OrionPanel.copy(alpha = 0.94f), CutCornerShape(topStart = 12.dp, bottomEnd = 12.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = label }
            .padding(horizontal = 18.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "[ $label ]",
            color = OrionCyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.3.sp,
        )
    }
}

private fun AuthenticationUiState.accessibilityState(): String = when (this) {
    AuthenticationUiState.Locked -> "システムロック中"
    AuthenticationUiState.Booting -> "セキュアブート中"
    is AuthenticationUiState.Authenticating -> "本人認証中"
    is AuthenticationUiState.Error -> "認証エラー: $message"
    AuthenticationUiState.AccessGranted -> "認証成功"
    AuthenticationUiState.Unlocked -> "ロック解除済み"
}

private const val BOOT_DURATION_MILLIS = 450
private const val UNLOCK_DURATION_MILLIS = 900

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun AuthenticationBootPreview() {
    OrionTheme {
        AuthenticationGate(
            uiState = AuthenticationUiState.Authenticating(),
            onAuthenticationRequested = {},
            onUnlockAnimationFinished = {},
            onOpenSecuritySettings = {},
            onClose = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun AuthenticationErrorPreview() {
    OrionTheme {
        AuthenticationGate(
            uiState = AuthenticationUiState.Error(
                title = "ACCESS ABORTED",
                message = "認証はキャンセルされました。",
                recoveryAction = AuthenticationRecoveryAction.Retry,
            ),
            onAuthenticationRequested = {},
            onUnlockAnimationFinished = {},
            onOpenSecuritySettings = {},
            onClose = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun AuthenticationGrantedPreview() {
    OrionTheme {
        AuthenticationGate(
            uiState = AuthenticationUiState.AccessGranted,
            onAuthenticationRequested = {},
            onUnlockAnimationFinished = {},
            onOpenSecuritySettings = {},
            onClose = {},
        )
    }
}
