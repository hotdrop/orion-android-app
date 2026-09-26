package jp.hotdrop.orion.ui.authentication.uistate

sealed interface BiometricAuthenticationResult {
    data object Success : BiometricAuthenticationResult
    data object AttemptFailed : BiometricAuthenticationResult
    data object DeviceSecurityRequired : BiometricAuthenticationResult
    data class Canceled(val message: String) : BiometricAuthenticationResult
    data class Unavailable(val message: String) : BiometricAuthenticationResult
}