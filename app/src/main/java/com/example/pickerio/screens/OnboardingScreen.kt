// OnboardingScreen.kt
@file:OptIn(ExperimentalAnimationApi::class)

package com.example.pickerio.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.zIndex
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
 import androidx.compose.animation.core.*
 import androidx.compose.foundation.Canvas
 import androidx.compose.ui.geometry.CornerRadius
 import kotlin.math.PI


data class OnboardingSlide(
    val id: Int,
    val title: String,
    val subtitle: String,
    val description: String,
    val accent: Color,
    val secondary: Color,
    val illustration: IllustrationType
)

enum class IllustrationType {
    EYE, CAMERA, TOUCH, PALETTE
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    val slides = listOf(
        OnboardingSlide(
            id = 1,
            title = "See the World",
            subtitle = "in Color",
            description = "Every surface, every shadow, every ray of light holds a story told through color.",
            accent = Color(0xFFE63946),
            secondary = Color(0xFFF4A261),
            illustration = IllustrationType.EYE
        ),
        OnboardingSlide(
            id = 2,
            title = "Capture",
            subtitle = "Any Moment",
            description = "Snap a photo or choose from your gallery. Your canvas awaits discovery.",
            accent = Color(0xFF2A9D8F),
            secondary = Color(0xFF264653),
            illustration = IllustrationType.CAMERA
        ),
        OnboardingSlide(
            id = 3,
            title = "Touch to",
            subtitle = "Discover",
            description = "Simply tap anywhere on your image. Watch as colors reveal their true identity.",
            accent = Color(0xFF9B5DE5),
            secondary = Color(0xFFF15BB5),
            illustration = IllustrationType.TOUCH
        ),
        OnboardingSlide(
            id = 4,
            title = "Build Your",
            subtitle = "Palette",
            description = "Create stunning color collections. Learn the art and science behind every hue.",
            accent = Color(0xFFF4A261),
            secondary = Color(0xFFE9C46A),
            illustration = IllustrationType.PALETTE
        )
    )

