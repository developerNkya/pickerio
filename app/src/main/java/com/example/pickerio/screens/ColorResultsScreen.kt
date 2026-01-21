package com.example.pickerio.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import com.example.pickerio.api.NetworkModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Custom colors matching your design
val customBackgroundColor = Color(0xFFFEF7F2)
val darkTextColor = Color(0xFF3A3329)
val mediumTextColor = Color(0xFF5C5346)
val warmPrimaryColor = Color(0xFFD4A574)

data class ColorResultsProps(
    val colors: List<CustomColorInfo>,
    val onNewPhoto: () -> Unit,
    val onBack: () -> Unit
)

@Composable
fun ColorResults(props: ColorResultsProps) {
    val systemUiController = rememberSystemUiController()
    var copiedIndex by remember { mutableIntStateOf(-1) }
    var selectedColor by remember { mutableStateOf<CustomColorInfo?>(null) }
    val clipboardManager = LocalClipboardManager.current
    
    // State to hold API names for each color: Hex -> Name
    val apiNames = remember { mutableStateMapOf<String, String>() }
    
    // Fetch API names for all colors
    LaunchedEffect(props.colors) {
        props.colors.forEach { colorInfo ->
            if (!apiNames.containsKey(colorInfo.hex)) {
                launch {
                    try {
                        val hexClean = colorInfo.hex.replace("#", "")
                        val response = withContext(Dispatchers.IO) {
                            NetworkModule.api.getColor(hexClean)
                        }
                        apiNames[colorInfo.hex] = response.name.value
                    } catch (e: Exception) {
                        e.printStackTrace()
                        // Optionally set a fallback or error state, but for now we leave it loading or empty
                        // apiNames[colorInfo.hex] = "Unknown" 
                    }
                }
            }
        }
    }

    // Reset status bar colors
    LaunchedEffect(Unit) {
        systemUiController.setStatusBarColor(Color.Transparent, darkIcons = false)
        systemUiController.setNavigationBarColor(Color.Transparent, darkIcons = false)
    }

    // Handle copy timeout
    LaunchedEffect(copiedIndex) {
        if (copiedIndex != -1) {
            kotlinx.coroutines.delay(2000)
            copiedIndex = -1
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(customBackgroundColor)  // Use custom background color
    ) {
        // Animated background blobs
        ColorBlobsBackground(colors = props.colors)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 40.dp),  // Increased top padding to move everything down
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header - moved down
            HeaderSection(
                colorsCount = props.colors.size,
                onBack = props.onBack,
                darkTextColor = darkTextColor,
                mediumTextColor = mediumTextColor
            )

            // Color palette strip
            PaletteStrip(
                colors = props.colors,
                onColorSelected = { selectedColor = it }
            )

            // Color list
            ColorList(
                colors = props.colors,
                apiNames = apiNames,
                copiedIndex = copiedIndex,
                onCopy = { index, text ->
                    copiedIndex = index
                    clipboardManager.setText(AnnotatedString(text))
                },
                onColorSelected = { selectedColor = it },
                darkTextColor = darkTextColor,
                mediumTextColor = mediumTextColor
            )

            // Bottom button
            BottomButton(
                onClick = props.onNewPhoto,
                darkTextColor = darkTextColor
            )
        }

        // Color detail modal
        if (selectedColor != null) {
            ColorDetailModal(
                props = ColorDetailModalProps(
                    color = selectedColor!!,
                    onClose = { selectedColor = null }
                )
            )
        }
    }
}

@Composable
private fun ColorBlobsBackground(colors: List<CustomColorInfo>) {
    val infiniteTransition = rememberInfiniteTransition()
    val density = LocalDensity.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(0.3f)
    ) {
        colors.take(4).forEachIndexed { index, color ->
            val blobOffset by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 2 * 3.14159f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 8000 + index * 2000,
                        easing = LinearEasing
                    )
                )
            )

            val xPercent = when (index % 2) {
                0 -> 0.1f
                else -> 0.6f
            }

            val yPercent = when (index / 2) {
                0 -> 0.2f
                else -> 0.7f
            }

            Box(
                modifier = Modifier
                    .offset(
                        x = (xPercent * LocalConfiguration.current.screenWidthDp.dp +
                                (16 * cos(blobOffset)).dp),
                        y = (yPercent * LocalConfiguration.current.screenHeightDp.dp +
                                (12 * sin(blobOffset)).dp)
                    )
                    .size(256.dp)
                    .clip(CircleShape)
                    .background(
                        color = hexToColor(color.hex).copy(alpha = 0.15f)
                    )
            )
        }
    }
}

