package jp.hotdrop.orion.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import jp.hotdrop.orion.data.archive.KnowledgeArchiveRepository
import jp.hotdrop.orion.data.incoming.GoogleDriveRemoteDataSource
import jp.hotdrop.orion.data.incoming.IncomingIntelligenceRepository
import jp.hotdrop.orion.data.settings.SettingsRepository
import jp.hotdrop.orion.ui.archive.KnowledgeArchiveEditorRoute
import jp.hotdrop.orion.ui.archive.KnowledgeArchiveRoute
import jp.hotdrop.orion.ui.incoming.IncomingIntelligenceRoute
import jp.hotdrop.orion.ui.settings.SettingsRoute

@Composable
fun OrionNavHost(
    navController: NavHostController,
    startDestination: OrionTopLevelDestination,
    settingsRepository: SettingsRepository,
    knowledgeArchiveRepository: KnowledgeArchiveRepository,
    incomingIntelligenceRepository: IncomingIntelligenceRepository,
    googleDriveRemoteDataSource: GoogleDriveRemoteDataSource,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination.route,
        modifier = modifier,
    ) {
        composable(OrionTopLevelDestination.Incoming.route) {
            IncomingIntelligenceRoute(
                settingsRepository = settingsRepository,
                incomingRepository = incomingIntelligenceRepository,
                onOpenSettings = {
                    navController.navigate(OrionDestination.SettingsRoute) {
                        launchSingleTop = true
                    }
                },
                modifier = Modifier,
            )
        }
        composable(OrionTopLevelDestination.Archive.route) {
            KnowledgeArchiveRoute(
                repository = knowledgeArchiveRepository,
                onCreateEntry = { navController.navigate(OrionDestination.ArchiveNewRoute) },
                onEditEntry = { entryId ->
                    navController.navigate(OrionDestination.archiveEditRoute(entryId))
                },
                modifier = Modifier,
            )
        }
        composable(OrionDestination.ArchiveNewRoute) {
            KnowledgeArchiveEditorRoute(
                repository = knowledgeArchiveRepository,
                entryId = null,
                onClose = navController::popBackStack,
                modifier = Modifier,
            )
        }
        composable(
            route = OrionDestination.ArchiveEditRoute,
            arguments = listOf(
                navArgument(OrionDestination.ArchiveEntryIdArgument) {
                    type = NavType.LongType
                },
            ),
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getLong(OrionDestination.ArchiveEntryIdArgument)
                ?: return@composable
            KnowledgeArchiveEditorRoute(
                repository = knowledgeArchiveRepository,
                entryId = entryId,
                onClose = navController::popBackStack,
                modifier = Modifier,
            )
        }
        composable(OrionDestination.SettingsRoute) {
            SettingsRoute(
                settingsRepository = settingsRepository,
                driveRemoteDataSource = googleDriveRemoteDataSource,
                modifier = Modifier,
            )
        }
    }
}

fun NavHostController.navigateToTopLevel(destination: OrionTopLevelDestination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
