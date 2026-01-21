package com.example.pickerio.screens

/**
 * Navigation routes for the app
 */
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object ImageSelector : Screen("image_selector")
    object Camera : Screen("camera")
    object Gallery : Screen("gallery")
    object ColorAnalysis : Screen("color_analysis/{imageUri}") {
        fun createRoute(imageUri: String) = "color_analysis/$imageUri"
    }
    object PaletteDetail : Screen("palette_detail/{paletteId}") {
        fun createRoute(paletteId: String) = "palette_detail/$paletteId"
    }
    object ColorResults : Screen("color_results/{colorsJson}") {
        fun createRoute(colorsJson: String) = "color_results/$colorsJson"
    }
    object Settings : Screen("settings")
}

/**
 * Arguments for navigation
 */
object NavArguments {
    const val IMAGE_URI = "imageUri"
    const val PALETTE_ID = "paletteId"
    const val COLORS_JSON = "colorsJson"
}