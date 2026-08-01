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
import jp.hotdrop.orion.data.archive.KnowledgeArchiveDraft
import jp.hotdrop.orion.data.archive.KnowledgeArchiveEntry
import jp.hotdrop.orion.data.archive.KnowledgeArchiveRepository
import jp.hotdrop.orion.data.settings.SettingsRepository
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
    modifier: Modifier = Modifier,
    viewModel: OrionViewModel = viewModel(),
) {
    val selectedDestination by viewModel.selectedDestination.collectAsStateWithLifecycle()

    OrionAppShell(
        selectedDestination = selectedDestination,
        onDestinationSelected = viewModel::selectDestination,
        settingsRepository = settingsRepository,
        knowledgeArchiveRepository = knowledgeArchiveRepository,
        modifier = modifier,
    )
}

@Composable
internal fun OrionAppShell(
    selectedDestination: OrionTopLevelDestination,
    onDestinationSelected: (OrionTopLevelDestination) -> Unit,
    settingsRepository: SettingsRepository,
    knowledgeArchiveRepository: KnowledgeArchiveRepository,
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
        )
    }
}

private val PreviewSettingsRepository = object : SettingsRepository {
    override fun observeGoogleDrivePath() = kotlinx.coroutines.flow.flowOf<String?>(null)

    override suspend fun setGoogleDrivePath(rawPath: String) = Unit
}

private val PreviewKnowledgeArchiveRepository = object : KnowledgeArchiveRepository {
    override fun observeEntries(): Flow<List<KnowledgeArchiveEntry>> = flowOf(emptyList())

    override suspend fun getEntry(id: Long): KnowledgeArchiveEntry? = null

    override suspend fun saveEntry(id: Long?, draft: KnowledgeArchiveDraft): Long = 1L

    override suspend fun deleteEntry(id: Long) = Unit
}
