@file:OptIn(androidx.media3.common.util.UnstableApi::class)

package com.example.pickerio.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color as AndroidColor
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.ColorInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import kotlin.math.sqrt

// Custom ColorInfo class since androidx.media3.common.ColorInfo is for video/audio
data class CustomColorInfo(
    val hex: String,
    val rgb: RGB,
    val name: String,
    val x: Int,
    val y: Int
)

data class RGB(
    val r: Int,
    val g: Int,
    val b: Int
)

data class ColorPickerProps(
    val imageUri: Uri,
    val onColorPick: (List<CustomColorInfo>) -> Unit,
    val onBack: () -> Unit
)

// Data class for magnifying glass effect
data class MagnifyingGlassState(
    val isVisible: Boolean = false,
    val position: Offset = Offset.Zero,
    val zoomLevel: Float = 2.0f,
    val capturedColor: CustomColorInfo? = null,
    val isCapturing: Boolean = false
)

@Composable
fun ColorPicker(props: ColorPickerProps) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // Color picking state
    var cursorPosition by remember { mutableStateOf<Offset?>(null) }
    var currentColor by remember { mutableStateOf<CustomColorInfo?>(null) }
    var pickedColors by remember { mutableStateOf<List<CustomColorInfo>>(emptyList()) }

    // Canvas size
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    // Magnifying glass state
    var magnifyingState by remember {
        mutableStateOf(MagnifyingGlassState())
    }

    // Load the image
    LaunchedEffect(props.imageUri) {
        isLoading = true
        error = null

        try {
            withContext(Dispatchers.IO) {
                val inputStream: InputStream? = context.contentResolver.openInputStream(props.imageUri)
                inputStream?.use { stream ->
                    val bitmap = BitmapFactory.decodeStream(stream)
                    if (bitmap != null) {
                        originalBitmap = bitmap
                        imageBitmap = bitmap.asImageBitmap()
                    } else {
                        error = "Failed to decode image"
                    }
                } ?: run {
                    error = "Failed to open image"
                }
            }
        } catch (e: Exception) {
            error = "Error loading image: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    // Color name lookup function
    fun getColorName(r: Int, g: Int, b: Int): String {
        val colors = listOf(
            NamedColor("Crimson", 220, 20, 60),
            NamedColor("Coral Reef", 255, 127, 80),
            NamedColor("Tangerine", 255, 165, 0),
            NamedColor("Golden Hour", 255, 215, 0),
            NamedColor("Sunflower", 255, 223, 0),
            NamedColor("Fresh Lime", 50, 205, 50),
            NamedColor("Forest", 34, 139, 34),
            NamedColor("Ocean Teal", 0, 128, 128),
            NamedColor("Aquamarine", 127, 255, 212),
            NamedColor("Sky Canvas", 135, 206, 235),
            NamedColor("Sapphire", 0, 0, 255),
            NamedColor("Midnight", 0, 0, 128),
            NamedColor("Royal Plum", 128, 0, 128),
            NamedColor("Wisteria", 186, 85, 211),
            NamedColor("Fuchsia", 255, 0, 255),
            NamedColor("Blush", 255, 192, 203),
            NamedColor("Rose Petal", 255, 0, 127),
            NamedColor("Sienna", 160, 82, 45),
            NamedColor("Sandstone", 210, 180, 140),
            NamedColor("Ivory", 255, 255, 240),
            NamedColor("Pure White", 255, 255, 255),
            NamedColor("Platinum", 229, 228, 226),
            NamedColor("Stone", 128, 128, 128),
            NamedColor("Graphite", 54, 69, 79),
            NamedColor("Obsidian", 0, 0, 0)
        )

        var minDistance = Double.POSITIVE_INFINITY
        var closestColor = "Unknown"

        for (color in colors) {
            val distance = sqrt(
                ((r - color.r) * (r - color.r) +
                        (g - color.g) * (g - color.g) +
                        (b - color.b) * (b - color.b)).toDouble()
            )
            if (distance < minDistance) {
                minDistance = distance
                closestColor = color.name
            }
        }

        return closestColor
    }

    // RGB to HEX conversion
    fun rgbToHex(r: Int, g: Int, b: Int): String {
        return String.format("#%02X%02X%02X", r, g, b)
    }

    // Get actual pixel color from bitmap
    fun getPixelColorAt(x: Int, y: Int): AndroidColor? {
        return try {
            if (originalBitmap != null &&
                x in 0 until originalBitmap!!.width &&
                y in 0 until originalBitmap!!.height) {

                val pixel = originalBitmap!!.getPixel(x, y)
                AndroidColor.valueOf(pixel)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    // Handle color pick
    fun handleColorPick(position: Offset) {
        if (imageBitmap == null || canvasSize == IntSize.Zero) return

        val scaleX = imageBitmap!!.width.toFloat() / canvasSize.width
        val scaleY = imageBitmap!!.height.toFloat() / canvasSize.height

        val pixelX = (position.x * scaleX).toInt()
        val pixelY = (position.y * scaleY).toInt()

        // Get actual pixel color
        val pixelColor = getPixelColorAt(pixelX, pixelY)
        if (pixelColor != null) {
            val r = AndroidColor.red(pixelColor.toArgb())
            val g = AndroidColor.green(pixelColor.toArgb())
            val b = AndroidColor.blue(pixelColor.toArgb())

            val colorInfo = CustomColorInfo(
                hex = rgbToHex(r, g, b),
                rgb = RGB(r, g, b),
                name = getColorName(r, g, b),
                x = pixelX,
                y = pixelY
            )

            // Start magnifying glass capture animation
            magnifyingState = magnifyingState.copy(
                isVisible = true,
                position = position,
                capturedColor = colorInfo,
                isCapturing = true
            )

            // Show magnifying glass for a moment, then add color
            coroutineScope.launch {
                delay(800) // Show magnifying glass for 0.8 seconds

                // Add the color to picked colors
                pickedColors = pickedColors + colorInfo

                // Hide magnifying glass with fade out
                magnifyingState = magnifyingState.copy(
                    isCapturing = false
                )

                delay(300) // Fade out duration
                magnifyingState = magnifyingState.copy(
                    isVisible = false,
                    capturedColor = null
                )
            }
        }
    }

    // Handle color preview (shows magnifying glass)
    fun handleColorPreview(position: Offset) {
        if (imageBitmap == null || canvasSize == IntSize.Zero) {
            cursorPosition = null
            currentColor = null
            magnifyingState = magnifyingState.copy(isVisible = false)
            return
        }

        cursorPosition = position

        val scaleX = imageBitmap!!.width.toFloat() / canvasSize.width
        val scaleY = imageBitmap!!.height.toFloat() / canvasSize.height

        val pixelX = (position.x * scaleX).toInt()
        val pixelY = (position.y * scaleY).toInt()

        // Get actual pixel color
        val pixelColor = getPixelColorAt(pixelX, pixelY)
        if (pixelColor != null) {
            val r = AndroidColor.red(pixelColor.toArgb())
            val g = AndroidColor.green(pixelColor.toArgb())
            val b = AndroidColor.blue(pixelColor.toArgb())

            currentColor = CustomColorInfo(
                hex = rgbToHex(r, g, b),
                rgb = RGB(r, g, b),
                name = getColorName(r, g, b),
                x = pixelX,
                y = pixelY
            )

            // Show magnifying glass for preview
            magnifyingState = magnifyingState.copy(
                isVisible = true,
                position = position,
                capturedColor = null,
                isCapturing = false
            )
        } else {
            currentColor = null
            magnifyingState = magnifyingState.copy(isVisible = false)
        }
    }

    // Handle mouse/touch release (hide magnifying glass)
    fun handleRelease() {
        if (!magnifyingState.isCapturing) {
            magnifyingState = magnifyingState.copy(isVisible = false)
        }
    }

    // Remove a color
    fun removeColor(index: Int) {
        pickedColors = pickedColors.filterIndexed { i, _ -> i != index }
    }

    // Handle done
    fun handleDone() {
        if (pickedColors.isNotEmpty()) {
            props.onColorPick(pickedColors)
        }
    }

    // Custom background color (same as previous screens)
    val customBackgroundColor = Color(0xFFFEF7F2)
    val darkTextColor = Color(0xFF3A3329)
    val mediumTextColor = Color(0xFF5C5346)
    val warmPrimaryColor = Color(0xFFD4A574)

    // Animation for magnifying glass capture
    val captureTransition = rememberInfiniteTransition()
    val capturePulse by captureTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(customBackgroundColor)
            .systemBarsPadding()
    ) {
        // Header
        ColorPickerHeader(
            onBack = props.onBack,
            pickedColorsCount = pickedColors.size,
            onDone = ::handleDone,
            darkTextColor = darkTextColor,
            mediumTextColor = mediumTextColor,
            warmPrimaryColor = warmPrimaryColor
        )

        // Main content area
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 70.dp)
        ) {
            // Canvas area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .shadow(
                        elevation = 25.dp,
                        shape = RoundedCornerShape(24.dp),
                        spotColor = Color.Black.copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    // Loading indicator
                    LoadingIndicator(mediumTextColor = mediumTextColor)
                } else if (error != null) {
                    // Error message
                    ErrorMessage(error = error!!, mediumTextColor = mediumTextColor)
                } else if (imageBitmap != null) {
                    // Image canvas with magnifying glass
                    ImageCanvasWithMagnifier(
                        imageBitmap = imageBitmap!!,
                        onSizeChanged = { size -> canvasSize = size },
                        onColorPreview = ::handleColorPreview,
                        onColorPick = ::handleColorPick,
                        onRelease = ::handleRelease,
                        cursorPosition = cursorPosition,
                        currentColor = currentColor,
                        pickedColors = pickedColors,
                        magnifyingState = magnifyingState,
                        capturePulse = if (magnifyingState.isCapturing) capturePulse else 1f,
                        onRemoveColor = ::removeColor,
                        darkTextColor = darkTextColor,
                        mediumTextColor = mediumTextColor
                    )
                }
            }

            // Palette section
            if (pickedColors.isNotEmpty()) {
                ColorPaletteSection(
                    pickedColors = pickedColors,
                    onRemoveColor = ::removeColor,
                    darkTextColor = darkTextColor,
                    mediumTextColor = mediumTextColor
                )
            }
        }
    }
}