    var currentSlide by remember { mutableIntStateOf(0) }
    val slide = slides[currentSlide]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0F))
    ) {
        // Animated background blobs
        AnimatedBackgroundBlobs(accent = slide.accent, secondary = slide.secondary)

        // Skip button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .zIndex(1f),
            contentAlignment = Alignment.TopEnd
        ) {
            TextButton(
                onClick = onComplete,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color.White.copy(alpha = 0.7f)
                )
            ) {
                Text("Skip", style = MaterialTheme.typography.bodyMedium)
            }
        }

        // Main content
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(64.dp))

            // Illustration with animation
            AnimatedContent(
                targetState = currentSlide,
                transitionSpec = {
                    fadeIn(animationSpec = tween(600)) +
                            slideInHorizontally(animationSpec = tween(600)) { -40 } with
                            fadeOut(animationSpec = tween(600)) +
                            slideOutHorizontally(animationSpec = tween(600)) { 40 }
                },
                modifier = Modifier.weight(1f)
            ) { slideIndex ->
                when (slides[slideIndex].illustration) {
                    IllustrationType.EYE -> EyeIllustration(
                        accent = slides[slideIndex].accent,
                        secondary = slides[slideIndex].secondary
                    )
                    IllustrationType.CAMERA -> CameraIllustration(
                        accent = slides[slideIndex].accent,
                        secondary = slides[slideIndex].secondary
                    )
                    IllustrationType.TOUCH -> TouchIllustration(
                        accent = slides[slideIndex].accent,
                        secondary = slides[slideIndex].secondary
                    )
                    IllustrationType.PALETTE -> PaletteIllustration(
                        accent = slides[slideIndex].accent,
                        secondary = slides[slideIndex].secondary
                    )
                }
            }

            // Text content
            AnimatedContent(
                targetState = currentSlide,
                transitionSpec = {
                    fadeIn(animationSpec = tween(600, delayMillis = 100)) +
                            slideInVertically(animationSpec = tween(600, delayMillis = 100)) { 20 } with
                            fadeOut(animationSpec = tween(600)) +
                            slideOutVertically(animationSpec = tween(600)) { -20 }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            ) { slideIndex ->
                val current = slides[slideIndex]
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = current.title,
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = current.subtitle,
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            color = current.accent
                        ),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = current.description,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color.White.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Light
                        ),
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )
                }
            }

            // Bottom section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .padding(bottom = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Progress dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    slides.forEachIndexed { index, _ ->
                        val isActive = index == currentSlide
                        val width by animateDpAsState(
                            targetValue = if (isActive) 32.dp else 8.dp,
                            animationSpec = tween(500),
                            label = "progressDot"
                        )

                        val color by animateColorAsState(
                            targetValue = if (isActive) slide.accent else Color.White.copy(alpha = 0.3f),
                            animationSpec = tween(500),
                            label = "progressColor"
                        )

                        Box(
                            modifier = Modifier
                                .height(4.dp)
                                .width(width)
                                .clip(RoundedCornerShape(2.dp))
                                .background(color)
                                .drawBehind {
                                    if (isActive) {
                                        val shimmerWidth = size.width * 0.3f
                                        drawRect(
                                            brush = Brush.linearGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    Color.White.copy(alpha = 0.5f),
                                                    Color.Transparent
                                                ),
                                                start = Offset(-shimmerWidth, 0f),
                                                end = Offset(shimmerWidth, 0f)
                                            ),
                                            topLeft = Offset(0f, 0f),
                                            size = Size(size.width, size.height)
                                        )
                                    }
                                }
                        )
                    }
                }

                // Continue button
                Button(
                    onClick = {
                        if (currentSlide < slides.size - 1) {
                            currentSlide++
                        } else {
                            onComplete()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = slide.accent,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 8.dp,
                        pressedElevation = 4.dp
                    )
                ) {
                    if (currentSlide == slides.size - 1) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Start Exploring",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )
                    } else {
                        Text(
                            "Continue",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedBackgroundBlobs(accent: Color, secondary: Color) {
    val infiniteTransition = rememberInfiniteTransition()

    val blob1Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "blob1"
    )

    val blob2Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "blob2"
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Blob 1
        Box(
            modifier = Modifier
                .offset(
                    x = 200.dp + (100 * cos(blob1Offset)).dp,
                    y = 100.dp + (80 * sin(blob1Offset * 0.7f)).dp
                )
                .size(400.dp)
                .drawBehind {
                    drawCircle(
                        color = accent.copy(alpha = 0.15f),
                        radius = size.minDimension / 2,
                        center = center
                    )
                }
        )

        // Blob 2
        Box(
            modifier = Modifier
                .offset(
                    x = (-100 + 150 * cos(blob2Offset * 0.5f)).dp,
                    y = 400.dp + (100 * sin(blob2Offset * 0.3f)).dp
                )
                .size(300.dp)
                .drawBehind {
                    drawCircle(
                        color = secondary.copy(alpha = 0.1f),
                        radius = size.minDimension / 2,
                        center = center
                    )
                }
        )
    }
}

@Composable
private fun EyeIllustration(accent: Color, secondary: Color) {
    val infiniteTransition = rememberInfiniteTransition()

    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "eyePulse"
    )

    Box(
        modifier = Modifier.size(256.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                accent.copy(alpha = 0.3f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = size.minDimension / 2
                        ),
                        radius = size.minDimension / 2,
                        center = center
                    )
                }
        )

        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            // Eye outline
            drawPath(
                path = Path().apply {
                    moveTo(size.width * 0.5f, size.height * 0.25f)
                    quadraticBezierTo(
                        size.width * 0.75f,
                        size.height * 0.5f,
                        size.width * 0.5f,
                        size.height * 0.75f
                    )
                    quadraticBezierTo(
                        size.width * 0.25f,
                        size.height * 0.5f,
                        size.width * 0.5f,
                        size.height * 0.25f
                    )
                    close()
                },
                color = accent,
                style = Stroke(width = 3.dp.toPx()),
                alpha = 0.8f
            )

            // Iris
            drawCircle(
                color = accent,
                radius = size.minDimension * 0.175f,
                center = center,
                alpha = 0.9f
            )

            // Pupil
            drawCircle(
                color = Color(0xFF1A1A2E),
                radius = size.minDimension * 0.09f,
                center = center
            )

            // Light reflections
            drawCircle(
                color = Color.White,
                radius = size.minDimension * 0.03f,
                center = Offset(center.x * 1.08f, center.y * 0.92f),
                alpha = 0.9f
            )

            drawCircle(
                color = Color.White,
                radius = size.minDimension * 0.015f,
                center = Offset(center.x * 0.94f, center.y * 1.04f),
                alpha = 0.6f
            )

            // Color spectrum around iris
            for (i in 0 until 8) {
                val angle = i * 45f
                val radius = size.minDimension * 0.225f
                val x = center.x + radius * cos(Math.toRadians(angle.toDouble())).toFloat()
                val y = center.y + radius * sin(Math.toRadians(angle.toDouble())).toFloat()

                val colors = listOf(
                    Color(0xFFE63946),
                    Color(0xFFF4A261),
                    Color(0xFFE9C46A),
                    Color(0xFF2A9D8F),
                    Color(0xFF264653),
                    Color(0xFF9B5DE5),
                    Color(0xFFF15BB5),
                    Color(0xFF00BBF9)
                )

                val pulseDelay = i * 0.1f
                val animatedRadius = size.minDimension * 0.025f * pulse

                drawCircle(
                    color = colors[i],
                    radius = animatedRadius,
                    center = Offset(x, y),
                    alpha = 0.8f
                )
            }
        }
    }
}

