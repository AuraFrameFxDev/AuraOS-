package dev.aurakai.auraframefx.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.aurakai.auraframefx.ui.screens.AgentNexusScreen
import dev.aurakai.auraframefx.ui.screens.MainScreen

/**
 * Navigation routes for AuraFrameFX
 */
object Routes {
    const val MAIN = "main"
    const val AGENT_NEXUS = "agent_nexus"
    const val ORACLE_DRIVE = "oracle_drive"
    const val SETTINGS = "settings"
}

/**
 * Main navigation host for the app
 */
@Composable
fun AuraFrameNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Routes.MAIN
    ) {
        composable(Routes.MAIN) {
            MainScreen(
                onNavigateToAgentNexus = {
                    navController.navigate(Routes.AGENT_NEXUS)
                },
                onNavigateToOracleDrive = {
                    navController.navigate(Routes.ORACLE_DRIVE)
                },
                onNavigateToSettings = {
                    navController.navigate(Routes.SETTINGS)
                }
            )
        }

        composable(Routes.AGENT_NEXUS) {
            AgentNexusScreen(
                onAgentSelected = { agentName ->
                    // Handle agent selection
                },
                onDepartureTaskAssigned = { agent, task ->
                    // Task will be handled by the service
                }
            )
        }

        composable(Routes.ORACLE_DRIVE) {
            // OracleDriveScreen()
        }

        composable(Routes.SETTINGS) {
            // SettingsScreen()
        }
    }
}
