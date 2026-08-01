package jp.hotdrop.orion.ui.incoming

import jp.hotdrop.orion.model.IncomingIntelligenceDocument

data class IncomingIntelligenceUiState(
    val isDriveConfigured: Boolean = false,
    val documents: List<IncomingIntelligenceDocument> = emptyList(),
    val isSyncing: Boolean = false,
    val isOffline: Boolean = false,
    val errorMessage: String? = null,
    val lastSyncedAtLabel: String? = null,
)