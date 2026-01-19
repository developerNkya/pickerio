package com.example.pickerio.screens

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlin.math.cos
import kotlin.math.sin

data class ImageSelectorProps(
    val onImageSelect: (Uri) -> Unit,
    val onBack: () -> Unit
)

// Updated color palette with blackish text colors
val CustomBackgroundColor = Color(0xFFFEF7F2) // Base background
val WarmGrayColor = Color(0xFFE8DED4) // Bold gray - matches FEF7F2 warmth
val WarmPrimaryColor = Color(0xFFD4A574) // Warm primary (brownish)
val WarmSecondaryColor = Color(0xFFA38B6D) // Warm secondary
val DottedBorderColor = Color(0xFFD9CABE) // Dotted border - matches FEF7F2
val IconBackgroundColor = Color(0xFFEDE4D9) // Icon background - bold version of FEF7F2

// Blackish text colors with warm undertones
val DarkTextColor = Color(0xFF3A3329) // Dark brownish-black for main text
val MediumTextColor = Color(0xFF5C5346) // Medium brownish for secondary text
val LightTextColor = Color(0xFF7D7568) // Light brownish for subtle text

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ImageSelector(props: ImageSelectorProps) {
    val context = LocalContext.current
    var isDragging by remember { mutableStateOf(false) }

    // Permission for camera
    val cameraPermissionState = rememberPermissionState(
        Manifest.permission.CAMERA
    )

    // Launcher for gallery picker
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let { props.onImageSelect(it) }
        }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CustomBackgroundColor)
            .padding(top = 32.dp)
    ) {
        // Animated background blobs
        AnimatedBackgroundBlobs()

        // Main content
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            HeaderSection(onBack = props.onBack)

            // Content area
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Drop zone for images
                ImageDropZone(
                    isDragging = isDragging,
                    onClick = {
                        // Open gallery
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onDragStart = { isDragging = true },
                    onDragEnd = { isDragging = false }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Divider with text
                OrDivider()

                Spacer(modifier = Modifier.height(32.dp))

                // Action buttons
                ActionButtons(
                    onGalleryClick = {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onCameraClick = {
                        if (cameraPermissionState.status.isGranted) {
                            // Camera logic would go here
                            // For now, just open gallery
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        } else {
                            cameraPermissionState.launchPermissionRequest()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun AnimatedBackgroundBlobs() {
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
            initialStartOffset = StartOffset(3000)
        )
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Blob 1 - Top right
        Box(
            modifier = Modifier
                .offset(
                    x = (0.8f * LocalDensity.current.density * 360).dp + (20 * cos(blob1Offset)).dp,
                    y = 80.dp + (20 * sin(blob1Offset)).dp
                )
                .size(128.dp)
                .clip(CircleShape)
                .background(WarmPrimaryColor.copy(alpha = 0.05f))
        )

        // Blob 2 - Bottom left
        Box(
            modifier = Modifier
                .offset(
                    x = 20.dp + (10 * cos(blob2Offset + 1f)).dp,
                    y = (0.6f * LocalDensity.current.density * 360).dp + (15 * sin(blob2Offset + 1f)).dp
                )
                .size(96.dp)
                .clip(CircleShape)
                .background(WarmSecondaryColor.copy(alpha = 0.05f))
        )
    }
}

@Composable
private fun HeaderSection(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        // Back button
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(WarmGrayColor)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = MediumTextColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Title and subtitle
        Column {
            Text(
                text = "Choose Your Canvas",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Serif
                ),
                color = DarkTextColor // Blackish main title
            )

            Text(
                text = "Select an image to explore its colors",
                style = MaterialTheme.typography.bodySmall,
                color = MediumTextColor // Blackish secondary text
            )
        }
    }
}

@Composable
private fun ImageDropZone(
    isDragging: Boolean,
    onClick: () -> Unit,
    onDragStart: () -> Unit,
    onDragEnd: () -> Unit
) {
    val borderColor = if (isDragging) {
        WarmPrimaryColor
    } else {
        DottedBorderColor
    }

    val backgroundColor = if (isDragging) {
        WarmPrimaryColor.copy(alpha = 0.1f)
    } else {
        WarmGrayColor.copy(alpha = 0.3f)
    }

    var isHovered by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(4f / 3f)
            .clip(RoundedCornerShape(24.dp))
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(24.dp)
            )
            .clickable { onClick() }
            .scale(if (isDragging) 1.02f else 1f)
            .drawBehind {
                // Draw dotted border with rounded corners
                drawDottedBorder(
                    color = borderColor,
                    strokeWidth = 3.dp.toPx(),
                    cornerRadius = 24.dp.toPx()
                )
            }
            .pointerInput(Unit) {
                // Handle drag states
            },
        contentAlignment = Alignment.Center
    ) {
        // Decorative corners with slight radius
        RoundedCorners()

        // Shimmer effect on hover
        if (isHovered) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                WarmPrimaryColor.copy(alpha = 0.1f),
                                Color.Transparent
                            ),
                            start = Offset(-100f, -100f),
                            end = Offset(100f, 100f)
                        )
                    )
                    .clip(RoundedCornerShape(24.dp))
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            // Icon with sparkle
            Box(
                contentAlignment = Alignment.Center
            ) {
                // Icon with bold version of FEF7F2 background
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(IconBackgroundColor)
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Create,
                        contentDescription = "Image icon",
                        tint = MediumTextColor, // Blackish icon color
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Sparkle icon
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Sparkle",
                    tint = WarmPrimaryColor,
                    modifier = Modifier
                        .size(24.dp)
                        .offset(x = 30.dp, y = (-30).dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Text content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Drop your masterpiece here",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Serif
                    ),
                    color = DarkTextColor, // Blackish main text
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "or tap to browse your gallery",
                    style = MaterialTheme.typography.bodySmall,
                    color = MediumTextColor, // Blackish secondary text
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun DrawScope.drawDottedBorder(
    color: Color,
    strokeWidth: Float,
    cornerRadius: Float
) {
    val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

    drawRoundRect(
        color = color,
        style = Stroke(
            width = strokeWidth,
            pathEffect = dashPathEffect
        ),
        cornerRadius = CornerRadius(cornerRadius, cornerRadius)
    )
}

@Composable
private fun RoundedCorners(color: Color = WarmPrimaryColor.copy(alpha = 0.3f)) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        val cornerSize = 32.dp.toPx()
        val borderWidth = 2.dp.toPx()
        val cornerRadius = 8.dp.toPx()

        // Draw rounded corners instead of sharp corners

        // Top-left rounded corner
        drawLine(
            color = color,
            start = Offset(cornerRadius, 0f),
            end = Offset(cornerSize, 0f),
            strokeWidth = borderWidth
        )
        drawLine(
            color = color,
            start = Offset(0f, cornerRadius),
            end = Offset(0f, cornerSize),
            strokeWidth = borderWidth
        )
        // Draw rounded corner arc
        drawArc(
            color = color,
            startAngle = 180f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(0f, 0f),
            size = Size(cornerRadius * 2, cornerRadius * 2),
            style = Stroke(width = borderWidth)
        )

        // Top-right rounded corner
        drawLine(
            color = color,
            start = Offset(size.width - cornerSize, 0f),
            end = Offset(size.width - cornerRadius, 0f),
            strokeWidth = borderWidth
        )
        drawLine(
            color = color,
            start = Offset(size.width, cornerRadius),
            end = Offset(size.width, cornerSize),
            strokeWidth = borderWidth
        )
        // Draw rounded corner arc
        drawArc(
            color = color,
            startAngle = 270f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(size.width - cornerRadius * 2, 0f),
            size = Size(cornerRadius * 2, cornerRadius * 2),
            style = Stroke(width = borderWidth)
        )

        // Bottom-left rounded corner
        drawLine(
            color = color,
            start = Offset(0f, size.height - cornerSize),
            end = Offset(0f, size.height - cornerRadius),
            strokeWidth = borderWidth
        )
        drawLine(
            color = color,
            start = Offset(cornerRadius, size.height),
            end = Offset(cornerSize, size.height),
            strokeWidth = borderWidth
        )
        // Draw rounded corner arc
        drawArc(
            color = color,
            startAngle = 90f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(0f, size.height - cornerRadius * 2),
            size = Size(cornerRadius * 2, cornerRadius * 2),
            style = Stroke(width = borderWidth)
        )

        // Bottom-right rounded corner
        drawLine(
            color = color,
            start = Offset(size.width - cornerSize, size.height),
            end = Offset(size.width - cornerRadius, size.height),
            strokeWidth = borderWidth
        )
        drawLine(
            color = color,
            start = Offset(size.width, size.height - cornerSize),
            end = Offset(size.width, size.height - cornerRadius),
            strokeWidth = borderWidth
        )
        // Draw rounded corner arc
        drawArc(
            color = color,
            startAngle = 0f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(size.width - cornerRadius * 2, size.height - cornerRadius * 2),
            size = Size(cornerRadius * 2, cornerRadius * 2),
            style = Stroke(width = borderWidth)
        )
    }
}

