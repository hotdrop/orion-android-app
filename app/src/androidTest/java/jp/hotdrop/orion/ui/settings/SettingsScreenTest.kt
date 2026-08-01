package jp.hotdrop.orion.ui.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import jp.hotdrop.orion.ui.theme.OrionTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SettingsScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun savedPath_isDisplayedAndSaveIsDisabledWhenUnchanged() {
        composeRule.setContent {
            OrionTheme {
                SettingsScreen(
                    uiState = SettingsUiState(
                        googleDrivePath = "ORION/Incoming",
                        savedGoogleDrivePath = "ORION/Incoming",
                        isLoading = false,
                    ),
                    onGoogleDrivePathChanged = {},
                    onSave = {},
                )
            }
        }

        composeRule.onNodeWithTag(GoogleDrivePathInputTag)
            .assertTextContains("ORION/Incoming")
        composeRule.onNodeWithTag(SaveSettingsButtonTag).assertIsNotEnabled()
        composeRule.onNodeWithText("保存済みのGoogle Driveフォルダを使用します。").assertIsDisplayed()
    }

    @Test
    fun editedPath_emitsChangeAndSaveEvents() {
        val changedValues = mutableListOf<String>()
        var saveClicked = false
        composeRule.setContent {
            OrionTheme {
                SettingsScreen(
                    uiState = SettingsUiState(
                        googleDrivePath = "ORION/New",
                        savedGoogleDrivePath = "ORION/Old",
                        isLoading = false,
                    ),
                    onGoogleDrivePathChanged = changedValues::add,
                    onSave = { saveClicked = true },
                )
            }
        }

        composeRule.onNodeWithTag(GoogleDrivePathInputTag).performTextReplacement("ORION/Reports")
        composeRule.onNodeWithTag(SaveSettingsButtonTag).assertIsEnabled().performClick()

        assertEquals(listOf("ORION/Reports"), changedValues)
        assertTrue(saveClicked)
    }

    @Test
    fun saveFailure_isDisplayedWithoutDisablingRetry() {
        composeRule.setContent {
            OrionTheme {
                SettingsScreen(
                    uiState = SettingsUiState(
                        googleDrivePath = "ORION/New",
                        savedGoogleDrivePath = "ORION/Old",
                        isLoading = false,
                        feedback = SettingsFeedback.SaveFailed,
                    ),
                    onGoogleDrivePathChanged = {},
                    onSave = {},
                )
            }
        }

        composeRule.onNodeWithText("保存できませんでした。入力内容を維持したまま再試行できます。")
            .assertIsDisplayed()
        composeRule.onNodeWithTag(SaveSettingsButtonTag).assertIsEnabled()
    }
}
