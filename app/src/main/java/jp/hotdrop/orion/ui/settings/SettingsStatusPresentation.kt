package jp.hotdrop.orion.ui.settings

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import jp.hotdrop.orion.R
import jp.hotdrop.orion.ui.settings.uistate.SettingsFeedback
import jp.hotdrop.orion.ui.settings.uistate.SettingsOperation
import jp.hotdrop.orion.ui.settings.uistate.SettingsUiState

@Immutable
internal data class SettingsStatusPresentation(
    @param:StringRes val codeRes: Int,
    @param:StringRes val messageRes: Int,
    val tone: SettingsStatusTone,
)

internal enum class SettingsStatusTone {
    Normal,
    Warning,
    Error,
}

internal fun SettingsUiState.toStatusPresentation(): SettingsStatusPresentation = when {
    operation == SettingsOperation.Loading -> SettingsStatusPresentation(
        codeRes = R.string.settings_status_loading_code,
        messageRes = R.string.settings_status_loading_message,
        tone = SettingsStatusTone.Normal,
    )

    operation == SettingsOperation.AuthorizingDrive -> SettingsStatusPresentation(
        codeRes = R.string.settings_status_selecting_code,
        messageRes = R.string.settings_status_selecting_message,
        tone = SettingsStatusTone.Normal,
    )

    operation == SettingsOperation.BrowsingFolders -> SettingsStatusPresentation(
        codeRes = R.string.settings_status_browsing_code,
        messageRes = R.string.settings_status_browsing_message,
        tone = SettingsStatusTone.Normal,
    )

    operation == SettingsOperation.SavingFolder -> SettingsStatusPresentation(
        codeRes = R.string.settings_status_saving_code,
        messageRes = R.string.settings_status_saving_message,
        tone = SettingsStatusTone.Normal,
    )

    operation == SettingsOperation.Clearing -> SettingsStatusPresentation(
        codeRes = R.string.settings_status_clearing_code,
        messageRes = R.string.settings_status_clearing_message,
        tone = SettingsStatusTone.Normal,
    )

    feedback == SettingsFeedback.FolderSaved -> SettingsStatusPresentation(
        codeRes = R.string.settings_status_saved_code,
        messageRes = R.string.settings_status_saved_message,
        tone = SettingsStatusTone.Normal,
    )

    feedback == SettingsFeedback.Cleared -> SettingsStatusPresentation(
        codeRes = R.string.settings_status_cleared_code,
        messageRes = R.string.settings_status_cleared_message,
        tone = SettingsStatusTone.Warning,
    )

    feedback == SettingsFeedback.SelectionFailed -> SettingsStatusPresentation(
        codeRes = R.string.settings_status_selection_failed_code,
        messageRes = R.string.settings_status_selection_failed_message,
        tone = SettingsStatusTone.Error,
    )

    feedback == SettingsFeedback.ClearFailed -> SettingsStatusPresentation(
        codeRes = R.string.settings_status_clear_failed_code,
        messageRes = R.string.settings_status_clear_failed_message,
        tone = SettingsStatusTone.Error,
    )

    feedback == SettingsFeedback.LoadFailed -> SettingsStatusPresentation(
        codeRes = R.string.settings_status_load_failed_code,
        messageRes = R.string.settings_status_load_failed_message,
        tone = SettingsStatusTone.Error,
    )

    driveTarget == null -> SettingsStatusPresentation(
        codeRes = R.string.settings_status_not_configured_code,
        messageRes = R.string.settings_status_not_configured_message,
        tone = SettingsStatusTone.Warning,
    )

    else -> SettingsStatusPresentation(
        codeRes = R.string.settings_status_ready_code,
        messageRes = R.string.settings_status_ready_message,
        tone = SettingsStatusTone.Normal,
    )
}
