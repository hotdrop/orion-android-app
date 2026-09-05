package jp.hotdrop.orion.ui.incoming.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import jp.hotdrop.orion.model.IncomingIntelligenceDocument

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
    val listState = rememberLazyListState()
    val lifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .testTag(IncomingDocumentListTag),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(
            items = documents,
            key = IncomingIntelligenceDocument::id,
        ) { document ->
            val isVisible by remember(listState, document.id) {
                derivedStateOf {
                    listState.layoutInfo.visibleItemsInfo.any { it.key == document.id }
                }
            }
            IncomingIntelligenceDocumentCard(
                playbackEnabled = isVisible && lifecycleState.isAtLeast(Lifecycle.State.RESUMED),
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
