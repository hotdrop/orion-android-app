package jp.hotdrop.orion.ui.archive

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import jp.hotdrop.orion.data.archive.KnowledgeArchiveEntry
import jp.hotdrop.orion.ui.theme.OrionTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class KnowledgeArchiveScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun emptyState_showsCaptureAction() {
        var createClicked = false
        composeRule.setContent {
            OrionTheme {
                KnowledgeArchiveScreen(
                    uiState = KnowledgeArchiveUiState(isLoading = false),
                    onCreateEntry = { createClicked = true },
                    onEditEntry = {},
                    onOpenUrl = {},
                    onDismissUrlError = {},
                )
            }
        }

        composeRule.onNodeWithText("NO KNOWLEDGE RECORDS").assertIsDisplayed()
        composeRule.onNodeWithTag(NewArchiveEntryButtonTag).performClick()
        assertTrue(createClicked)
    }

    @Test
    fun entry_exposesSeparateOpenAndEditActions() {
        var editedId: Long? = null
        val openedUrls = mutableListOf<String>()
        val entry = KnowledgeArchiveEntry(
            id = 9,
            title = "Compose State",
            url = "https://developer.android.com/compose",
            memo = "Memo",
            createdAt = 1,
            updatedAt = 2,
        )
        composeRule.setContent {
            OrionTheme {
                KnowledgeArchiveScreen(
                    uiState = KnowledgeArchiveUiState(entries = listOf(entry), isLoading = false),
                    onCreateEntry = {},
                    onEditEntry = { editedId = it },
                    onOpenUrl = openedUrls::add,
                    onDismissUrlError = {},
                )
            }
        }

        composeRule.onNodeWithContentDescription("Compose StateのURLを開く").performClick()
        composeRule.onNodeWithContentDescription("Compose Stateを編集").performClick()

        assertEquals(listOf(entry.url), openedUrls)
        assertEquals(9L, editedId)
    }
}
