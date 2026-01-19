package com.example.pickerio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.pickerio.screens.HomeScreen
import com.example.pickerio.screens.HomeScreenProps
import com.example.pickerio.ui.theme.PickerioTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PickerioTheme(
                darkTheme = false, // Start with light theme
                dynamicColor = true // Enable Material You on Android 12+
            ) {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeScreen(
                        props = HomeScreenProps(
                            onGetStarted = {
                                // TODO: Implement navigation to camera/gallery screen
                                // For now, just show a toast or log
                                // In the future, you'll add navigation like:
                                // navController.navigate(Screen.Camera.route)
                            }
                        )
                    )
                }
            }
        }
    }
}