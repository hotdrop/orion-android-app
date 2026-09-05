package jp.hotdrop.orion.ui.incoming

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.runtime.mutableStateOf
import jp.hotdrop.orion.model.IncomingIntelligenceDocument
import jp.hotdrop.orion.ui.incoming.components.IncomingDocumentListTag
import jp.hotdrop.orion.ui.incoming.components.IncomingSyncButtonTag
import jp.hotdrop.orion.ui.incoming.uistate.IncomingIntelligenceUiState
import jp.hotdrop.orion.ui.theme.OrionTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class IncomingIntelligenceScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun personalActions_doNotOpenDocument_andFavoritesFilterWorks() {
        val first = IncomingIntelligenceDocument("first", "Compose notes", "09/05", "/", "https://example.com", false, true, "Useful notes")
        val second = first.copy(id = "second", title = "Other notes", isFavorite = false)
        val state = mutableStateOf(IncomingIntelligenceUiState(isDriveConfigured = true, documents = listOf(first, second)))
        var opens = 0
        var favorites = 0
        var edited: String? = null
        composeRule.setContent {
            OrionTheme {
                IncomingIntelligenceScreen(state.value, {}, {}, { opens++ },
                    onToggleFavorite = { favorites++ },
                    onEditMemo = { edited = it },
                    onFavoritesOnlyChanged = { enabled -> state.value = state.value.copy(favoritesOnly = enabled) },
                )
            }
        }
        composeRule.onNodeWithContentDescription("Compose notesのお気に入りを解除").performClick()
        composeRule.onNodeWithContentDescription("Compose notesの概要メモを編集").performClick()
        assertEquals(0, opens)
        assertEquals(1, favorites)
        assertEquals("first", edited)
        composeRule.onNodeWithText("★ / お気に入り").performClick()
        composeRule.onNodeWithText("Other notes").assertDoesNotExist()
        composeRule.onNodeWithText("Compose notes").assertIsDisplayed()
    }

    @Test
    fun retainedDocumentWithoutDrive_showsMemoAndDeleteConfirmation() {
        val document = IncomingIntelligenceDocument("retained", "同期対象外の技術資料", "09/05", "/", "https://example.com", false, true,
            "再コンポーズの計測方法と、最適化の判断基準を整理した資料。次の実装前に読み返す。", false)
        val state = mutableStateOf(IncomingIntelligenceUiState(documents = listOf(document)))
        var deletes = 0
        composeRule.setContent {
            OrionTheme {
                IncomingIntelligenceScreen(state.value, {}, {}, {},
                    onRequestDelete = { state.value = state.value.copy(pendingDelete = it) },
                    onConfirmDelete = { deletes++ },
                    onDismissDelete = { state.value = state.value.copy(pendingDelete = null) },
                )
            }
        }
        composeRule.onNodeWithText("同期対象外").assertIsDisplayed()
        composeRule.onNodeWithText("REMOVE").performClick()
        assertEquals(0, deletes)
        composeRule.onNodeWithText("ローカル記録を削除").assertIsDisplayed()
        composeRule.onNodeWithText("削除").performClick()
        assertEquals(1, deletes)
    }

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
            .onNodeWithContentDescription("Compose performance notesを文書アプリで開く")
            .performClick()
        assertEquals("first", openedId)
    }

    @Test
    fun ready_hidesPersistentStatusAndSyncsFromReactor() {
        var syncClicks = 0
        composeRule.setContent {
            OrionTheme {
                IncomingIntelligenceScreen(
                    uiState = IncomingIntelligenceUiState(
                        isDriveConfigured = true,
                        lastSyncedAtLabel = "08/01 09:45",
                    ),
                    onSync = { syncClicks++ },
                    onOpenSettings = {},
                    onOpenDocument = {},
                )
            }
        }

        composeRule.onAllNodesWithText("UPLINK // READY").assertCountEquals(0)
        composeRule.onNodeWithText("LAST // 08/01 09:45").assertIsDisplayed()
        composeRule.onNodeWithTag(IncomingSyncButtonTag).performClick()
        assertEquals(1, syncClicks)
    }

    @Test
    fun syncing_disablesSyncReactor() {
        composeRule.setContent {
            OrionTheme {
                IncomingIntelligenceScreen(
                    uiState = IncomingIntelligenceUiState(
                        isDriveConfigured = true,
                        isSyncing = true,
                    ),
                    onSync = {},
                    onOpenSettings = {},
                    onOpenDocument = {},
                )
            }
        }

        composeRule.onNodeWithText("UPLINK // RECEIVING").assertIsDisplayed()
        composeRule.onNodeWithTag(IncomingSyncButtonTag).assertIsNotEnabled()
    }
}