@Composable
private fun OrDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            DottedBorderColor,
                            Color.Transparent
                        )
                    )
                )
        )

        Text(
            text = "or capture",
            style = MaterialTheme.typography.bodySmall.copy(
                fontStyle = FontStyle.Italic,
                fontFamily = FontFamily.Serif
            ),
            color = LightTextColor, // Blackish but lighter
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            DottedBorderColor,
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@Composable
private fun ActionButtons(
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Gallery Button
        ActionButton(
            icon = Icons.Default.Image,
            title = "Gallery",
            subtitle = "Choose existing",
            onClick = onGalleryClick,
            isPrimaryButton = true
        )

        // Camera Button
        ActionButton(
            icon = Icons.Default.Camera,
            title = "Camera",
            subtitle = "Take a photo",
            onClick = onCameraClick,
            isPrimaryButton = false
        )
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isPrimaryButton: Boolean
) {
    val borderColor = if (isPrimaryButton) {
        WarmPrimaryColor.copy(alpha = 0.3f)
    } else {
        WarmGrayColor.copy(alpha = 0.3f)
    }

    val hoverBorderColor = if (isPrimaryButton) {
        WarmPrimaryColor
    } else {
        WarmSecondaryColor
    }

    val hoverBackgroundColor = if (isPrimaryButton) {
        WarmPrimaryColor.copy(alpha = 0.05f)
    } else {
        WarmGrayColor.copy(alpha = 0.05f)
    }

    var isHovered by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 2.dp,
                color = if (isHovered) hoverBorderColor else borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .background(
                color = if (isHovered) hoverBackgroundColor else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .pointerInput(Unit) {
                // Handle hover state
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent()
                    }
                }
            },
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            // Icon container
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        color = if (isHovered) {
                            if (isPrimaryButton) {
                                WarmPrimaryColor.copy(alpha = 0.1f)
                            } else {
                                WarmGrayColor.copy(alpha = 0.1f)
                            }
                        } else {
                            IconBackgroundColor
                        }
                    )
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MediumTextColor, // Blackish icon color
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Text content
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = DarkTextColor // Blackish main text
                )

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MediumTextColor // Blackish secondary text
                )
            }
        }
    }
}

// Preview
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ImageSelectorPreview() {
    MaterialTheme {
        ImageSelector(
            props = ImageSelectorProps(
                onImageSelect = { /* Handle image selection */ },
                onBack = { /* Handle back navigation */ }
            )
        )
    }
}