@Composable
private fun CameraIllustration(accent: Color, secondary: Color) {
    val infiniteTransition = rememberInfiniteTransition()

    val flashAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flash"
    )

    val lensPulse by infiniteTransition.animateFloat(
        initialValue = 25f,
        targetValue = 27f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "lens"
    )

    Box(
        modifier = Modifier.size(256.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                accent.copy(alpha = 0.2f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = size.minDimension / 2
                        ),
                        radius = size.minDimension / 2,
                        center = center
                    )
                }
        )

        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            // Camera body
            drawRoundRect(
                color = accent,
                topLeft = Offset(size.width * 0.175f, size.height * 0.325f),
                size = Size(size.width * 0.65f, size.height * 0.45f),
                cornerRadius = CornerRadius(12.dp.toPx())
            )

            // Top bump
            drawRoundRect(
                color = secondary,
                topLeft = Offset(size.width * 0.35f, size.height * 0.25f),
                size = Size(size.width * 0.2f, size.height * 0.1f),
                cornerRadius = CornerRadius(4.dp.toPx())
            )

            // Lens outer ring
            drawCircle(
                color = Color(0xFF1A1A2E),
                radius = size.minDimension * 0.19f,
                center = center
            )

            drawCircle(
                color = Color(0xFF2A2A4E),
                radius = size.minDimension * 0.16f,
                center = center
            )

            // Lens inner
            drawCircle(
                color = Color(0xFF1A1A2E),
                radius = lensPulse,
                center = center
            )

            // Rainbow reflection on lens
            rotate(-30f) {
                drawOval(
                    brush = Brush.linearGradient(
                        colors = listOf(accent, secondary),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height)
                    ),
                    topLeft = Offset(center.x * 0.95f, center.y * 0.8f),
                    size = Size(size.width * 0.12f, size.height * 0.08f),
                    alpha = 0.4f
                )
            }

            // Flash
            drawRoundRect(
                color = Color(0xFFE9C46A),
                topLeft = Offset(size.width * 0.65f, size.height * 0.36f),
                size = Size(size.width * 0.1f, size.height * 0.06f),
                cornerRadius = CornerRadius(2.dp.toPx()),
                alpha = flashAlpha
            )

            // Shutter button
            drawCircle(
                color = accent,
                radius = size.minDimension * 0.04f,
                center = Offset(size.width * 0.725f, size.height * 0.29f)
            )

            // Photos flying out
            val photos = listOf(
                Triple(size.width * 0.125f, size.height * 0.2f, -20f),
                Triple(size.width * 0.8f, size.height * 0.175f, 15f),
                Triple(size.width * 0.85f, size.height * 0.7f, 25f)
            )

            val photoColors = listOf(
                Color(0xFFE63946),
                Color(0xFF2A9D8F),
                Color(0xFFF4A261)
            )

            photos.forEachIndexed { i, (x, y, rotation) ->
                rotate(rotation, Offset(x + size.width * 0.06f, y + size.height * 0.075f)) {
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(x, y),
                        size = Size(size.width * 0.12f, size.height * 0.15f),
                        cornerRadius = CornerRadius(2.dp.toPx())
                    )

                    drawRoundRect(
                        color = photoColors[i],
                        topLeft = Offset(x + size.width * 0.015f, y + size.height * 0.015f),
                        size = Size(size.width * 0.09f, size.height * 0.07f),
                        cornerRadius = CornerRadius(1.dp.toPx())
                    )
                }
            }
        }
    }
}

