package jp.hotdrop.orion.ui.archive.uistate

import jp.hotdrop.orion.model.KnowledgeArchiveEntry

data class KnowledgeArchiveUiState(
    val entries: List<KnowledgeArchiveEntry> = emptyList(),
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
    val urlOpenFailed: Boolean = false,
)