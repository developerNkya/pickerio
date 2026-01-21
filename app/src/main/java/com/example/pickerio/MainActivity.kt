package com.example.pickerio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import android.net.Uri
import com.example.pickerio.screens.*
import com.example.pickerio.ui.theme.PickerioTheme
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PickerioTheme(
                darkTheme = false,
                dynamicColor = true
            ) {
                // Create navigation controller
                val navController = rememberNavController()

                // Setup NavHost
                NavHost(
                    navController = navController,
                    startDestination = Screen.Home.route
                ) {
                    // Home Screen
                    composable(Screen.Home.route) {
                        HomeScreen(
                            props = HomeScreenProps(
                                onGetStarted = {
                                    // Navigate to ImageSelector when button is clicked
                                    navController.navigate(Screen.ImageSelector.route)
                                }
                            )
                        )
                    }

                    // Image Selector Screen
                    composable(Screen.ImageSelector.route) {
                        ImageSelector(
                            props = ImageSelectorProps(
                                onImageSelect = { imageUri ->
                                    val encodedUri = Uri.encode(imageUri.toString())
                                    navController.navigate(Screen.ColorAnalysis.createRoute(encodedUri))
                                },
                                onBack = {
                                    // Go back to home screen
                                    navController.popBackStack()
                                }
                            )
                        )
                    }

                    // Add other screens as needed
                    composable(
                        route = Screen.ColorAnalysis.route,
                        arguments = listOf(
                            navArgument(NavArguments.IMAGE_URI) {
                                type = NavType.StringType
                            }
                        )
                    ) { backStackEntry ->
                        val imageUriString = backStackEntry.arguments?.getString(NavArguments.IMAGE_URI)
                        if (imageUriString != null) {
                            val imageUri = Uri.parse(imageUriString)
                            ColorPicker(
                                props = ColorPickerProps(
                                    imageUri = imageUri,
                                    onColorPick = { colors ->
                                        val json = Uri.encode(Gson().toJson(colors))
                                        navController.navigate(Screen.ColorResults.createRoute(json))
                                    },
                                    onBack = {
                                        navController.popBackStack()
                                    }
                                )
                            )
                        }
                    }

                    composable(
                        route = Screen.ColorResults.route,
                        arguments = listOf(
                            navArgument(NavArguments.COLORS_JSON) {
                                type = NavType.StringType
                            }
                        )
                    ) { backStackEntry ->
                        val colorsJson = backStackEntry.arguments?.getString(NavArguments.COLORS_JSON)
                        if (colorsJson != null) {
                            val type = object : TypeToken<List<CustomColorInfo>>() {}.type
                            val colors = Gson().fromJson<List<CustomColorInfo>>(colorsJson, type)
                            ColorResultsScreen(
                                colors = colors,
                                onBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                    /*
                    composable(Screen.Camera.route) {
                        // Camera screen implementation
                    }

                    composable(Screen.Gallery.route) {
                        // Gallery screen implementation
                    }

                    composable(
                        route = Screen.PaletteDetail.route,
                        arguments = listOf(
                            navArgument(NavArguments.PALETTE_ID) {
                                type = NavType.StringType
                            }
                        )
                    ) { backStackEntry ->
                        val paletteId = backStackEntry.arguments?.getString(NavArguments.PALETTE_ID)
                        // Palette detail screen
                    }
                    */
                }
            }
        }
    }


}