@Composable
private fun ColorPickerHeader(
    onBack: () -> Unit,
    pickedColorsCount: Int,
    onDone: () -> Unit,
    darkTextColor: Color,
    mediumTextColor: Color,
    warmPrimaryColor: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 4.dp,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Back button and title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                // Back button
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE8DED4))
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = mediumTextColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Title and subtitle
                Column {
                    Text(
                        text = "Color Studio",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Serif
                        ),
                        color = darkTextColor
                    )

                    Text(
                        text = "Touch and hold to magnify, tap to capture",
                        style = MaterialTheme.typography.bodySmall,
                        color = mediumTextColor
                    )
                }
            }

            // Done button
            Button(
                onClick = onDone,
                enabled = pickedColorsCount > 0,
                modifier = Modifier.height(40.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = darkTextColor,
                    contentColor = Color.White,
                    disabledContainerColor = mediumTextColor.copy(alpha = 0.3f),
                    disabledContentColor = Color.White.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Done",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Done ($pickedColorsCount)",
                    style = MaterialTheme.typography.labelMedium,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun LoadingIndicator(mediumTextColor: Color) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = mediumTextColor,
            strokeWidth = 3.dp,
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Preparing your canvas...",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                fontFamily = FontFamily.Serif
            ),
            color = mediumTextColor
        )
    }
}

