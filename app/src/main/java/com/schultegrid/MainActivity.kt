package com.schultegrid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.schultegrid.domain.model.GameConfig
import com.schultegrid.ui.navigation.NavArguments
import com.schultegrid.ui.navigation.Screen
import com.schultegrid.ui.screens.game.GameScreen
import com.schultegrid.ui.screens.history.HistoryScreen
import com.schultegrid.ui.screens.home.HomeScreen
import com.schultegrid.ui.screens.statistics.StatisticsScreen
import com.schultegrid.ui.theme.SchulteGridTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main activity - Single activity with Navigation Compose
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SchulteGridTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SchulteGridNavGraph()
                }
            }
        }
    }
}

@Composable
fun SchulteGridNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        // Home screen
        composable(route = Screen.Home.route) {
            HomeScreen(
                onStartGame = { config ->
                    navController.navigate(
                        Screen.Game.createRoute(config.getSize(), config.getDifficulty())
                    )
                },
                onHistoryClick = {
                    navController.navigate(Screen.History.route)
                },
                onStatisticsClick = {
                    navController.navigate(Screen.Statistics.route)
                }
            )
        }

        // Game screen
        composable(
            route = "game?size={size}&difficulty={difficulty}",
            arguments = listOf(
                navArgument(NavArguments.SIZE) {
                    type = NavType.StringType
                    defaultValue = GameConfig.DEFAULT_SIZE
                },
                navArgument(NavArguments.DIFFICULTY) {
                    type = NavType.StringType
                    defaultValue = GameConfig.DEFAULT_DIFFICULTY
                }
            )
        ) { backStackEntry ->
            val size = backStackEntry.arguments?.getString(NavArguments.SIZE) ?: GameConfig.DEFAULT_SIZE
            val difficulty = backStackEntry.arguments?.getString(NavArguments.DIFFICULTY) ?: GameConfig.DEFAULT_DIFFICULTY
            val config = GameConfig(size, difficulty)

            GameScreen(
                config = config,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // History screen
        composable(route = Screen.History.route) {
            HistoryScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // Statistics screen
        composable(route = Screen.Statistics.route) {
            StatisticsScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