@Composable
private fun TouchIllustration(accent: Color, secondary: Color) {
    val infiniteTransition = rememberInfiniteTransition()

    // Create arrays to store animation values
    val rippleAlphas = remember { mutableListOf<State<Float>>() }
    val rippleRadii = remember { mutableListOf<State<Float>>() }
    val particlePulses = remember { mutableListOf<State<Float>>() }
    val particleAlphas = remember { mutableListOf<State<Float>>() }

    // Initialize animations (this runs only once)
    if (rippleAlphas.isEmpty()) {
        for (i in 0 until 3) {
            rippleAlphas.add(
                infiniteTransition.animateFloat(
                    initialValue = 0.6f,
                    targetValue = 0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500, delayMillis = (i * 500).toLong().toInt()),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "rippleAlpha$i"
                )
            )

            rippleRadii.add(
                infiniteTransition.animateFloat(
                    initialValue = 0.1f,
                    targetValue = 0.3f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500, delayMillis = (i * 500).toLong().toInt()),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "rippleRadius$i"
                )
            )
        }

        for (i in 0 until 8) {
            particlePulses.add(
                infiniteTransition.animateFloat(
                    initialValue = 0.02f,
                    targetValue = 0.04f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500, delayMillis = (i * 100).toLong().toInt()),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "particlePulse$i"
                )
            )

            particleAlphas.add(
                infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 0.5f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500, delayMillis = (i * 100).toLong().toInt()),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "particleAlpha$i"
                )
            )
        }
    }

    val touchPulse = infiniteTransition.animateFloat(
        initialValue = 0.075f,
        targetValue = 0.09f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "touchPulse"
    )

    Box(
        modifier = Modifier.size(256.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                accent.copy(alpha = 0.2f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = size.minDimension / 2
                        ),
                        radius = size.minDimension / 2,
                        center = center
                    )
                }
        )

        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            // Ripple effects
            for (i in 0 until 3) {
                drawCircle(
                    color = accent,
                    radius = size.minDimension * rippleRadii[i].value,
                    center = center,
                    style = Stroke(width = 2.dp.toPx()),
                    alpha = rippleAlphas[i].value
                )
            }

            // Center touch point
            drawCircle(
                brush = Brush.linearGradient(
                    colors = listOf(accent, secondary),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, size.height)
                ),
                radius = size.minDimension * touchPulse.value,
                center = center
            )

            // Hand/finger
            drawPath(
                path = Path().apply {
                    moveTo(size.width * 0.475f, size.height * 0.425f)
                    quadraticBezierTo(
                        size.width * 0.5f,
                        size.height * 0.35f,
                        size.width * 0.525f,
                        size.height * 0.425f
                    )
                    lineTo(size.width * 0.54f, size.height * 0.65f)
                    quadraticBezierTo(
                        size.width * 0.5f,
                        size.height * 0.7f,
                        size.width * 0.46f,
                        size.height * 0.65f
                    )
                    lineTo(size.width * 0.475f, size.height * 0.425f)
                },
                brush = Brush.linearGradient(
                    colors = listOf(accent, secondary),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, size.height)
                ),
                alpha = 0.9f
            )

            // Color particles
            val particleColors = listOf(
                Color(0xFFE63946),
                Color(0xFFF4A261),
                Color(0xFFE9C46A),
                Color(0xFF2A9D8F),
                Color(0xFF264653),
                Color(0xFF9B5DE5),
                Color(0xFFF15BB5),
                Color(0xFF00BBF9)
            )

            for (i in 0 until 8) {
                val angle = i * 45f
                val baseRadius = size.minDimension * 0.25f + (i % 5) * size.minDimension * 0.01f
                val x = center.x + baseRadius * cos(Math.toRadians(angle.toDouble())).toFloat()
                val y = center.y + baseRadius * sin(Math.toRadians(angle.toDouble())).toFloat()

                drawCircle(
                    color = particleColors[i],
                    radius = size.minDimension * particlePulses[i].value,
                    center = Offset(x, y),
                    alpha = particleAlphas[i].value
                )
            }
        }
    }
}