@Composable
private fun ErrorMessage(error: String, mediumTextColor: Color) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Error",
            tint = mediumTextColor,
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = mediumTextColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@Composable
private fun ImageCanvasWithMagnifier(
    imageBitmap: ImageBitmap,
    onSizeChanged: (IntSize) -> Unit,
    onColorPreview: (Offset) -> Unit,
    onColorPick: (Offset) -> Unit,
    onRelease: () -> Unit,
    cursorPosition: Offset?,
    currentColor: CustomColorInfo?,
    pickedColors: List<CustomColorInfo>,
    magnifyingState: MagnifyingGlassState,
    capturePulse: Float,
    onRemoveColor: (Int) -> Unit,
    darkTextColor: Color,
    mediumTextColor: Color
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        onColorPreview(offset)
                        tryAwaitRelease()
                        onRelease()
                    },
                    onTap = { offset ->
                        onColorPick(offset)
                    }
                )
            }
    ) {
        // Calculate image dimensions to maintain aspect ratio
        val imageWidth = imageBitmap.width.toFloat()
        val imageHeight = imageBitmap.height.toFloat()
        val containerWidth = constraints.maxWidth.toFloat()
        val containerHeight = constraints.maxHeight.toFloat()

        val scale = minOf(containerWidth / imageWidth, containerHeight / imageHeight)
        val displayWidth = imageWidth * scale
        val displayHeight = imageHeight * scale

        // Notify parent of actual canvas size
        LaunchedEffect(displayWidth, displayHeight) {
            onSizeChanged(IntSize(displayWidth.toInt(), displayHeight.toInt()))
        }

        // Image with optional dimming when magnifying glass is visible
        Image(
            bitmap = imageBitmap,
            contentDescription = "Selected image",
            modifier = Modifier
                .width(displayWidth.dp)
                .height(displayHeight.dp)
                .clip(RoundedCornerShape(16.dp))
                .graphicsLayer {
                    if (magnifyingState.isVisible) {
                        // Dim the background image when magnifying glass is shown
                        alpha = 0.7f
                    }
                },
            contentScale = ContentScale.FillBounds
        )

        // Picked color markers
        pickedColors.forEachIndexed { index, colorInfo ->
            // Calculate marker position based on original image coordinates
            val markerX = (colorInfo.x.toFloat() / imageWidth * displayWidth)
            val markerY = (colorInfo.y.toFloat() / imageHeight * displayHeight)

            val color = parseColorHex(colorInfo.hex)

            ColorMarker(
                index = index,
                color = color,
                position = Offset(markerX, markerY),
                onRemove = { onRemoveColor(index) }
            )
        }

        // Magnifying glass overlay - only show if cursor position is within image bounds
        if (magnifyingState.isVisible && cursorPosition != null) {
            // Check if cursor is within image bounds
            val isWithinImage = cursorPosition.x in 0f..displayWidth &&
                    cursorPosition.y in 0f..displayHeight

            if (isWithinImage) {
                MagnifyingGlassOverlay(
                    position = cursorPosition,
                    zoomLevel = magnifyingState.zoomLevel,
                    imageBitmap = imageBitmap,
                    imageDisplaySize = Size(displayWidth, displayHeight),
                    originalImageSize = Size(imageWidth, imageHeight),
                    currentColor = currentColor,
                    isCapturing = magnifyingState.isCapturing,
                    capturePulse = capturePulse,
                    capturedColor = magnifyingState.capturedColor
                )
            }
        }

        // Color preview popup (only show if not capturing and cursor is within image)
        if (cursorPosition != null && currentColor != null &&
            !magnifyingState.isCapturing &&
            cursorPosition.x in 0f..displayWidth &&
            cursorPosition.y in 0f..displayHeight) {

            ColorPreviewPopup(
                position = cursorPosition,
                colorInfo = currentColor,
                darkTextColor = darkTextColor,
                mediumTextColor = mediumTextColor
            )
        }

        // Instruction hint
        if (pickedColors.isEmpty() && !magnifyingState.isVisible) {
            InstructionHint(mediumTextColor = mediumTextColor)
        }
    }
}



