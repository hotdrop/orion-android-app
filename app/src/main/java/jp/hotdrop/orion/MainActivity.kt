package jp.hotdrop.orion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import jp.hotdrop.orion.ui.OrionRoot
import jp.hotdrop.orion.ui.theme.OrionTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OrionTheme {
                OrionRoot(
                    settingsRepository = (application as OrionApplication).settingsRepository,
                    knowledgeArchiveRepository = (application as OrionApplication).knowledgeArchiveRepository,
                    incomingIntelligenceRepository = (application as OrionApplication).incomingIntelligenceRepository,
                    googleDriveRemoteDataSource = (application as OrionApplication).googleDriveRemoteDataSource,
                )
            }
        }
    }
}
