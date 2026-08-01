package jp.hotdrop.orion.ui.archive

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import jp.hotdrop.orion.data.archive.KnowledgeArchiveDraft
import jp.hotdrop.orion.data.archive.KnowledgeArchiveValidationError
import jp.hotdrop.orion.ui.theme.OrionTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class KnowledgeArchiveEditorScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun invalidTitle_isDisplayedAndSaveEventIsAvailable() {
        var saveClicked = false
        composeRule.setContent {
            OrionTheme {
                KnowledgeArchiveEditorScreen(
                    uiState = KnowledgeArchiveEditorUiState(
                        url = "https://example.com",
                        validationError = KnowledgeArchiveValidationError.TitleRequired,
                    ),
                    onTitleChanged = {},
                    onUrlChanged = {},
                    onMemoChanged = {},
                    onSave = { saveClicked = true },
                    onRequestDelete = {},
                    onDismissDiscard = {},
                    onConfirmDiscard = {},
                    onDismissDelete = {},
                    onConfirmDelete = {},
                )
            }
        }

        composeRule.onNodeWithText("タイトルを入力してください。").assertIsDisplayed()
        composeRule.onNodeWithTag(SaveArchiveEntryButtonTag).assertIsEnabled().performClick()
        assertTrue(saveClicked)
    }

    @Test
    fun editState_confirmsDeleteBeforeEmittingDelete() {
        var deleteConfirmed = false
        composeRule.setContent {
            OrionTheme {
                KnowledgeArchiveEditorScreen(
                    uiState = KnowledgeArchiveEditorUiState(
                        entryId = 4,
                        title = "Title",
                        url = "https://example.com",
                        originalDraft = KnowledgeArchiveDraft("Title", "https://example.com", ""),
                        showDeleteConfirmation = true,
                    ),
                    onTitleChanged = {},
                    onUrlChanged = {},
                    onMemoChanged = {},
                    onSave = {},
                    onRequestDelete = {},
                    onDismissDiscard = {},
                    onConfirmDiscard = {},
                    onDismissDelete = {},
                    onConfirmDelete = { deleteConfirmed = true },
                )
            }
        }

        composeRule.onNodeWithText("DELETE RECORD?").assertIsDisplayed()
        composeRule.onNodeWithText("[ DELETE ]").performClick()
        assertTrue(deleteConfirmed)
    }

    @Test
    fun dirtyBackState_confirmsDiscard() {
        var discardConfirmed = false
        composeRule.setContent {
            OrionTheme {
                KnowledgeArchiveEditorScreen(
                    uiState = KnowledgeArchiveEditorUiState(
                        memo = "draft",
                        showDiscardConfirmation = true,
                    ),
                    onTitleChanged = {},
                    onUrlChanged = {},
                    onMemoChanged = {},
                    onSave = {},
                    onRequestDelete = {},
                    onDismissDiscard = {},
                    onConfirmDiscard = { discardConfirmed = true },
                    onDismissDelete = {},
                    onConfirmDelete = {},
                )
            }
        }

        composeRule.onNodeWithText("DISCARD CHANGES?").assertIsDisplayed()
        composeRule.onNodeWithText("[ DISCARD ]").performClick()
        assertTrue(discardConfirmed)
    }
}
