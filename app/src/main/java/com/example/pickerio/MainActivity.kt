package com.example.pickerio

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.pickerio.api.VersionNetworkModule
import com.example.pickerio.screens.*
import com.example.pickerio.ui.theme.PickerioTheme
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PickerioTheme(
                darkTheme = false,
                dynamicColor = true
            ) {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp() {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Version Check States
    var showUpdateDialog by remember { mutableStateOf(false) }
    var updateRequired by remember { mutableStateOf(false) }
    var updateUrl by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var updateProgress by remember { mutableStateOf(0f) }

    // Track if user tried to navigate
    var userTriedToNavigate by remember { mutableStateOf(false) }

    // Check for app update on app start
    LaunchedEffect(Unit) {
        try {
            val response = VersionNetworkModule.api.checkVersion("pickerio", "2.0")
            Log.d("VersionCheck", "Response: $response")
            if (response.status == "false") {
                response.download_url?.let {
                    updateUrl = it
                    updateRequired = true
                    showUpdateDialog = true
                }
            }
        } catch (e: Exception) {
            Log.e("VersionCheck", "Error checking version", e)
            e.printStackTrace()
        }
    }

    // Handle update progress animation
    LaunchedEffect(isLoading) {
        if (isLoading) {
            // Simulate progress animation
            for (i in 0..100 step 5) {
                updateProgress = i / 100f
                delay(50)
            }
            // Open update URL in browser
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl))
            context.startActivity(intent)
            // Close dialog after opening browser
            delay(500)
            isLoading = false
            showUpdateDialog = false
        }
    }

    // Show blocking overlay when update is required
    if (updateRequired) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background.copy(alpha = 0.95f)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Show update dialog always when update is required
                UpdateRequiredDialog(
                    updateUrl = updateUrl,
                    isLoading = isLoading,
                    progress = updateProgress,
                    onUpdateClick = {
                        isLoading = true
                    },
                    onDismiss = {
                        // Don't allow dismiss - update is required
                        showUpdateDialog = true
                    },
                    isRequired = true
                )
            }
        }
    } else {
        // Normal app navigation - only show when update is NOT required
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route
        ) {
            // Home Screen
            composable(Screen.Home.route) {
                HomeScreen(
                    props = HomeScreenProps(
                        onGetStarted = {
                            // If update is required, show update dialog instead of navigating
                            if (updateRequired) {
                                userTriedToNavigate = true
                                showUpdateDialog = true
                            } else {
                                navController.navigate(Screen.ImageSelector.route)
                            }
                        }
                    )
                )
            }

            // Image Selector Screen
            composable(Screen.ImageSelector.route) {
                ImageSelector(
                    props = ImageSelectorProps(
                        onImageSelect = { imageUri ->
                            if (updateRequired) {
                                userTriedToNavigate = true
                                showUpdateDialog = true
                            } else {
                                val encodedUri = Uri.encode(imageUri.toString())
                                navController.navigate(Screen.ColorAnalysis.createRoute(encodedUri))
                            }
                        },
                        onBack = {
                            if (!updateRequired) {
                                navController.popBackStack()
                            }
                        }
                    )
                )
            }

            // Color Analysis Screen
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
                    ColorPicker(
                        props = ColorPickerProps(
                            imageUri = Uri.parse(imageUriString),
                            onColorPick = { colors ->
                                if (updateRequired) {
                                    userTriedToNavigate = true
                                    showUpdateDialog = true
                                } else {
                                    val json = Uri.encode(Gson().toJson(colors))
                                    navController.navigate(Screen.ColorResults.createRoute(json))
                                }
                            },
                            onBack = {
                                if (!updateRequired) {
                                    navController.popBackStack()
                                }
                            }
                        )
                    )
                }
            }

            // Color Results Screen
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
                    ColorResults(
                        props = ColorResultsProps(
                            colors = colors,
                            onNewPhoto = {
                                if (updateRequired) {
                                    userTriedToNavigate = true
                                    showUpdateDialog = true
                                } else {
                                    navController.popBackStack(Screen.ImageSelector.route, inclusive = false)
                                }
                            },
                            onBack = {
                                if (!updateRequired) {
                                    navController.popBackStack()
                                }
                            }
                        )
                    )
                }
            }
        }
    }

    // Show update dialog when triggered (either automatically or by user trying to navigate)
    if (showUpdateDialog) {
        UpdateRequiredDialog(
            updateUrl = updateUrl,
            isLoading = isLoading,
            progress = updateProgress,
            onUpdateClick = {
                isLoading = true
            },
            onDismiss = {
                // Only allow dismiss if user hasn't tried to navigate
                if (!userTriedToNavigate && !isLoading) {
                    showUpdateDialog = false
                }
            },
            isRequired = updateRequired || userTriedToNavigate
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateRequiredDialog(
    updateUrl: String,
    isLoading: Boolean = false,
    progress: Float = 0f,
    onUpdateClick: () -> Unit,
    onDismiss: () -> Unit,
    isRequired: Boolean = false
) {
    Dialog(
        onDismissRequest = {
            if (!isLoading && !isRequired) {
                onDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = !isLoading && !isRequired,
            dismissOnClickOutside = !isLoading && !isRequired
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Decorative Top Gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = if (isRequired) {
                                    listOf(
                                        MaterialTheme.colorScheme.error,
                                        MaterialTheme.colorScheme.errorContainer,
                                        MaterialTheme.colorScheme.error
                                    )
                                } else {
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary,
                                        MaterialTheme.colorScheme.tertiary
                                    )
                                }
                            )
                        )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Icon Container
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            color = if (isRequired) {
                                MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                            } else {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            },
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            strokeWidth = 3.dp,
                            color = if (isRequired) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.CloudDownload,
                            contentDescription = "Update",
                            modifier = Modifier.size(48.dp),
                            tint = if (isRequired) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Title
                Text(
                    text = if (isLoading) "Updating..."
                    else if (isRequired) "Update Required!"
                    else "New Update Available!",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ),
                    color = if (isRequired) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Description
                Text(
                    text = if (isLoading)
                        "Please wait while we prepare your update..."
                    else if (isRequired)
                        "You must update the app to continue using all features. Please update now to access the latest improvements and bug fixes."
                    else
                        "Experience the latest features, improvements, and bug fixes. Update now for the best experience!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Progress Indicator (shown during loading)
                if (isLoading) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (isRequired) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            trackColor = if (isRequired) {
                                MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                            } else {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Features List (only when not loading)
                if (!isLoading) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    ) {
                        UpdateFeatureItem(
                            icon = Icons.Filled.Update,
                            text = "Latest features & improvements",
                            isRequired = isRequired
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        UpdateFeatureItem(
                            icon = Icons.Filled.Warning,
                            text = "Bug fixes & performance updates",
                            isRequired = isRequired
                        )
                        if (isRequired) {
                            Spacer(modifier = Modifier.height(8.dp))
                            UpdateFeatureItem(
                                icon = Icons.Filled.Warning,
                                text = "Required for app functionality",
                                isRequired = isRequired
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Only show "Later" button if not required and not loading
                    if (!isLoading && !isRequired) {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.textButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text(
                                text = "Later",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Button(
                        onClick = onUpdateClick,
                        modifier = Modifier
                            .weight(1f)
                            .then(if (isRequired) Modifier.fillMaxWidth() else Modifier),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRequired) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            contentColor = if (isRequired) {
                                MaterialTheme.colorScheme.onError
                            } else {
                                MaterialTheme.colorScheme.onPrimary
                            }
                        ),
                        enabled = !isLoading
                    ) {
                        Icon(
                            imageVector = if (isLoading) Icons.Filled.Update else Icons.Filled.CloudDownload,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isLoading) "Preparing..." else "Update Now",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Footer Note
                if (!isLoading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (isRequired)
                            "You must update to continue using the app"
                        else "This update is required to continue using the app",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isRequired) {
                            MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        },
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun UpdateFeatureItem(icon: ImageVector, text: String, isRequired: Boolean = false) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = if (isRequired) {
                MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
            } else {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            }
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
    }
}