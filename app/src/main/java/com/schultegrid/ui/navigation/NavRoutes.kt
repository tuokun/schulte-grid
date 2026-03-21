package com.schultegrid.ui.navigation

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Navigation routes for the app
 */
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Game : Screen("game") {
        fun createRoute(size: String, difficulty: String): String {
            val encodedSize = URLEncoder.encode(size, StandardCharsets.UTF_8.toString())
            val encodedDifficulty = URLEncoder.encode(difficulty, StandardCharsets.UTF_8.toString())
            return "game?size=$encodedSize&difficulty=$encodedDifficulty"
        }
    }
    object History : Screen("history")
    object Statistics : Screen("statistics")
}

/**
 * Navigation arguments
 */
object NavArguments {
    const val SIZE = "size"
    const val DIFFICULTY = "difficulty"
}
