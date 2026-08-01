package jp.hotdrop.orion.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import jp.hotdrop.orion.data.KnowledgeArchiveRepository
import jp.hotdrop.orion.data.remote.GoogleDriveFile
import jp.hotdrop.orion.data.remote.GoogleDriveRemoteDataSource
import jp.hotdrop.orion.data.local.entity.IncomingIntelligenceRecord
import jp.hotdrop.orion.data.IncomingIntelligenceRepository
import jp.hotdrop.orion.data.SettingsRepository
import jp.hotdrop.orion.model.GoogleDriveTarget
import jp.hotdrop.orion.model.KnowledgeArchiveDraft
import jp.hotdrop.orion.model.KnowledgeArchiveEntry
import jp.hotdrop.orion.navigation.OrionDestination
import jp.hotdrop.orion.navigation.OrionNavHost
import jp.hotdrop.orion.navigation.OrionTopLevelDestination
import jp.hotdrop.orion.navigation.navigateToTopLevel
import jp.hotdrop.orion.ui.components.OrionBottomNavigation
import jp.hotdrop.orion.ui.components.OrionHeader
import jp.hotdrop.orion.ui.theme.OrionDeepNavy
import jp.hotdrop.orion.ui.theme.OrionTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Composable
fun OrionRoot(
    settingsRepository: SettingsRepository,
    knowledgeArchiveRepository: KnowledgeArchiveRepository,
    incomingIntelligenceRepository: IncomingIntelligenceRepository,
    googleDriveRemoteDataSource: GoogleDriveRemoteDataSource,
    modifier: Modifier = Modifier,
    viewModel: OrionViewModel = viewModel(),
) {
    val selectedDestination by viewModel.selectedDestination.collectAsStateWithLifecycle()

    OrionAppShell(
        selectedDestination = selectedDestination,
        onDestinationSelected = viewModel::selectDestination,
        settingsRepository = settingsRepository,
        knowledgeArchiveRepository = knowledgeArchiveRepository,
        incomingIntelligenceRepository = incomingIntelligenceRepository,
        googleDriveRemoteDataSource = googleDriveRemoteDataSource,
        modifier = modifier,
    )
}

@Composable
internal fun OrionAppShell(
    selectedDestination: OrionTopLevelDestination,
    onDestinationSelected: (OrionTopLevelDestination) -> Unit,
    settingsRepository: SettingsRepository,
    knowledgeArchiveRepository: KnowledgeArchiveRepository,
    incomingIntelligenceRepository: IncomingIntelligenceRepository,
    googleDriveRemoteDataSource: GoogleDriveRemoteDataSource,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val isShowingSettings = currentRoute == OrionDestination.SettingsRoute
    val isShowingArchiveEditor = currentRoute == OrionDestination.ArchiveNewRoute ||
        currentRoute == OrionDestination.ArchiveEditRoute
    val isShowingSecondaryDestination = isShowingSettings || isShowingArchiveEditor
    val currentTopLevelDestination = OrionTopLevelDestination.fromRoute(currentRoute)
        ?: selectedDestination
    val currentTitle = when {
        isShowingSettings -> OrionDestination.SettingsTitle
        currentRoute == OrionDestination.ArchiveNewRoute -> OrionDestination.ArchiveNewTitle
        currentRoute == OrionDestination.ArchiveEditRoute -> OrionDestination.ArchiveEditTitle
        else -> currentTopLevelDestination.title
    }
    val backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    BackHandler(enabled = isShowingSettings) {
        navController.popBackStack()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = OrionDeepNavy,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            OrionHeader(
                title = currentTitle,
                isShowingBackNavigation = isShowingSecondaryDestination,
                onSettingsClick = {
                    navController.navigate(OrionDestination.SettingsRoute) {
                        launchSingleTop = true
                    }
                },
                onBackClick = { backPressedDispatcher?.onBackPressed() },
            )
        },
        bottomBar = {
            if (!isShowingSecondaryDestination) {
                OrionBottomNavigation(
                    selectedDestination = currentTopLevelDestination,
                    onDestinationSelected = { destination ->
                        onDestinationSelected(destination)
                        navController.navigateToTopLevel(destination)
                    },
                )
            }
        },
    ) { innerPadding ->
        OrionNavHost(
            navController = navController,
            startDestination = selectedDestination,
            settingsRepository = settingsRepository,
            knowledgeArchiveRepository = knowledgeArchiveRepository,
            incomingIntelligenceRepository = incomingIntelligenceRepository,
            googleDriveRemoteDataSource = googleDriveRemoteDataSource,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Preview(
    name = "ORION Root Shell",
    showBackground = true,
    backgroundColor = 0xFF030812,
    widthDp = 393,
    heightDp = 852,
)
@Composable
private fun OrionRootPreview() {
    OrionTheme {
        OrionAppShell(
            selectedDestination = OrionTopLevelDestination.Incoming,
            onDestinationSelected = {},
            settingsRepository = PreviewSettingsRepository,
            knowledgeArchiveRepository = PreviewKnowledgeArchiveRepository,
            incomingIntelligenceRepository = PreviewIncomingIntelligenceRepository,
            googleDriveRemoteDataSource = PreviewGoogleDriveRemoteDataSource,
        )
    }
}

private val PreviewSettingsRepository = object : SettingsRepository {
    override fun observeDriveTarget() = kotlinx.coroutines.flow.flowOf<GoogleDriveTarget?>(null)

    override suspend fun setDriveTarget(target: GoogleDriveTarget) = Unit

    override suspend fun clearDriveTarget() = Unit
}

private val PreviewKnowledgeArchiveRepository = object : KnowledgeArchiveRepository {
    override fun observeEntries(): Flow<List<KnowledgeArchiveEntry>> = flowOf(emptyList())

    override suspend fun getEntry(id: Long): KnowledgeArchiveEntry? = null

    override suspend fun saveEntry(id: Long?, draft: KnowledgeArchiveDraft): Long = 1L

    override suspend fun deleteEntry(id: Long) = Unit
}

private val PreviewIncomingIntelligenceRepository = object : IncomingIntelligenceRepository {
    override fun observeDocuments(rootFolderId: String): Flow<List<IncomingIntelligenceRecord>> =
        flowOf(emptyList())

    override fun observeLastSyncedAt(rootFolderId: String): Flow<Long?> = flowOf(null)

    override suspend fun synchronize(rootFolderId: String, accessToken: String) = Unit

    override suspend fun markOpened(rootFolderId: String, driveFileId: String) = Unit
}

private val PreviewGoogleDriveRemoteDataSource = object : GoogleDriveRemoteDataSource {
    override suspend fun getFolder(accessToken: String, folderId: String) = GoogleDriveFile(
        id = folderId,
        name = "ORION/Incoming",
        mimeType = "application/vnd.google-apps.folder",
        modifiedAt = 0,
        webViewLink = null,
    )

    override suspend fun listChildren(accessToken: String, folderId: String) =
        emptyList<GoogleDriveFile>()
}
