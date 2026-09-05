package jp.hotdrop.orion.ui.incoming

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import jp.hotdrop.orion.ui.theme.OrionTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class IncomingMemoScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun memoCanBeEditedAndSaved_withoutLosingInputOnFailure() {
        val state = mutableStateOf(IncomingMemoUiState(title = "Composeパフォーマンスの実践ガイド", isLoading = false, canSave = true))
        var saved = ""
        compose.setContent {
            OrionTheme {
                IncomingMemoScreen(state.value,
                    onMemoChanged = { state.value = state.value.copy(memo = it) },
                    onSave = { saved = state.value.memo; state.value = state.value.copy(error = "保存できませんでした。") },
                    onRetry = {}, onDismissDiscard = {}, onConfirmDiscard = {},
                )
            }
        }
        compose.onNodeWithText("概要メモ").performTextReplacement("重要な資料\n次回の実装で参照する")
        compose.onNodeWithText("SAVE").performClick()
        assertEquals("重要な資料\n次回の実装で参照する", saved)
        compose.onNodeWithText("保存できませんでした。").assertIsDisplayed()
        compose.onNodeWithText(saved).assertIsDisplayed()

    }
}