@Composable
private fun PaletteIllustration(accent: Color, secondary: Color) {
    val infiniteTransition = rememberInfiniteTransition()

    // Create blob animations outside Canvas
    val blobPulse1 = infiniteTransition.animateFloat(
        initialValue = 0.07f,
        targetValue = 0.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob1"
    )
    val blobPulse2 = infiniteTransition.animateFloat(
        initialValue = 0.07f,
        targetValue = 0.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, delayMillis = 300),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob2"
    )
    val blobPulse3 = infiniteTransition.animateFloat(
        initialValue = 0.07f,
        targetValue = 0.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, delayMillis = 600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob3"
    )
    val blobPulse4 = infiniteTransition.animateFloat(
        initialValue = 0.07f,
        targetValue = 0.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, delayMillis = 900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob4"
    )
    val blobPulse5 = infiniteTransition.animateFloat(
        initialValue = 0.07f,
        targetValue = 0.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, delayMillis = 1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob5"
    )
    val blobPulse6 = infiniteTransition.animateFloat(
        initialValue = 0.07f,
        targetValue = 0.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, delayMillis = 1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob6"
    )

    // Create float animations outside Canvas
    val floatOffset1 = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float1"
    )
    val floatOffset2 = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, delayMillis = 500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float2"
    )
    val floatOffset3 = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, delayMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float3"
    )

    Box(
        modifier = Modifier.size(256.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                accent.copy(alpha = 0.2f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = size.minDimension / 2
                        ),
                        radius = size.minDimension / 2,
                        center = center
                    )
                }
        )

        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            // Artist palette
            drawOval(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFFAF5F0), Color(0xFFF0E6D8)),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, size.height)
                ),
                topLeft = Offset(size.width * 0.15f, size.height * 0.225f),
                size = Size(size.width * 0.7f, size.height * 0.6f)
            )

            // Thumb hole
            drawOval(
                color = Color(0xFFF9F4EF),
                topLeft = Offset(size.width * 0.225f, size.height * 0.5f),
                size = Size(size.width * 0.15f, size.height * 0.12f)
            )

            drawOval(
                color = Color(0xFFE0D5C8),
                topLeft = Offset(size.width * 0.225f, size.height * 0.5f),
                size = Size(size.width * 0.15f, size.height * 0.12f),
                style = Stroke(width = 2.dp.toPx())
            )

            // Paint blobs
            val paintBlobs = listOf(
                Triple(size.width * 0.425f, size.height * 0.325f, Color(0xFFE63946)),
                Triple(size.width * 0.575f, size.height * 0.3f, Color(0xFFF4A261)),
                Triple(size.width * 0.7f, size.height * 0.375f, Color(0xFFE9C46A)),
                Triple(size.width * 0.75f, size.height * 0.5f, Color(0xFF2A9D8F)),
                Triple(size.width * 0.7f, size.height * 0.625f, Color(0xFF264653)),
                Triple(size.width * 0.575f, size.height * 0.7f, Color(0xFF9B5DE5))
            )

            // Create list of blob pulse animations
            val blobPulses = listOf(
                blobPulse1, blobPulse2, blobPulse3,
                blobPulse4, blobPulse5, blobPulse6
            )

            paintBlobs.forEachIndexed { i, (x, y, color) ->
                drawCircle(
                    color = color,
                    radius = size.minDimension * blobPulses[i].value,
                    center = Offset(x, y)
                )
            }

            // Paint brush
            rotate(45f) {
                // Brush handle
                drawRoundRect(
                    color = accent,
                    topLeft = Offset(size.width * 0.775f, size.height * 0.15f),
                    size = Size(size.width * 0.04f, size.height * 0.25f),
                    cornerRadius = CornerRadius(2.dp.toPx())
                )

                // Brush ferrule
                drawRoundRect(
                    color = Color(0xFFC0C0C0),
                    topLeft = Offset(size.width * 0.74f, size.height * 0.35f),
                    size = Size(size.width * 0.07f, size.height * 0.05f),
                    cornerRadius = CornerRadius(1.dp.toPx())
                )

                // Brush bristles
                drawOval(
                    color = secondary,
                    topLeft = Offset(size.width * 0.725f, size.height * 0.44f),
                    size = Size(size.width * 0.04f, size.height * 0.06f)
                )
            }

            // Floating color swatches
            val swatches = listOf(
                Triple(size.width * 0.1f, size.height * 0.25f, Color(0xFFE63946)),
                Triple(size.width * 0.85f, size.height * 0.3f, Color(0xFF2A9D8F)),
                Triple(size.width * 0.15f, size.height * 0.75f, Color(0xFFF4A261))
            )

            // Create list of float animations
            val floatOffsets = listOf(floatOffset1, floatOffset2, floatOffset3)

            swatches.forEachIndexed { i, (x, y, color) ->
                translate(0f, -floatOffsets[i].value) {
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(x, y),
                        size = Size(size.width * 0.1f, size.height * 0.125f),
                        cornerRadius = CornerRadius(3.dp.toPx())
                    )

                    drawRoundRect(
                        color = color,
                        topLeft = Offset(x + size.width * 0.01f, y + size.height * 0.01f),
                        size = Size(size.width * 0.08f, size.height * 0.06f),
                        cornerRadius = CornerRadius(1.dp.toPx())
                    )
                }
            }
        }
    }
}

// Don't forget to add necessary imports:
// import androidx.compose.animation.core.*
// import androidx.compose.foundation.Canvas
// import androidx.compose.ui.geometry.CornerRadius
// import kotlin.math.PI