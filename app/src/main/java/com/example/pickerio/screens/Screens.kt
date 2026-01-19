package com.example.pickerio.screens

/**
 * Navigation routes for the app
 */
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Camera : Screen("camera")
    object Gallery : Screen("gallery")
    object PaletteDetail : Screen("palette_detail/{paletteId}") {
        fun createRoute(paletteId: String) = "palette_detail/$paletteId"
    }
    object Settings : Screen("settings")
}

/**
 * Arguments for navigation
 */
object NavArguments {
    const val PALETTE_ID = "paletteId"
}