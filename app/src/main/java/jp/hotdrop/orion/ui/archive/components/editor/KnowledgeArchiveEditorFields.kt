package jp.hotdrop.orion.ui.archive.components.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import jp.hotdrop.orion.model.KnowledgeArchiveValidationError
import jp.hotdrop.orion.ui.theme.OrionCyan
import jp.hotdrop.orion.ui.theme.OrionCyanMuted
import jp.hotdrop.orion.ui.theme.OrionError
import jp.hotdrop.orion.ui.theme.OrionPanel
import jp.hotdrop.orion.ui.theme.OrionPanelElevated
import jp.hotdrop.orion.ui.theme.OrionTextMuted
import jp.hotdrop.orion.ui.theme.OrionTheme

internal const val ArchiveTitleInputTag = "archive_title_input"
internal const val ArchiveUrlInputTag = "archive_url_input"
internal const val ArchiveMemoInputTag = "archive_memo_input"

@Composable
fun KnowledgeArchiveEditorFields(
    title: String,
    url: String,
    memo: String,
    validationError: KnowledgeArchiveValidationError?,
    enabled: Boolean,
    onTitleChanged: (String) -> Unit,
    onUrlChanged: (String) -> Unit,
    onMemoChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, OrionCyanMuted, CutCornerShape(topStart = 16.dp, bottomEnd = 16.dp))
            .background(OrionPanel.copy(alpha = 0.72f), CutCornerShape(topStart = 16.dp, bottomEnd = 16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChanged,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(ArchiveTitleInputTag)
                .semantics { contentDescription = "記事タイトル" },
            enabled = enabled,
            label = { Text("TITLE // REQUIRED") },
            isError = validationError == KnowledgeArchiveValidationError.TitleRequired,
            supportingText = if (validationError == KnowledgeArchiveValidationError.TitleRequired) {
                { Text("タイトルを入力してください。") }
            } else {
                null
            },
            singleLine = true,
            shape = CutCornerShape(topStart = 10.dp, bottomEnd = 10.dp),
            colors = archiveTextFieldColors(),
        )
        OutlinedTextField(
            value = url,
            onValueChange = onUrlChanged,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(ArchiveUrlInputTag)
                .semantics { contentDescription = "記事URL" },
            enabled = enabled,
            label = { Text("URL // HTTP OR HTTPS") },
            placeholder = { Text("https://example.com/article") },
            isError = validationError == KnowledgeArchiveValidationError.UrlRequired ||
                validationError == KnowledgeArchiveValidationError.UrlInvalid,
            supportingText = when (validationError) {
                KnowledgeArchiveValidationError.UrlRequired -> ({ Text("URLを入力してください。") })
                KnowledgeArchiveValidationError.UrlInvalid -> ({ Text("httpまたはhttpsのURLを入力してください。") })
                else -> null
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            shape = CutCornerShape(topStart = 10.dp, bottomEnd = 10.dp),
            colors = archiveTextFieldColors(),
        )
        OutlinedTextField(
            value = memo,
            onValueChange = onMemoChanged,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 150.dp)
                .testTag(ArchiveMemoInputTag)
                .semantics { contentDescription = "記事についてのメモ" },
            enabled = enabled,
            label = { Text("MEMO // OPTIONAL") },
            placeholder = { Text("気になった点、試したいこと、あとで深掘りする内容…") },
            minLines = 5,
            shape = CutCornerShape(topStart = 10.dp, bottomEnd = 10.dp),
            colors = archiveTextFieldColors(),
        )
    }
}

@Composable
private fun archiveTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = OrionCyan,
    unfocusedBorderColor = OrionCyanMuted,
    errorBorderColor = OrionError,
    focusedLabelColor = OrionCyan,
    unfocusedLabelColor = OrionTextMuted,
    cursorColor = OrionCyan,
    focusedContainerColor = OrionPanelElevated.copy(alpha = 0.45f),
    unfocusedContainerColor = OrionPanelElevated.copy(alpha = 0.25f),
    disabledContainerColor = OrionPanelElevated.copy(alpha = 0.2f),
)

@Preview
@Composable
private fun KnowledgeArchiveEditorFieldsPreview() {
    OrionTheme {
        KnowledgeArchiveEditorFields(
            title = "Composeの状態管理",
            url = "https://developer.android.com/develop/ui/compose/state",
            memo = "状態ホイスティングの例をあとで試す。",
            validationError = null,
            enabled = true,
            onTitleChanged = {},
            onUrlChanged = {},
            onMemoChanged = {},
        )
    }
}

@Preview
@Composable
private fun KnowledgeArchiveEditorFieldsValidationErrorPreview() {
    OrionTheme {
        KnowledgeArchiveEditorFields(
            title = "",
            url = "example.com",
            memo = "",
            validationError = KnowledgeArchiveValidationError.UrlInvalid,
            enabled = true,
            onTitleChanged = {},
            onUrlChanged = {},
            onMemoChanged = {},
        )
    }
}
