package jp.hotdrop.orion.navigation

import jp.hotdrop.orion.ui.incoming.IncomingMemoRoute
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import jp.hotdrop.orion.ui.archive.KnowledgeArchiveEditorRoute
import jp.hotdrop.orion.ui.archive.KnowledgeArchiveRoute
import jp.hotdrop.orion.ui.incoming.IncomingIntelligenceRoute
import jp.hotdrop.orion.ui.settings.SettingsRoute

@Composable
fun OrionNavHost(
    navController: NavHostController,
    startDestination: OrionTopLevelDestination,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination.route,
        modifier = modifier,
    ) {
        composable(OrionTopLevelDestination.Incoming.route) {
            IncomingIntelligenceRoute(
                onOpenSettings = {
                    navController.navigate(OrionDestination.SettingsRoute) {
                        launchSingleTop = true
                    }
                },
                onEditMemo = { id -> navController.navigate(OrionDestination.incomingMemoRoute(id)) },
                modifier = Modifier,
            )
        }
        composable(
            route = OrionDestination.INCOMING_MEMO_ROUTE,
            arguments = listOf(navArgument(OrionDestination.INCOMING_DOCUMENT_ID) { type = NavType.StringType }),
        ) {
            IncomingMemoRoute(onClose = { navController.popBackStack() })
        }
        composable(OrionTopLevelDestination.Archive.route) {
            KnowledgeArchiveRoute(
                onCreateEntry = { navController.navigate(OrionDestination.ArchiveNewRoute) },
                onEditEntry = { entryId ->
                    navController.navigate(OrionDestination.archiveEditRoute(entryId))
                },
                modifier = Modifier,
            )
        }
        composable(OrionDestination.ArchiveNewRoute) {
            KnowledgeArchiveEditorRoute(
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
        ) {
            KnowledgeArchiveEditorRoute(
                onClose = navController::popBackStack,
                modifier = Modifier,
            )
        }
        composable(OrionDestination.SettingsRoute) {
            SettingsRoute(modifier = Modifier)
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
