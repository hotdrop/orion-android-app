package jp.hotdrop.orion.ui.authentication

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

interface BiometricAuthenticator {
    fun authenticate()
}

sealed interface BiometricAuthenticationResult {
    data object Success : BiometricAuthenticationResult

    data object AttemptFailed : BiometricAuthenticationResult

    data object DeviceSecurityRequired : BiometricAuthenticationResult

    data class Canceled(val message: String) : BiometricAuthenticationResult

    data class Unavailable(val message: String) : BiometricAuthenticationResult
}

class AndroidBiometricAuthenticator(
    activity: FragmentActivity,
    private val onResult: (BiometricAuthenticationResult) -> Unit,
) : BiometricAuthenticator {
    private val biometricManager = BiometricManager.from(activity)
    private val biometricPrompt = BiometricPrompt(
        activity,
        ContextCompat.getMainExecutor(activity),
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onResult(BiometricAuthenticationResult.Success)
            }

            override fun onAuthenticationFailed() {
                onResult(BiometricAuthenticationResult.AttemptFailed)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                val result = when (errorCode) {
                    BiometricPrompt.ERROR_USER_CANCELED,
                    BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                    BiometricPrompt.ERROR_CANCELED,
                        -> BiometricAuthenticationResult.Canceled(
                        message = "認証はキャンセルされました。",
                    )

                    else -> BiometricAuthenticationResult.Unavailable(
                        message = errString.toString().ifBlank {
                            "端末認証を利用できません。"
                        },
                    )
                }
                onResult(result)
            }
        },
    )

    override fun authenticate() {
        when (biometricManager.canAuthenticate(ALLOWED_AUTHENTICATORS)) {
            BiometricManager.BIOMETRIC_SUCCESS -> biometricPrompt.authenticate(PROMPT_INFO)
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                onResult(BiometricAuthenticationResult.DeviceSecurityRequired)
            }

            else -> onResult(
                BiometricAuthenticationResult.Unavailable(
                    message = "この端末では生体認証または端末認証を利用できません。",
                ),
            )
        }
    }

    private companion object {
        const val ALLOWED_AUTHENTICATORS: Int =
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL

        val PROMPT_INFO: BiometricPrompt.PromptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("ORION // IDENTITY VERIFICATION")
            .setSubtitle("セキュアノードへのアクセスを認証します")
            .setAllowedAuthenticators(ALLOWED_AUTHENTICATORS)
            .build()
    }
}
