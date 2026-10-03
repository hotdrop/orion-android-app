package jp.hotdrop.orion.ui.authentication.uistate

sealed interface AuthenticationUiState {
    data object Locked : AuthenticationUiState
    data object Booting : AuthenticationUiState
    data class Authenticating(val failedAttempts: Int = 0) : AuthenticationUiState

    data class Error(
        val title: String,
        val message: String,
        val recoveryAction: AuthenticationRecoveryAction,
    ) : AuthenticationUiState
    data object AccessGranted : AuthenticationUiState
    data object Unlocked : AuthenticationUiState
}

enum class AuthenticationRecoveryAction {
    Retry,
    OpenSecuritySettings,
}