@Composable
private fun HeaderSection(
    colorsCount: Int,
    onBack: () -> Unit,
    darkTextColor: Color,
    mediumTextColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),  // Increased vertical padding
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(44.dp)  // Slightly larger
                .background(
                    color = Color(0xFFE8DED4),  // Same as your other screens
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = mediumTextColor,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Your Palette",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Serif
                ),
                color = darkTextColor
            )

            Text(
                text = "$colorsCount color${if (colorsCount != 1) "s" else ""} discovered",
                style = MaterialTheme.typography.bodySmall,
                color = mediumTextColor
            )
        }

        Icon(
            imageVector = Icons.Default.Palette,
            contentDescription = "Palette",
            tint = warmPrimaryColor,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun PaletteStrip(colors: List<CustomColorInfo>, onColorSelected: (CustomColorInfo) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .height(64.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            colors.forEach { color ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(hexToColor(color.hex))
                        .clickable { onColorSelected(color) }
                )
            }
        }
    }
}

@Composable
private fun ColorList(
    colors: List<CustomColorInfo>,
    apiNames: Map<String, String>,
    copiedIndex: Int,
    onCopy: (Int, String) -> Unit,
    onColorSelected: (CustomColorInfo) -> Unit,
    darkTextColor: Color,
    mediumTextColor: Color
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        itemsIndexed(colors) { index, color ->
            ColorListItem(
                color = color,
                apiName = apiNames[color.hex],
                index = index,
                copiedIndex = copiedIndex,
                onCopy = onCopy,
                onClick = { onColorSelected(color) },
                darkTextColor = darkTextColor,
                mediumTextColor = mediumTextColor
            )
        }
    }
}

@Composable
private fun ColorListItem(
    color: CustomColorInfo,
    apiName: String?,
    index: Int,
    copiedIndex: Int,
    onCopy: (Int, String) -> Unit,
    onClick: () -> Unit,
    darkTextColor: Color,
    mediumTextColor: Color
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    // Interactive color swatch that takes up full left side
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)  // Increased height
            .clickable(onClick = onClick)
            .hoverable(interactionSource),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isHovered) 8.dp else 4.dp
        )
    ) {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            // Full-height color swatch on left
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(100.dp)  // Fixed width for color swatch
                    .background(hexToColor(color.hex))
            ) {
                // Index badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.9f))
                        .border(
                            width = 2.dp,
                            color = hexToColor(color.hex).copy(alpha = 0.3f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = darkTextColor
                        )
                    )
                }

            }

            // Content area on right (white background)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Color name and technical name at top
                Column {
                    if (apiName != null) {
                        Text(
                            text = apiName,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif
                            ),
                            color = darkTextColor,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    } else {
                        // Loading state for list item
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = warmPrimaryColor,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Loading...",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    color = mediumTextColor.copy(alpha = 0.5f)
                                )
                            )
                        }
                    }

                    Text(
                        text = getTechnicalName(color.hex),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        ),
                        color = mediumTextColor
                    )
                }

                // Interactive elements at bottom
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Creative click prompt - using visual metaphor
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(warmPrimaryColor.copy(alpha = 0.1f))
                            .clickable { onClick() }
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Insights,
                                contentDescription = "Insights",
                                tint = warmPrimaryColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "View color insights",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = darkTextColor
                                )
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Arrow",
                            tint = warmPrimaryColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Color value in a visually appealing way
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Color hex in a stylish pill
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .clickable { onCopy(index * 2, color.hex) }
                                .border(
                                    width = 1.dp,
                                    color = hexToColor(color.hex).copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(18.dp)
                                ),
                            color = hexToColor(color.hex).copy(alpha = 0.1f)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(hexToColor(color.hex))
                                        .border(
                                            width = 1.dp,
                                            color = Color.White,
                                            shape = CircleShape
                                        )
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = color.hex.uppercase(),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        color = darkTextColor
                                    )
                                )

                                if (copiedIndex == index * 2) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Copied",
                                        tint = warmPrimaryColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        // Copy icon as a standalone button
                        IconButton(
                            onClick = { onCopy(index * 2 + 1, "rgb(${color.rgb.r}, ${color.rgb.g}, ${color.rgb.b})") },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(mediumTextColor.copy(alpha = 0.1f))
                        ) {
                            if (copiedIndex == index * 2 + 1) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Copied",
                                    tint = warmPrimaryColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy RGB",
                                    tint = mediumTextColor,
                                    modifier = Modifier.size(16.dp)
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
private fun BottomButton(
    onClick: () -> Unit,
    darkTextColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        customBackgroundColor
                    )
                )
            )
            .padding(top = 48.dp, bottom = 32.dp, start = 24.dp, end = 24.dp)
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = darkTextColor,
                contentColor = customBackgroundColor
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 8.dp
            )
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Camera",
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Discover New Colors",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}

