package jp.hotdrop.orion.ui.incoming.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import jp.hotdrop.orion.model.IncomingIntelligenceDocument
import jp.hotdrop.orion.ui.theme.OrionTheme

internal const val IncomingDocumentListTag = "incoming_document_list"

@Composable
fun IncomingIntelligenceDocumentList(
    documents: List<IncomingIntelligenceDocument>,
    onOpenDocument: (IncomingIntelligenceDocument) -> Unit,
    modifier: Modifier = Modifier,
    onToggleFavorite: (String) -> Unit = {},
    onEditMemo: (String) -> Unit = {},
    onRequestDelete: (IncomingIntelligenceDocument) -> Unit = {},
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag(IncomingDocumentListTag),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(
            items = documents,
            key = IncomingIntelligenceDocument::id,
        ) { document ->
            IncomingIntelligenceDocumentCard(
                title = document.title,
                updatedAtLabel = document.updatedAtLabel,
                isFavorite = document.isFavorite,
                memo = document.memo,
                isSyncTarget = document.isSyncTarget,
                onToggleFavorite = { onToggleFavorite(document.id) },
                onEditMemo = { onEditMemo(document.id) },
                onDelete = { onRequestDelete(document) },
                isNew = document.isNew,
                onClick = { onOpenDocument(document) },
            )
        }
    }
}
