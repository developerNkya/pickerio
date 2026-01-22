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
import com.example.pickerio.utils.OnboardingManager
import androidx.compose.ui.platform.LocalContext
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import android.content.Intent
import com.example.pickerio.api.VersionNetworkModule

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
                val context = LocalContext.current

                // Version Check
                val showUpdateDialog = remember { mutableStateOf(false) }
                val updateUrl = remember { mutableStateOf("") }

                LaunchedEffect(Unit) {
                    try {
                        val response = VersionNetworkModule.api.checkVersion("pickerio", "2.0")
                        if (response.status == "false") {
                            response.download_url?.let {
                                updateUrl.value = it
                                showUpdateDialog.value = true
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                if (showUpdateDialog.value) {
                    AlertDialog(
                        onDismissRequest = {},
                        title = { Text("Update Required") },
                        text = { Text("App is out of date and needs to be updated.") },
                        confirmButton = {
                            TextButton(onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl.value))
                                context.startActivity(intent)
                            }) {
                                Text("Update Now")
                            }
                        }
                    )
                }
//                val onboardingManager = remember { OnboardingManager(context) }

//                // Determine start destination based on onboarding state
//                val startDestination = remember {
//                    if (onboardingManager.isOnboardingCompleted()) Screen.Home.route else Screen.Onboarding.route
//                }

                val startDestination = Screen.Home.route;

                // Setup NavHost
                NavHost(
                    navController = navController,
                    startDestination = startDestination
                ) {
                    // Onboarding Screen
//                    composable(Screen.Onboarding.route) {
//                        OnboardingScreen(
//                            onComplete = {
//                                onboardingManager.setOnboardingCompleted()
//                                navController.navigate(Screen.Home.route) {
//                                    popUpTo(Screen.Onboarding.route) { inclusive = true }
//                                }
//                            }
//                        )
//                    }

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
                            ColorResults(  // Changed from ColorResultsScreen to ColorResults
                                props = ColorResultsProps(  // Add props wrapper
                                    colors = colors,
                                    onNewPhoto = {
                                        // Navigate back to image selector for new photo
                                        navController.popBackStack(Screen.ImageSelector.route, inclusive = false)
                                    },
                                    onBack = {
                                        navController.popBackStack()
                                    }
                                )
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