@Composable
private fun ColorMarker(
    index: Int,
    color: Color,
    position: Offset,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .offset(x = position.x.dp - 16.dp, y = position.y.dp - 16.dp)
            .size(32.dp)
    ) {
        // Color circle
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(color)
                .border(width = 3.dp, color = Color.White, shape = CircleShape)
        )

        // Number badge
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-4).dp, y = (-4).dp)
                .size(16.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(width = 1.dp, color = Color.LightGray, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${index + 1}",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color.Black,
                fontSize = 8.sp
            )
        }
    }
}

@Composable
private fun MagnifyingGlassOverlay(
    position: Offset,
    zoomLevel: Float,
    imageBitmap: ImageBitmap,
    imageDisplaySize: Size,
    originalImageSize: Size,
    currentColor: CustomColorInfo?,
    isCapturing: Boolean,
    capturePulse: Float,
    capturedColor: CustomColorInfo?
) {
    val magnifierSize = 120.dp
    val magnifierRadius = magnifierSize / 2

    // Get local density to convert dp to pixels
    val density = LocalDensity.current

    // Calculate the position for magnifying glass
    // We need to ensure it stays within the screen bounds
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val containerWidth = constraints.maxWidth.toFloat()
        val containerHeight = constraints.maxHeight.toFloat()

        // Convert dp values to pixels
        val magnifierSizePx = with(density) { magnifierSize.toPx() }
        val magnifierRadiusPx = with(density) { magnifierRadius.toPx() }
        val spaceBufferPx = with(density) { 80.dp.toPx() }
        val paddingPx = with(density) { 40.dp.toPx() }

        // Calculate where to position the magnifying glass
        // Try to position it above the touch point, but adjust if needed
        val offsetX = position.x
        val offsetY = position.y

        // Check if we have enough space above the touch point
        val hasSpaceAbove = offsetY > magnifierSizePx + spaceBufferPx

        // Calculate final position
        val finalOffsetY = if (hasSpaceAbove) {
            // Position above the touch point
            offsetY - magnifierSizePx - paddingPx
        } else {
            // Position below the touch point (with some padding)
            offsetY + paddingPx
        }

        // Ensure the magnifying glass stays within horizontal bounds
        val finalOffsetX = offsetX.coerceIn(
            magnifierRadiusPx,
            containerWidth - magnifierRadiusPx
        )

        // Ensure the magnifying glass stays within vertical bounds
        val finalY = finalOffsetY.coerceIn(
            magnifierRadiusPx,
            containerHeight - magnifierRadiusPx
        )

        Box(
            modifier = Modifier
                .offset(
                    x = (finalOffsetX - magnifierRadiusPx).dp,
                    y = (finalY - magnifierRadiusPx).dp
                )
                .size(magnifierSize)
                .scale(if (isCapturing) capturePulse else 1f)
                .shadow(
                    elevation = 16.dp,
                    shape = CircleShape,
                    spotColor = Color.Black.copy(alpha = 0.3f)
                )
        ) {
            // Magnifying glass lens
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Convert dp to px for drawing
                val borderWidthPx = with(density) { 2.dp.toPx() }
                val strokeWidthPx = with(density) { 4.dp.toPx() }
                val crosshairWidthPx = with(density) { 1.dp.toPx() }

                // Draw a simple circular background with crosshair
                // For a simpler implementation, we can show a color preview

                // Draw the selected color as background
                currentColor?.let { color ->
                    val composeColor = parseColorHex(color.hex)
                    drawCircle(
                        color = composeColor,
                        radius = size.width / 2 - borderWidthPx
                    )
                } ?: run {
                    // Fallback: draw a gradient if no color
                    val gradient = Brush.radialGradient(
                        colors = listOf(Color.LightGray, Color.DarkGray),
                        center = Offset(size.width / 2, size.height / 2),
                        radius = size.width / 2
                    )
                    drawCircle(
                        brush = gradient,
                        radius = size.width / 2 - borderWidthPx
                    )
                }

                // Draw lens border
                drawCircle(
                    color = Color.White,
                    radius = size.width / 2,
                    style = Stroke(width = strokeWidthPx)
                )

                // Draw crosshair
                drawLine(
                    color = Color.White.copy(alpha = 0.8f),
                    start = Offset(size.width / 2, 0f),
                    end = Offset(size.width / 2, size.height),
                    strokeWidth = crosshairWidthPx
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.8f),
                    start = Offset(0f, size.height / 2),
                    end = Offset(size.width, size.height / 2),
                    strokeWidth = crosshairWidthPx
                )

                // Draw center dot
                drawCircle(
                    color = Color.Black,
                    radius = 3f,
                    center = Offset(size.width / 2, size.height / 2)
                )
                drawCircle(
                    color = Color.White,
                    radius = 2f,
                    center = Offset(size.width / 2, size.height / 2)
                )
            }

            // Color info display - position it relative to the magnifying glass
            val tooltipOffset = if (hasSpaceAbove) {
                // If magnifying glass is above touch point, show tooltip below magnifying glass
                30.dp
            } else {
                // If magnifying glass is below touch point, show tooltip above magnifying glass
                (-30).dp
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = tooltipOffset)
            ) {
                Surface(
                    modifier = Modifier
                        .width(140.dp)
                        .height(if (isCapturing) 70.dp else 56.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = if (isCapturing) Color(0xFF4CAF50) else Color.Black.copy(alpha = 0.8f),
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (isCapturing) {
                            // Capturing state
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Capturing",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Captured!",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            capturedColor?.let { color ->
                                Text(
                                    text = color.name,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        } else {
                            // Preview state
                            currentColor?.let { color ->
                                Text(
                                    text = color.hex,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                )
                                Text(
                                    text = color.name,
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            } ?: run {
                                Text(
                                    text = "Scanning...",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorPreviewPopup(
    position: Offset,
    colorInfo: CustomColorInfo,
    darkTextColor: Color,
    mediumTextColor: Color
) {
    val color = parseColorHex(colorInfo.hex)
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val containerWidth = constraints.maxWidth.toFloat()
        val containerHeight = constraints.maxHeight.toFloat()

        // Calculate position for popup - try to show it above the touch point
        val popupWidth = 180.dp
        val popupHeight = 56.dp

        // Convert dp values to pixels
        val popupWidthPx = with(density) { popupWidth.toPx() }
        val popupHeightPx = with(density) { popupHeight.toPx() }
        val rightPaddingPx = with(density) { 20.dp.toPx() }
        val leftPaddingPx = with(density) { 10.dp.toPx() }
        val verticalPaddingPx = with(density) { 20.dp.toPx() }

        // Calculate X position
        val offsetX = if (position.x + popupWidthPx + rightPaddingPx > containerWidth) {
            // Not enough space on the right, show on left
            position.x - popupWidthPx - leftPaddingPx
        } else {
            // Enough space on the right
            position.x + rightPaddingPx
        }.coerceIn(0f, containerWidth - popupWidthPx)

        // Calculate Y position
        val offsetY = if (position.y - popupHeightPx - verticalPaddingPx < 0) {
            // Not enough space above, show below
            position.y + verticalPaddingPx
        } else {
            // Enough space above
            position.y - popupHeightPx - verticalPaddingPx
        }.coerceIn(0f, containerHeight - popupHeightPx)

        Box(
            modifier = Modifier
                .offset(x = offsetX.dp, y = offsetY.dp)
        ) {
            Surface(
                modifier = Modifier
                    .width(popupWidth)
                    .height(popupHeight),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 8.dp,
                tonalElevation = 4.dp,
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    // Color preview
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(color)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Color info
                    Column {
                        Text(
                            text = colorInfo.hex,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            ),
                            color = darkTextColor,
                            fontSize = 14.sp
                        )

                        Text(
                            text = colorInfo.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = mediumTextColor,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InstructionHint(mediumTextColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 32.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .width(200.dp)
                .height(48.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White.copy(alpha = 0.95f),
            shadowElevation = 8.dp,
            tonalElevation = 4.dp,
            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Hint",
                    tint = Color(0xFFD4A574),
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Touch and hold to magnify",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = mediumTextColor,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun ColorPaletteSection(
    pickedColors: List<CustomColorInfo>,
    onRemoveColor: (Int) -> Unit,
    darkTextColor: Color,
    mediumTextColor: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 4.dp,
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp)
        ) {
            // Section title
            Text(
                text = "YOUR PALETTE",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp
                ),
                color = mediumTextColor,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Color swatches
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                pickedColors.forEachIndexed { index, colorInfo ->
                    val color = parseColorHex(colorInfo.hex)

                    ColorSwatch(
                        index = index,
                        color = color,
                        onRemove = { onRemoveColor(index) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorSwatch(
    index: Int,
    color: Color,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(64.dp)
    ) {
        // Color swatch
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .background(color)
        )

        // Remove button
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 8.dp, y = (-8).dp)
                .size(24.dp)
                .clip(CircleShape)
                .background(Color.Black)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                tint = Color.White,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

// Helper function to parse hex color string to Compose Color
fun parseColorHex(hex: String): Color {
    // Remove # if present
    val cleanHex = if (hex.startsWith("#")) {
        hex.substring(1)
    } else {
        hex
    }

    // Ensure we have a valid hex (6 or 8 characters)
    val fullHex = when {
        cleanHex.length == 6 -> "FF$cleanHex" // Add alpha if not present
        cleanHex.length == 8 -> cleanHex
        else -> "FFFFFFFF" // Default to white if invalid
    }

    // Convert to Long and create Color
    val colorLong = fullHex.toLong(16)
    return Color(colorLong)
}

data class NamedColor(
    val name: String,
    val r: Int,
    val g: Int,
    val b: Int
)

// Preview
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ColorPickerPreview() {
    MaterialTheme {
        ColorPicker(
            props = ColorPickerProps(
                imageUri = Uri.EMPTY,
                onColorPick = { /* Handle color pick */ },
                onBack = { /* Handle back navigation */ }
            )
        )
    }
}