// Helper functions (same as before)
private fun getTechnicalName(hex: String): String {
    val cleanHex = hex.replace("#", "")
    if (cleanHex.length < 6) return "Unknown"

    val r = cleanHex.substring(0, 2).toIntOrNull(16) ?: 0
    val g = cleanHex.substring(2, 4).toIntOrNull(16) ?: 0
    val b = cleanHex.substring(4, 6).toIntOrNull(16) ?: 0
    val max = max(r, max(g, b))
    val min = min(r, min(g, b))
    val lightness = (max + min) / 2f / 255f

    if (max - min < 20) {
        return when {
            lightness > 0.9f -> "White Tint"
            lightness > 0.7f -> "Light Gray"
            lightness > 0.4f -> "Medium Gray"
            else -> "Dark Tone"
        }
    }

    return when {
        r >= g && r >= b -> when {
            g > b + 20 -> "Warm Vermillion"
            b > g + 20 -> "Cool Crimson"
            else -> "True Red Base"
        }
        g >= r && g >= b -> when {
            b > r + 20 -> "Cyan-Green"
            r > b + 20 -> "Yellow-Green"
            else -> "True Green Base"
        }
        else -> when {
            r > g + 20 -> "Violet-Blue"
            g > r + 20 -> "Cyan-Blue"
            else -> "True Blue Base"
        }
    }
}

private fun hexToColor(hex: String): Color {
    val cleanHex = hex.replace("#", "")
    return when (cleanHex.length) {
        6 -> {
            val colorLong = cleanHex.toLongOrNull(16) ?: 0xFF000000L
            Color(colorLong or 0xFF000000L)
        }
        8 -> {
            val colorLong = cleanHex.toLongOrNull(16) ?: 0xFF000000L
            Color(colorLong)
        }
        else -> Color.Black
    }
}

// Preview
@Preview(showBackground = true, showSystemUi = true, widthDp = 360)
@Composable
fun ColorResultsPreviewSmall() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            ColorResults(
                props = ColorResultsProps(
                    colors = listOf(
                        CustomColorInfo(
                            name = "Crimson Sunset",
                            hex = "#E63946",
                            rgb = RGB(230, 57, 70),
                            x = 0,
                            y = 0
                        ),
                        CustomColorInfo(
                            name = "Golden Hour",
                            hex = "#F4A261",
                            rgb = RGB(244, 162, 97),
                            x = 0,
                            y = 0
                        ),
                        CustomColorInfo(
                            name = "Ocean Depth",
                            hex = "#2A9D8F",
                            rgb = RGB(42, 157, 143),
                            x = 0,
                            y = 0
                        )
                    ),
                    onNewPhoto = {},
                    onBack = {}
                )
            )
        }
    }
}