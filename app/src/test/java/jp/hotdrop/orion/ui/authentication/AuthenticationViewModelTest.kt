package jp.hotdrop.orion.ui.authentication

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthenticationViewModelTest {
    @Test
    fun authentication_startsOnlyInForeground() {
        val viewModel = AuthenticationViewModel()

        assertEquals(AuthenticationUiState.Locked, viewModel.uiState.value)
        assertFalse(viewModel.requestAuthentication())

        viewModel.onForeground()

        assertEquals(AuthenticationUiState.Booting, viewModel.uiState.value)
        assertTrue(viewModel.requestAuthentication())
        assertEquals(AuthenticationUiState.Authenticating(), viewModel.uiState.value)
        assertFalse(viewModel.requestAuthentication())
    }

    @Test
    fun successfulAuthentication_unlocksAfterAnimation() {
        val viewModel = foregroundViewModel()

        viewModel.requestAuthentication()
        viewModel.onAuthenticationResult(BiometricAuthenticationResult.Success)

        assertEquals(AuthenticationUiState.AccessGranted, viewModel.uiState.value)

        viewModel.completeUnlockAnimation()

        assertEquals(AuthenticationUiState.Unlocked, viewModel.uiState.value)
    }

    @Test
    fun failedAttempt_keepsSystemAuthenticationActive() {
        val viewModel = foregroundViewModel()
        viewModel.requestAuthentication()

        viewModel.onAuthenticationResult(BiometricAuthenticationResult.AttemptFailed)
        viewModel.onAuthenticationResult(BiometricAuthenticationResult.AttemptFailed)

        assertEquals(
            AuthenticationUiState.Authenticating(failedAttempts = 2),
            viewModel.uiState.value,
        )
    }

    @Test
    fun canceledAuthentication_canBeRetried() {
        val viewModel = foregroundViewModel()
        viewModel.requestAuthentication()

        viewModel.onAuthenticationResult(
            BiometricAuthenticationResult.Canceled("認証はキャンセルされました。"),
        )

        assertEquals(
            AuthenticationUiState.Error(
                title = "ACCESS ABORTED",
                message = "認証はキャンセルされました。",
                recoveryAction = AuthenticationRecoveryAction.Retry,
            ),
            viewModel.uiState.value,
        )
        assertTrue(viewModel.requestAuthentication())
    }

    @Test
    fun missingDeviceSecurity_offersSystemSettings() {
        val viewModel = foregroundViewModel()
        viewModel.requestAuthentication()

        viewModel.onAuthenticationResult(BiometricAuthenticationResult.DeviceSecurityRequired)

        val state = viewModel.uiState.value as AuthenticationUiState.Error
        assertEquals(AuthenticationRecoveryAction.OpenSecuritySettings, state.recoveryAction)
    }

    @Test
    fun backgroundingUnlockedApp_relocksIt() {
        val viewModel = foregroundViewModel()
        viewModel.requestAuthentication()
        viewModel.onAuthenticationResult(BiometricAuthenticationResult.Success)
        viewModel.completeUnlockAnimation()

        viewModel.onBackground()

        assertEquals(AuthenticationUiState.Locked, viewModel.uiState.value)
    }

    @Test
    fun authenticationSuccessWhileBackgrounded_doesNotUnlock() {
        val viewModel = foregroundViewModel()
        viewModel.requestAuthentication()
        viewModel.onBackground()

        viewModel.onAuthenticationResult(BiometricAuthenticationResult.Success)

        assertEquals(AuthenticationUiState.Locked, viewModel.uiState.value)
    }

    private fun foregroundViewModel() = AuthenticationViewModel().also {
        it.onForeground()
    }
}
