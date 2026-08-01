package jp.hotdrop.orion

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import jp.hotdrop.orion.ui.OrionRoot
import jp.hotdrop.orion.ui.authentication.AndroidBiometricAuthenticator
import jp.hotdrop.orion.ui.authentication.AuthenticationViewModel
import jp.hotdrop.orion.ui.authentication.BiometricAuthenticator
import jp.hotdrop.orion.ui.authentication.OrionSecureRoot
import jp.hotdrop.orion.ui.theme.OrionTheme

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    private val authenticationViewModel: AuthenticationViewModel by viewModels()
    private lateinit var biometricAuthenticator: BiometricAuthenticator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        biometricAuthenticator = AndroidBiometricAuthenticator(
            activity = this,
            onResult = authenticationViewModel::onAuthenticationResult,
        )

        setContent {
            val authenticationUiState by authenticationViewModel.uiState.collectAsStateWithLifecycle()

            OrionTheme {
                OrionSecureRoot(
                    uiState = authenticationUiState,
                    onAuthenticationRequested = {
                        if (authenticationViewModel.requestAuthentication()) {
                            biometricAuthenticator.authenticate()
                        }
                    },
                    onUnlockAnimationFinished = authenticationViewModel::completeUnlockAnimation,
                    onOpenSecuritySettings = {
                        startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
                    },
                    onClose = ::finish,
                ) {
                    OrionRoot()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        authenticationViewModel.onForeground()
    }

    override fun onStop() {
        if (!isChangingConfigurations) {
            authenticationViewModel.onBackground()
        }
        super.onStop()
    }
}
