package com.example.pickerio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pickerio.screens.*
import com.example.pickerio.ui.theme.PickerioTheme

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
                                    // TODO: Handle image selection - navigate to color analysis
                                    // For now, just go back or show a toast
                                    // navController.navigate(Screen.Gallery.route)
                                },
                                onBack = {
                                    // Go back to home screen
                                    navController.popBackStack()
                                }
                            )
                        )
                    }

                    // Add other screens as needed
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