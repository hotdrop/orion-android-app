package jp.hotdrop.orion

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import jp.hotdrop.orion.ui.settings.components.SelectDriveFolderButtonTag
import jp.hotdrop.orion.ui.archive.components.editor.ArchiveTitleInputTag
import org.junit.Rule
import org.junit.Test

class OrionNavigationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun navigation_switchesModulesAndReturnsFromSettings() {
        composeRule
            .onNodeWithContentDescription("現在の画面: INCOMING INTELLIGENCE")
            .assertIsDisplayed()
        composeRule.onNodeWithText("DRIVE TARGET // NOT CONFIGURED").assertIsDisplayed()

        composeRule
            .onNodeWithContentDescription("KNOWLEDGE ARCHIVEを開く")
            .performClick()
            .assertIsSelected()
        composeRule
            .onNodeWithContentDescription("現在の画面: KNOWLEDGE ARCHIVE")
            .assertIsDisplayed()
        composeRule.onNodeWithContentDescription("新しい記録を追加").performClick()
        composeRule
            .onNodeWithContentDescription("現在の画面: NEW KNOWLEDGE RECORD")
            .assertIsDisplayed()
        composeRule.onNodeWithTag(ArchiveTitleInputTag).assertIsDisplayed()
        composeRule.onNodeWithContentDescription("前の画面へ戻る").performClick()
        composeRule
            .onNodeWithContentDescription("現在の画面: KNOWLEDGE ARCHIVE")
            .assertIsDisplayed()

        composeRule.onNodeWithContentDescription("Settingsを開く").performClick()
        composeRule
            .onNodeWithContentDescription("現在の画面: SYSTEM SETTINGS")
            .assertIsDisplayed()
        composeRule.onNodeWithTag(SelectDriveFolderButtonTag).assertIsDisplayed()

        composeRule.onNodeWithContentDescription("前の画面へ戻る").performClick()
        composeRule
            .onNodeWithContentDescription("現在の画面: KNOWLEDGE ARCHIVE")
            .assertIsDisplayed()
        composeRule
            .onNodeWithContentDescription("KNOWLEDGE ARCHIVEを開く")
            .assertIsSelected()
    }
}
