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
                relativePath = document.relativePath,
                isNew = document.isNew,
                onClick = { onOpenDocument(document) },
            )
        }
    }
}

@Preview
@Composable
private fun IncomingIntelligenceDocumentListPreview() {
    OrionTheme {
        IncomingIntelligenceDocumentList(
            documents = listOf(
                IncomingIntelligenceDocument(
                    id = "compose-performance",
                    title = "Jetpack Composeの描画パフォーマンスを安定させるための実践ガイド",
                    updatedAtLabel = "08/01 09:42",
                    relativePath = "Android/Compose/Weekly",
                    webUrl = "https://docs.google.com/document/d/compose-performance",
                    isNew = true,
                ),
            ),
            onOpenDocument = {},
        )
    }
}
