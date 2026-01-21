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
import androidx.compose.ui.graphics.drawscope.Stroke
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

    // Handle proceed
    fun handleProceed() {
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
        // Header (simplified - just back button and title)
        SimpleHeader(
            onBack = props.onBack,
            darkTextColor = darkTextColor,
            mediumTextColor = mediumTextColor
        )

        // Main content area
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 60.dp)
        ) {
            // Canvas area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
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

            // Bottom section with palette and proceed button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Palette section
                if (pickedColors.isNotEmpty()) {
                    ColorPaletteSection(
                        pickedColors = pickedColors,
                        onRemoveColor = ::removeColor,
                        darkTextColor = darkTextColor,
                        mediumTextColor = mediumTextColor
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Proceed button (floating at bottom)
                    FloatingProceedButton(
                        pickedColorsCount = pickedColors.size,
                        onProceed = ::handleProceed,
                        darkTextColor = darkTextColor,
                        mediumTextColor = mediumTextColor
                    )
                } else {
                    // Show instructions when no colors picked
                    InstructionHintBottom(mediumTextColor = mediumTextColor)
                }
            }
        }
    }
}

@Composable
private fun SimpleHeader(
    onBack: () -> Unit,
    darkTextColor: Color,
    mediumTextColor: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
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

            // Title
            Text(
                text = "Color Studio",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Serif
                ),
                color = darkTextColor
            )
        }
    }
}

@Composable
private fun FloatingProceedButton(
    pickedColorsCount: Int,
    onProceed: () -> Unit,
    darkTextColor: Color,
    mediumTextColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = onProceed,
            enabled = pickedColorsCount > 0,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = darkTextColor,
                contentColor = Color.White,
                disabledContainerColor = mediumTextColor.copy(alpha = 0.3f),
                disabledContentColor = Color.White.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 8.dp,
                pressedElevation = 4.dp
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Proceed",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Show count badge
                Badge(
                    containerColor = Color.White,
                    contentColor = darkTextColor
                ) {
                    Text(
                        text = "$pickedColorsCount",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        fontSize = 12.sp
                    )
                }
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


        // Instruction hint (centered in the canvas)
        if (pickedColors.isEmpty() && !magnifyingState.isVisible) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                InstructionHint(mediumTextColor = mediumTextColor)
            }
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
    capturedColor: CustomColorInfo?,
    containerSize: Size
) {
    val magnifierSize = 100.dp
    val magnifierRadius = magnifierSize / 2

    Box(
        modifier = Modifier
            .offset(x = position.x.dp - magnifierRadius, y = position.y.dp - magnifierRadius)
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
            // Draw the selected color as background
            currentColor?.let { color ->
                val composeColor = parseColorHex(color.hex)
                drawCircle(
                    color = composeColor,
                    radius = size.width / 2
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
                    radius = size.width / 2
                )
            }

            // Draw lens border
            drawCircle(
                color = Color.White,
                radius = size.width / 2,
                style = Stroke(width = 4f)
            )

            // Draw crosshair
            drawLine(
                color = Color.White.copy(alpha = 0.8f),
                start = Offset(size.width / 2, 0f),
                end = Offset(size.width / 2, size.height),
                strokeWidth = 1f
            )
            drawLine(
                color = Color.White.copy(alpha = 0.8f),
                start = Offset(0f, size.height / 2),
                end = Offset(size.width, size.height / 2),
                strokeWidth = 1f
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

    }
}

@Composable
private fun InstructionHint(mediumTextColor: Color) {
    Surface(
        modifier = Modifier
            .width(240.dp)
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
                text = "Touch anywhere to pick colors",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = mediumTextColor,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun InstructionHintBottom(mediumTextColor: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Select colors from the image",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            ),
            color = mediumTextColor,
            fontSize = 14.sp
        )
        Text(
            text = "Your palette will appear here",
            style = MaterialTheme.typography.bodySmall,
            color = mediumTextColor.copy(alpha = 0.8f),
            fontSize = 12.sp
        )
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
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp)
        ) {
            // Section title with count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "YOUR PALETTE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp
                    ),
                    color = mediumTextColor
                )

                Text(
                    text = "${pickedColors.size} colors",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = darkTextColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

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

        // Small number indicator
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 8.dp, y = (-8).dp)
                .size(20.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.9f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${index + 1}",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color.Black,
                fontSize = 10.sp
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
                onColorPick = {/* Handle color pick */ },
                onBack = { /* Handle back navigation */ }
            )
        )
    }
}