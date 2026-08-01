package jp.hotdrop.orion.ui.archive.components.list

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.hotdrop.orion.ui.theme.OrionCyan
import jp.hotdrop.orion.ui.theme.OrionCyanMuted
import jp.hotdrop.orion.ui.theme.OrionDeepNavy
import jp.hotdrop.orion.ui.theme.OrionPanel
import jp.hotdrop.orion.ui.theme.OrionTextMuted
import jp.hotdrop.orion.ui.theme.OrionTheme

internal const val NewArchiveEntryButtonTag = "new_archive_entry_button"

@Composable
fun KnowledgeArchiveListHeader(
    entryCount: Int,
    onCreateEntry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, OrionCyanMuted, CutCornerShape(topStart = 16.dp, bottomEnd = 16.dp))
            .background(OrionPanel.copy(alpha = 0.82f), CutCornerShape(topStart = 16.dp, bottomEnd = 16.dp))
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "MODULE // KA",
                    color = OrionCyan,
                    fontSize = 10.sp,
                    letterSpacing = 1.4.sp,
                )
                Text(
                    text = "LOCAL KNOWLEDGE NODE",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.1.sp,
                )
                Text(
                    text = "$entryCount RECORDS // LOCAL ONLY",
                    color = OrionTextMuted,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp,
                )
            }
            Button(
                onClick = onCreateEntry,
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag(NewArchiveEntryButtonTag)
                    .semantics { contentDescription = "新しい記録を追加" },
                shape = CutCornerShape(topStart = 8.dp, bottomEnd = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OrionCyan,
                    contentColor = OrionDeepNavy,
                ),
            ) {
                Text("[ NEW ]", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun KnowledgeArchiveListHeaderPreview() {
    OrionTheme {
        KnowledgeArchiveListHeader(
            entryCount = 12,
            onCreateEntry = {},
        )
    }
}
