package jp.hotdrop.orion.ui.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import jp.hotdrop.orion.model.GoogleDriveTarget
import jp.hotdrop.orion.ui.settings.components.ClearDriveFolderButtonTag
import jp.hotdrop.orion.ui.settings.components.SelectDriveFolderButtonTag
import jp.hotdrop.orion.ui.settings.uistate.SettingsFeedback
import jp.hotdrop.orion.ui.settings.uistate.SettingsOperation
import jp.hotdrop.orion.ui.settings.uistate.SettingsUiState
import jp.hotdrop.orion.ui.theme.OrionTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SettingsScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun loading_showsLoadingTargetAndDisablesSelection() {
        composeRule.setContent {
            OrionTheme {
                SettingsScreen(
                    uiState = SettingsUiState(),
                    onSelectFolder = {},
                    onClearFolder = {},
                )
            }
        }

        composeRule.onNodeWithText("LOADING…").assertIsDisplayed()
        composeRule.onNodeWithTag(SelectDriveFolderButtonTag).assertIsNotEnabled()
        composeRule.onNodeWithText("保存済み設定を読み込んでいます。").assertIsDisplayed()
    }

    @Test
    fun unsetTarget_showsFolderSelectionAction() {
        var selectClicks = 0
        composeRule.setContent {
            OrionTheme {
                SettingsScreen(
                    uiState = SettingsUiState(operation = SettingsOperation.Idle),
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
                        operation = SettingsOperation.Idle,
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

    @Test
    fun selectingFolder_disablesActionAndAnnouncesActiveStatus() {
        composeRule.setContent {
            OrionTheme {
                SettingsScreen(
                    uiState = SettingsUiState(
                        operation = SettingsOperation.SelectingFolder,
                    ),
                    onSelectFolder = {},
                    onClearFolder = {},
                )
            }
        }

        composeRule.onNodeWithTag(SelectDriveFolderButtonTag).assertIsNotEnabled()
        composeRule.onNodeWithText("DRIVE AUTH // ACTIVE").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Google Driveフォルダを選択").assertIsDisplayed()
    }

    @Test
    fun clearFailure_showsDedicatedError() {
        composeRule.setContent {
            OrionTheme {
                SettingsScreen(
                    uiState = SettingsUiState(
                        driveTarget = GoogleDriveTarget("folder-1", "ORION/Incoming"),
                        operation = SettingsOperation.Idle,
                        feedback = SettingsFeedback.ClearFailed,
                    ),
                    onSelectFolder = {},
                    onClearFolder = {},
                )
            }
        }

        composeRule.onNodeWithText("DRIVE TARGET // ERROR").assertIsDisplayed()
        composeRule.onNodeWithText(
            "Google Driveフォルダの設定を解除できませんでした。再試行してください。",
        ).assertIsDisplayed()
    }
}
