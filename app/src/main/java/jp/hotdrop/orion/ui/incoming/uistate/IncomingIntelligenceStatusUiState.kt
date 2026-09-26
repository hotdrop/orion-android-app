package jp.hotdrop.orion.ui.incoming.uistate

data class IncomingIntelligenceStatusUiState(
    val code: String,
    val description: String,
    val tone: IncomingIntelligenceStatusToneEnum,
)
