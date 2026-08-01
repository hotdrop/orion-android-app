package jp.hotdrop.orion.ui.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import jp.hotdrop.orion.data.settings.GoogleDriveTarget
import jp.hotdrop.orion.ui.theme.OrionTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SettingsScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun unsetTarget_showsFolderSelectionAction() {
        var selectClicks = 0
        composeRule.setContent {
            OrionTheme {
                SettingsScreen(
                    uiState = SettingsUiState(isLoading = false),
                    onSelectFolder = { selectClicks++ },
                    onClearFolder = {},
                )
            }
        }

        composeRule.onNodeWithText("NOT CONFIGURED").assertIsDisplayed()
        composeRule.onNodeWithTag(SelectDriveFolderButtonTag).assertIsEnabled().performClick()
        assertEquals(1, selectClicks)
    }

    @Test
    fun connectedTarget_showsPathAndDisconnectAction() {
        var clearClicks = 0
        composeRule.setContent {
            OrionTheme {
                SettingsScreen(
                    uiState = SettingsUiState(
                        driveTarget = GoogleDriveTarget("folder-1", "ORION/Incoming"),
                        isLoading = false,
                    ),
                    onSelectFolder = {},
                    onClearFolder = { clearClicks++ },
                )
            }
        }

        composeRule.onNodeWithText("ORION/Incoming").assertIsDisplayed()
        composeRule.onNodeWithTag(ClearDriveFolderButtonTag).performClick()
        assertEquals(1, clearClicks)
    }
}
