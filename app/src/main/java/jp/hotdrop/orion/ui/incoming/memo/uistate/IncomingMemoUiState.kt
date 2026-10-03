package jp.hotdrop.orion.ui.incoming.memo.uistate

data class IncomingMemoUiState(
    val title: String = "",
    val memo: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val canSave: Boolean = false,
    val showDiscard: Boolean = false,
    val error: String? = null,
)
