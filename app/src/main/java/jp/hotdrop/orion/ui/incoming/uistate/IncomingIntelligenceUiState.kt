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
)