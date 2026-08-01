package jp.hotdrop.orion.ui.incoming

import androidx.compose.runtime.Immutable
import jp.hotdrop.orion.ui.incoming.uistate.IncomingIntelligenceUiState

@Immutable
internal data class IncomingIntelligenceStatusPresentation(
    val code: String,
    val description: String,
    val tone: IncomingIntelligenceStatusTone,
)

internal enum class IncomingIntelligenceStatusTone {
    Normal,
    Warning,
    Error,
}

internal fun IncomingIntelligenceUiState.toStatusPresentation(): IncomingIntelligenceStatusPresentation = when {
    !isDriveConfigured -> IncomingIntelligenceStatusPresentation(
        code = "UPLINK // STANDBY",
        description = "同期先が未設定です。",
        tone = IncomingIntelligenceStatusTone.Warning,
    )

    isSyncing -> IncomingIntelligenceStatusPresentation(
        code = "UPLINK // RECEIVING",
        description = "同期中です。保存済みの信号は引き続き参照できます。",
        tone = IncomingIntelligenceStatusTone.Normal,
    )

    errorMessage != null -> IncomingIntelligenceStatusPresentation(
        code = "UPLINK // ERROR",
        description = errorMessage,
        tone = IncomingIntelligenceStatusTone.Error,
    )

    isOffline -> IncomingIntelligenceStatusPresentation(
        code = "UPLINK // OFFLINE CACHE",
        description = "オフラインのため、最後に取得した信号を表示しています。",
        tone = IncomingIntelligenceStatusTone.Warning,
    )

    else -> IncomingIntelligenceStatusPresentation(
        code = "UPLINK // READY",
        description = "Google Drive同期チャネルは待機中です。",
        tone = IncomingIntelligenceStatusTone.Normal,
    )
}
