package jp.hotdrop.orion.ui.authentication

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class AuthenticationViewModel @Inject constructor() : ViewModel() {
    private val mutableUiState = MutableStateFlow<AuthenticationUiState>(AuthenticationUiState.Locked)
    val uiState: StateFlow<AuthenticationUiState> = mutableUiState.asStateFlow()

    private var isForeground = false

    fun onForeground() {
        isForeground = true
        if (mutableUiState.value == AuthenticationUiState.Locked) {
            mutableUiState.value = AuthenticationUiState.Booting
        }
    }

    fun onBackground() {
        isForeground = false
        if (mutableUiState.value !is AuthenticationUiState.Authenticating) {
            mutableUiState.value = AuthenticationUiState.Locked
        }
    }

    fun requestAuthentication(): Boolean {
        val canRequest = isForeground && when (mutableUiState.value) {
            AuthenticationUiState.Booting,
            is AuthenticationUiState.Error,
            -> true

            else -> false
        }
        if (canRequest) {
            mutableUiState.value = AuthenticationUiState.Authenticating()
        }
        return canRequest
    }

    fun onAuthenticationResult(result: BiometricAuthenticationResult) {
        if (!isForeground) {
            mutableUiState.value = AuthenticationUiState.Locked
            return
        }

        mutableUiState.value = when (result) {
            BiometricAuthenticationResult.Success -> AuthenticationUiState.AccessGranted
            BiometricAuthenticationResult.AttemptFailed -> {
                val attempts = (mutableUiState.value as? AuthenticationUiState.Authenticating)
                    ?.failedAttempts
                    ?: 0
                AuthenticationUiState.Authenticating(failedAttempts = attempts + 1)
            }

            BiometricAuthenticationResult.DeviceSecurityRequired -> AuthenticationUiState.Error(
                title = "SECURITY PROFILE REQUIRED",
                message = "生体認証または画面ロックを端末に設定してください。",
                recoveryAction = AuthenticationRecoveryAction.OpenSecuritySettings,
            )

            is BiometricAuthenticationResult.Canceled -> AuthenticationUiState.Error(
                title = "ACCESS ABORTED",
                message = result.message,
                recoveryAction = AuthenticationRecoveryAction.Retry,
            )

            is BiometricAuthenticationResult.Unavailable -> AuthenticationUiState.Error(
                title = "AUTHENTICATION OFFLINE",
                message = result.message,
                recoveryAction = AuthenticationRecoveryAction.Retry,
            )
        }
    }

    fun completeUnlockAnimation() {
        if (mutableUiState.value == AuthenticationUiState.AccessGranted && isForeground) {
            mutableUiState.value = AuthenticationUiState.Unlocked
        }
    }
}

sealed interface AuthenticationUiState {
    data object Locked : AuthenticationUiState

    data object Booting : AuthenticationUiState

    data class Authenticating(
        val failedAttempts: Int = 0,
    ) : AuthenticationUiState

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
