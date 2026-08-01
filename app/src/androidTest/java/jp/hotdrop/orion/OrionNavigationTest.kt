package jp.hotdrop.orion

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.platform.testTag
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import jp.hotdrop.orion.navigation.OrionDestination
import jp.hotdrop.orion.navigation.OrionTopLevelDestination
import jp.hotdrop.orion.ui.OrionAppShell
import jp.hotdrop.orion.ui.archive.components.editor.ArchiveTitleInputTag
import jp.hotdrop.orion.ui.settings.components.SelectDriveFolderButtonTag
import jp.hotdrop.orion.ui.theme.OrionTheme
import org.junit.Rule
import org.junit.Test

class OrionNavigationTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun navigation_switchesModulesAndReturnsFromSettings() {
        var selectedDestination by mutableStateOf(OrionTopLevelDestination.Incoming)
        composeRule.setContent {
            OrionTheme {
                OrionAppShell(
                    selectedDestination = selectedDestination,
                    onDestinationSelected = { selectedDestination = it },
                    navHost = { navController, modifier ->
                        NavHost(
                            navController = navController,
                            startDestination = selectedDestination.route,
                            modifier = modifier,
                        ) {
                            composable(OrionTopLevelDestination.Incoming.route) {
                                Text("DRIVE TARGET // NOT CONFIGURED")
                            }
                            composable(OrionTopLevelDestination.Archive.route) {
                                Column {
                                    Text("ARCHIVE CONTENT")
                                    Text(
                                        text = "NEW",
                                        modifier = Modifier
                                            .semantics { contentDescription = "新しい記録を追加" }
                                            .clickable {
                                                navController.navigate(OrionDestination.ArchiveNewRoute)
                                            },
                                    )
                                }
                            }
                            composable(OrionDestination.ArchiveNewRoute) {
                                Text("TITLE", modifier = Modifier.testTag(ArchiveTitleInputTag))
                            }
                            composable(OrionDestination.SettingsRoute) {
                                Text(
                                    text = "SELECT DRIVE FOLDER",
                                    modifier = Modifier.testTag(SelectDriveFolderButtonTag),
                                )
                            }
                        }
                    },
                )
            }
        }

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
