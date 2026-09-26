package jp.hotdrop.orion.ui.incoming.uistate

import jp.hotdrop.orion.model.IncomingIntelligenceDocument

data class IncomingIntelligenceUiState(
    val isDriveConfigured: Boolean = false,
    val documents: List<IncomingIntelligenceDocument> = emptyList(),
    val favoritesOnly: Boolean = false,
    val pendingDelete: IncomingIntelligenceDocument? = null,
    val isDeleting: Boolean = false,
    val isSyncing: Boolean = false,
    val isOffline: Boolean = false,
    val actionErrorMessage: String? = null,
    val errorMessage: String? = null,
    val lastSyncedAtLabel: String? = null,
) {
    fun toStatusUiState(): IncomingIntelligenceStatusUiState? = when {
        !isDriveConfigured -> IncomingIntelligenceStatusUiState(
            code = "UPLINK STANDBY",
            description = "同期先が未設定です。",
            tone = IncomingIntelligenceStatusToneEnum.Warning,
        )
        isSyncing -> IncomingIntelligenceStatusUiState(
            code = "UPLINK RECEIVING",
            description = "同期中です。保存済みの信号は引き続き参照できます。",
            tone = IncomingIntelligenceStatusToneEnum.Normal,
        )
        errorMessage != null -> IncomingIntelligenceStatusUiState(
            code = "UPLINK ERROR",
            description = errorMessage,
            tone = IncomingIntelligenceStatusToneEnum.Error,
        )
        isOffline -> IncomingIntelligenceStatusUiState(
            code = "UPLINK OFFLINE CACHE",
            description = "オフラインのため、最後に取得した信号を表示しています。",
            tone = IncomingIntelligenceStatusToneEnum.Warning,
        )
        else -> null
    }
}