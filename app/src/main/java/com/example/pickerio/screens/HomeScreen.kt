package com.example.pickerio.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlin.math.cos
import kotlin.math.sin

data class HomeScreenProps(
    val onGetStarted: () -> Unit
)

@Composable
fun HomeScreen(props: HomeScreenProps) {
    val systemUiController = rememberSystemUiController()

    // Set status bar/navigation bar colors
    LaunchedEffect(Unit) {
        systemUiController.setStatusBarColor(Color.Transparent, darkIcons = false)
        systemUiController.setNavigationBarColor(Color.Transparent, darkIcons = false)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Animated background blobs
        AnimatedBlobs()

        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top content area - centered
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Animated palette icon
                FloatingPaletteIcon()

                Spacer(modifier = Modifier.height(40.dp))

                // Title and description
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = buildAnnotatedString {
                            append("Picker")
                            withStyle(style = SpanStyle(
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                color = MaterialTheme.colorScheme.primary
                            )) {
                                append("io")
                            }
                        },
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 56.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        ),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Capture the palette of the world around you. Every moment holds a masterpiece of color.",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 18.sp,
                            lineHeight = 28.sp,
                            fontWeight = FontWeight.Light
                        ),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(0.9f)
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Color swatches
                FloatingColorSwatches()

                Spacer(modifier = Modifier.height(40.dp))

                // Feature tags
                Row(
                    modifier = Modifier
                        .wrapContentWidth()
                        .fillMaxWidth(0.9f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FeatureTag("Snap a photo")
                    Spacer(modifier = Modifier.width(12.dp))
                    FeatureTag("Touch to discover")
                    Spacer(modifier = Modifier.width(12.dp))
                    FeatureTag("Save your palette")
                }
            }

            // Bottom button area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = props.onGetStarted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .shadow(
                            elevation = 16.dp,
                            shape = RoundedCornerShape(16.dp),
                            spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onBackground,
                        contentColor = MaterialTheme.colorScheme.background
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp
                    )
                ) {
                    Text(
                        text = "Begin Your Discovery",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Text(
                    text = "No account needed. Simply explore.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    ),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun AnimatedBlobs() {
    val infiniteTransition = rememberInfiniteTransition()

    val blob1Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * 3.14159f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 20000, easing = LinearEasing)
        )
    )

    val blob2Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * 3.14159f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 17000, easing = LinearEasing),
            initialStartOffset = StartOffset(2000)
        )
    )

    val blob3Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * 3.14159f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 22000, easing = LinearEasing),
            initialStartOffset = StartOffset(4000)
        )
    )

    val blob4Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * 3.14159f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            initialStartOffset = StartOffset(6000)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Blob 1
        Box(
            modifier = Modifier
                .offset(
                    x = (-80).dp + (40 * cos(blob1Offset)).dp,
                    y = (-80).dp + (30 * sin(blob1Offset)).dp
                )
                .size(256.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
        )

        // Blob 2
        Box(
            modifier = Modifier
                .offset(
                    x = (0.75f * LocalDensity.current.density * 360 - 64).dp +
                            (20 * cos(blob2Offset + 1.5f)).dp,
                    y = (0.25f * LocalDensity.current.density * 360).dp +
                            (25 * sin(blob2Offset + 1.5f)).dp
                )
                .size(192.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
        )

        // Blob 3
        Box(
            modifier = Modifier
                .offset(
                    x = (-40).dp + (15 * cos(blob3Offset + 1f)).dp,
                    y = (0.75f * LocalDensity.current.density * 360 - 40).dp +
                            (20 * sin(blob3Offset + 1f)).dp
                )
                .size(160.dp)
                .clip(CircleShape)
                .background(Color(0xFF2A9D8F).copy(alpha = 0.1f))
        )

        // Blob 4
        Box(
            modifier = Modifier
                .offset(
                    x = (0.5f * LocalDensity.current.density * 360 - 40).dp +
                            (30 * cos(blob4Offset + 2f)).dp,
                    y = (LocalDensity.current.density * 360 - 40).dp +
                            (25 * sin(blob4Offset + 2f)).dp
                )
                .size(224.dp)
                .clip(CircleShape)
                .background(Color(0xFF264653).copy(alpha = 0.1f))
        )
    }
}

@Composable
private fun FloatingPaletteIcon() {
    val infiniteTransition = rememberInfiniteTransition()

    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * 3.14159f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing)
        )
    )

    val yOffset = (10 * sin(floatOffset)).dp

    Box(
        modifier = Modifier
            .size(128.dp)
            .offset(y = yOffset),
        contentAlignment = Alignment.Center
    ) {
        // Palette ellipse with gradient
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Main ellipse
            drawCircle(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFF2E8DF),
                        Color(0xFFE0D6CC)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, size.height)
                ),
                radius = size.minDimension * 0.4f,
                center = center
            )

            // Paint dots
            drawCircle(color = Color(0xFFE63946), radius = 12f, center = center + Offset(-32f, -20f))
            drawCircle(color = Color(0xFFF4A261), radius = 10f, center = center + Offset(0f, -28f))
            drawCircle(color = Color(0xFF2A9D8F), radius = 12f, center = center + Offset(32f, -20f))
            drawCircle(color = Color(0xFF264653), radius = 10f, center = center + Offset(40f, 0f))
            drawCircle(color = Color(0xFFE9C46A), radius = 9f, center = center + Offset(24f, 32f))

            // Thumb hole
            drawCircle(
                color = Color(0xFFFAF7F4),
                radius = 16f,
                center = center + Offset(-20f, 16f)
            )

            // Outline
            drawCircle(
                color = Color(0xFF735C3D),
                radius = size.minDimension * 0.4f,
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
            )
        }
    }
}

@Composable
private fun FloatingColorSwatches() {
    val swatches = listOf(
        SwatchData(Color(0xFFE63946), 0.05f, 0.1f, 56.dp, -12f),
        SwatchData(Color(0xFFF4A261), 0.22f, 0.35f, 48.dp, 8f),
        SwatchData(Color(0xFFE9C46A), 0.42f, 0.05f, 64.dp, -5f),
        SwatchData(Color(0xFF2A9D8F), 0.60f, 0.30f, 52.dp, 15f),
        SwatchData(Color(0xFF264653), 0.80f, 0.10f, 56.dp, -8f)
    )

    val infiniteTransition = rememberInfiniteTransition()
    val delays = listOf(0, 500, 1000, 1500, 2000)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .padding(horizontal = 32.dp)
    ) {
        swatches.forEachIndexed { index, swatch ->
            val floatOffset by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 2 * 3.14159f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 3000, easing = LinearEasing),
                    initialStartOffset = StartOffset(delays[index])
                )
            )

            val yOffset = (4 * sin(floatOffset)).dp

            Box(
                modifier = Modifier
                    .offset(
                        x = swatch.x * (LocalDensity.current.density * 360 - swatch.size.value).dp,
                        y = swatch.y * 96.dp + yOffset
                    )
                    .size(swatch.size)
                    .rotate(swatch.rotation)
                    .clip(RoundedCornerShape(16.dp))
                    .background(swatch.color)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(16.dp)
                    )
            )
        }
    }
}

@Composable
private fun FeatureTag(text: String) {
    Surface(
        modifier = Modifier
            .wrapContentWidth()
            .shadow(2.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

private data class SwatchData(
    val color: Color,
    val x: Float, // percentage of container width
    val y: Float, // percentage of container height
    val size: Dp,
    val rotation: Float
)

// Preview for Android Studio
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            HomeScreen(props = HomeScreenProps(onGetStarted = {}))
        }
    }
}