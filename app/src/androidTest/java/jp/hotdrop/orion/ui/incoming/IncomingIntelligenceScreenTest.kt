package jp.hotdrop.orion.ui.incoming

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import jp.hotdrop.orion.model.IncomingIntelligenceDocument
import jp.hotdrop.orion.ui.incoming.uistate.IncomingIntelligenceUiState
import jp.hotdrop.orion.ui.theme.OrionTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class IncomingIntelligenceScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun notConfigured_showsConfigAction() {
        var configClicks = 0
        composeRule.setContent {
            OrionTheme {
                IncomingIntelligenceScreen(
                    uiState = IncomingIntelligenceUiState(),
                    onSync = {},
                    onOpenSettings = { configClicks++ },
                    onOpenDocument = {},
                )
            }
        }

        composeRule.onNodeWithText("DRIVE TARGET // NOT CONFIGURED").assertIsDisplayed()
        composeRule
            .onNodeWithContentDescription("Settingsを開いてGoogle Driveを設定")
            .performClick()
        assertEquals(1, configClicks)
    }

    @Test
    fun populated_showsCachedDocumentsAndOpensSelectedDocument() {
        val first = IncomingIntelligenceDocument(
            id = "first",
            title = "Compose performance notes",
            updatedAtLabel = "08/01 09:42",
            relativePath = "Android/Compose",
            webUrl = "https://docs.google.com/document/d/first",
            isNew = true,
        )
        val second = first.copy(id = "second", title = "RAG architecture", isNew = false)
        var openedId: String? = null
        composeRule.setContent {
            OrionTheme {
                IncomingIntelligenceScreen(
                    uiState = IncomingIntelligenceUiState(
                        isDriveConfigured = true,
                        documents = listOf(first, second),
                        isOffline = true,
                    ),
                    onSync = {},
                    onOpenSettings = {},
                    onOpenDocument = { openedId = it.id },
                )
            }
        }

        composeRule.onNodeWithTag(IncomingDocumentListTag).assertIsDisplayed()
        composeRule.onNodeWithText("UPLINK // OFFLINE CACHE").assertIsDisplayed()
        composeRule.onNodeWithTag(IncomingSyncButtonTag).assertIsEnabled()
        composeRule
            .onNodeWithContentDescription("Compose performance notesをGoogleドキュメントで開く")
            .performClick()
        assertEquals("first", openedId)
    }
}
