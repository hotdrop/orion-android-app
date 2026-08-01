package jp.hotdrop.orion.ui.authentication

import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import jp.hotdrop.orion.ui.theme.OrionTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class OrionSecureRootTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun lockedState_hidesProtectedContentFromSemantics() {
        composeRule.setContent {
            OrionTheme {
                OrionSecureRoot(
                    uiState = AuthenticationUiState.Authenticating(),
                    onAuthenticationRequested = {},
                    onUnlockAnimationFinished = {},
                    onOpenSecuritySettings = {},
                    onClose = {},
                ) {
                    Text("PROTECTED CONTENT")
                }
            }
        }

        composeRule.onNodeWithTag(AUTHENTICATION_GATE_TAG).assertIsDisplayed()
        composeRule.onAllNodesWithText("PROTECTED CONTENT").assertCountEquals(0)
        composeRule.onNodeWithText("IDENTITY VERIFICATION").assertIsDisplayed()
    }

    @Test
    fun retryAction_requestsAuthentication() {
        var requested = false
        composeRule.setContent {
            OrionTheme {
                OrionSecureRoot(
                    uiState = AuthenticationUiState.Error(
                        title = "ACCESS ABORTED",
                        message = "認証はキャンセルされました。",
                        recoveryAction = AuthenticationRecoveryAction.Retry,
                    ),
                    onAuthenticationRequested = { requested = true },
                    onUnlockAnimationFinished = {},
                    onOpenSecuritySettings = {},
                    onClose = {},
                ) { }
            }
        }

        composeRule.onNodeWithTag(AUTHENTICATION_PRIMARY_ACTION_TAG).performClick()

        composeRule.runOnIdle { assertTrue(requested) }
    }

    @Test
    fun unlockedState_exposesProtectedContent() {
        composeRule.setContent {
            OrionTheme {
                OrionSecureRoot(
                    uiState = AuthenticationUiState.Unlocked,
                    onAuthenticationRequested = {},
                    onUnlockAnimationFinished = {},
                    onOpenSecuritySettings = {},
                    onClose = {},
                ) {
                    Text("PROTECTED CONTENT")
                }
            }
        }

        composeRule.onNodeWithText("PROTECTED CONTENT").assertIsDisplayed()
        composeRule.onAllNodesWithTag(AUTHENTICATION_GATE_TAG).assertCountEquals(0)
    }

    @Test
    fun bootAndUnlockAnimations_emitCompletionEvents() {
        var authenticationRequested = false
        var unlockFinished = false
        var uiState by mutableStateOf<AuthenticationUiState>(AuthenticationUiState.Booting)
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            OrionTheme {
                AuthenticationGate(
                    uiState = uiState,
                    onAuthenticationRequested = { authenticationRequested = true },
                    onUnlockAnimationFinished = { unlockFinished = true },
                    onOpenSecuritySettings = {},
                    onClose = {},
                )
            }
        }

        composeRule.mainClock.advanceTimeBy(BOOT_TEST_ADVANCE_MILLIS)
        composeRule.runOnIdle { assertTrue(authenticationRequested) }

        composeRule.runOnIdle {
            uiState = AuthenticationUiState.AccessGranted
        }
        composeRule.mainClock.advanceTimeBy(UNLOCK_TEST_ADVANCE_MILLIS)
        composeRule.runOnIdle { assertTrue(unlockFinished) }
    }

    private companion object {
        const val BOOT_TEST_ADVANCE_MILLIS = 600L
        const val UNLOCK_TEST_ADVANCE_MILLIS = 1_100L
    }
}
