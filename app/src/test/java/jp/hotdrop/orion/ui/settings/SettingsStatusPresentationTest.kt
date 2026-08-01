package jp.hotdrop.orion.ui.settings

import jp.hotdrop.orion.R
import jp.hotdrop.orion.model.GoogleDriveTarget
import jp.hotdrop.orion.ui.settings.uistate.SettingsFeedback
import jp.hotdrop.orion.ui.settings.uistate.SettingsOperation
import jp.hotdrop.orion.ui.settings.uistate.SettingsUiState
import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsStatusPresentationTest {
    @Test
    fun uiStates_mapToExpectedPresentation() {
        val target = GoogleDriveTarget("folder-1", "Incoming")
        val cases = listOf(
            StatusCase(
                state = SettingsUiState(),
                codeRes = R.string.settings_status_loading_code,
                messageRes = R.string.settings_status_loading_message,
                tone = SettingsStatusTone.Normal,
            ),
            StatusCase(
                state = SettingsUiState(operation = SettingsOperation.SelectingFolder),
                codeRes = R.string.settings_status_selecting_code,
                messageRes = R.string.settings_status_selecting_message,
                tone = SettingsStatusTone.Normal,
            ),
            StatusCase(
                state = SettingsUiState(operation = SettingsOperation.Clearing),
                codeRes = R.string.settings_status_clearing_code,
                messageRes = R.string.settings_status_clearing_message,
                tone = SettingsStatusTone.Normal,
            ),
            StatusCase(
                state = idleState(target, SettingsFeedback.FolderSaved),
                codeRes = R.string.settings_status_saved_code,
                messageRes = R.string.settings_status_saved_message,
                tone = SettingsStatusTone.Normal,
            ),
            StatusCase(
                state = idleState(feedback = SettingsFeedback.Cleared),
                codeRes = R.string.settings_status_cleared_code,
                messageRes = R.string.settings_status_cleared_message,
                tone = SettingsStatusTone.Warning,
            ),
            StatusCase(
                state = idleState(feedback = SettingsFeedback.SelectionFailed),
                codeRes = R.string.settings_status_selection_failed_code,
                messageRes = R.string.settings_status_selection_failed_message,
                tone = SettingsStatusTone.Error,
            ),
            StatusCase(
                state = idleState(target, SettingsFeedback.ClearFailed),
                codeRes = R.string.settings_status_clear_failed_code,
                messageRes = R.string.settings_status_clear_failed_message,
                tone = SettingsStatusTone.Error,
            ),
            StatusCase(
                state = idleState(feedback = SettingsFeedback.LoadFailed),
                codeRes = R.string.settings_status_load_failed_code,
                messageRes = R.string.settings_status_load_failed_message,
                tone = SettingsStatusTone.Error,
            ),
            StatusCase(
                state = idleState(),
                codeRes = R.string.settings_status_not_configured_code,
                messageRes = R.string.settings_status_not_configured_message,
                tone = SettingsStatusTone.Warning,
            ),
            StatusCase(
                state = idleState(target),
                codeRes = R.string.settings_status_ready_code,
                messageRes = R.string.settings_status_ready_message,
                tone = SettingsStatusTone.Normal,
            ),
        )

        cases.forEach { case ->
            val presentation = case.state.toStatusPresentation()
            assertEquals(case.codeRes, presentation.codeRes)
            assertEquals(case.messageRes, presentation.messageRes)
            assertEquals(case.tone, presentation.tone)
        }
    }

    private fun idleState(
        target: GoogleDriveTarget? = null,
        feedback: SettingsFeedback = SettingsFeedback.None,
    ) = SettingsUiState(
        driveTarget = target,
        operation = SettingsOperation.Idle,
        feedback = feedback,
    )
}

private data class StatusCase(
    val state: SettingsUiState,
    val codeRes: Int,
    val messageRes: Int,
    val tone: SettingsStatusTone,
)
