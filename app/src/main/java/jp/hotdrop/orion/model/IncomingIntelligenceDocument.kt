package jp.hotdrop.orion.model

import androidx.compose.runtime.Immutable

@Immutable
data class IncomingIntelligenceDocument(
    val id: String,
    val title: String,
    val updatedAtLabel: String,
    val relativePath: String,
    val webUrl: String,
    val isNew: Boolean,
)