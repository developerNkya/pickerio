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



// If ColorInfo is defined elsewhere, import it:
// import com.example.pickerio.models.ColorInfo
// If not, define it here temporarily:

// Define ColorInfo if not already imported
data class ColorInfo(
    val name: String,
    val hex: String,
    val rgb: RGB,
    val percentage: Float
)

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
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Animated background blobs
        ColorBlobsBackground(colors = props.colors)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            HeaderSection(
                colorsCount = props.colors.size,
                onBack = props.onBack
            )

            // Color palette strip
            PaletteStrip(
                colors = props.colors,
                onColorSelected = { selectedColor = it }
            )

            // Color list
            ColorList(
                colors = props.colors,
                copiedIndex = copiedIndex,
                onCopy = { index, text ->
                    copiedIndex = index
                    clipboardManager.setText(AnnotatedString(text))
                },
                onColorSelected = { selectedColor = it }
            )

            // Bottom button
            BottomButton(
                onClick = props.onNewPhoto
            )
        }

        // Color detail modal - Convert CustomColorInfo to ColorInfo
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

// Conversion function: CustomColorInfo → ColorInfo
private fun CustomColorInfo.toColorInfo(percentage: Float = 0f): ColorInfo {
    return ColorInfo(
        name = this.name,
        hex = this.hex,
        rgb = this.rgb,
        percentage = percentage
    )
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
                        x = with(density) {
                            (xPercent * LocalConfiguration.current.screenWidthDp.dp.toPx() +
                                    (16 * cos(blobOffset)).dp.toPx()).toDp()
                        },
                        y = with(density) {
                            (yPercent * LocalConfiguration.current.screenHeightDp.dp.toPx() +
                                    (12 * sin(blobOffset)).dp.toPx()).toDp()
                        }
                    )
                    .size(256.dp)
                    .clip(CircleShape)
                    .background(
                        color = hexToColor(color.hex).copy(alpha = 0.15f)
                    )
                    .blur(radius = 48.dp, edgeTreatment = BlurredEdgeTreatment(RoundedCornerShape(0.dp)))
            )
        }
    }
}

@Composable
private fun HeaderSection(colorsCount: Int, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(20.dp)
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
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "$colorsCount color${if (colorsCount != 1) "s" else ""} discovered",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            imageVector = Icons.Default.Palette,
            contentDescription = "Palette",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
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
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
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
    copiedIndex: Int,
    onCopy: (Int, String) -> Unit,
    onColorSelected: (CustomColorInfo) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(colors) { index, color ->
            ColorListItem(
                color = color,
                index = index,
                copiedIndex = copiedIndex,
                onCopy = onCopy,
                onClick = { onColorSelected(color) }
            )
        }
    }
}

@Composable
private fun ColorListItem(
    color: CustomColorInfo,
    index: Int,
    copiedIndex: Int,
    onCopy: (Int, String) -> Unit,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .hoverable(interactionSource),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(
                alpha = if (isHovered) 0.3f else 0.1f
            )
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isHovered) 4.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Color preview
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .fillMaxHeight()
                    .background(hexToColor(color.hex))
            ) {
                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.2f),
                                    Color.Transparent
                                ),
                                start = Offset(0f, 0f),
                                end = Offset(1f, 1f)
                            )
                        )
                )

                // Index badge
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                        .size(32.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f),
                    tonalElevation = 2.dp
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Color name and technical name
                Column {
                    Text(
                        text = color.name,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = getTechnicalName(color.hex),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // HEX code copy button
                CopyButton(
                    text = color.hex,
                    iconColor = hexToColor(color.hex),
                    copyIndex = index * 2,
                    copiedIndex = copiedIndex,
                    onCopy = onCopy
                )

                // RGB copy button
                CopyButton(
                    text = "rgb(${color.rgb.r}, ${color.rgb.g}, ${color.rgb.b})",
                    iconColor = MaterialTheme.colorScheme.error,
                    copyIndex = index * 2 + 1,
                    copiedIndex = copiedIndex,
                    onCopy = onCopy,
                    isRgb = true
                )

                // View details hint
                if (isHovered) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "View shades & mixture",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CopyButton(
    text: String,
    iconColor: Color,
    copyIndex: Int,
    copiedIndex: Int,
    onCopy: (Int, String) -> Unit,
    isRgb: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onCopy(copyIndex, text) }
            )
            .hoverable(interactionSource),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHovered) {
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
            } else {
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isHovered) 1.dp else 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isRgb) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp, 12.dp)
                                .clip(CircleShape)
                                .background(Color.Red.copy(alpha = 0.8f))
                        )
                        Box(
                            modifier = Modifier
                                .size(6.dp, 12.dp)
                                .clip(CircleShape)
                                .background(Color.Green.copy(alpha = 0.8f))
                        )
                        Box(
                            modifier = Modifier
                                .size(6.dp, 12.dp)
                                .clip(CircleShape)
                                .background(Color.Blue.copy(alpha = 0.8f))
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(iconColor)
                            .border(
                                width = 1.dp,
                                color = Color.Black.copy(alpha = 0.1f),
                                shape = CircleShape
                            )
                    )
                }

                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (copiedIndex == copyIndex) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Copied",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(16.dp)
                        .alpha(if (isHovered) 1f else 0f)
                )
            }
        }
    }
}

@Composable
private fun BottomButton(onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onBackground,
                contentColor = MaterialTheme.colorScheme.background
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 8.dp
            )
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Camera",
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Discover New Colors",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

// Helper functions
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

@Preview(showBackground = true, showSystemUi = true, widthDp = 411)
@Composable
fun ColorResultsPreviewMedium() {
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
                        )
                    ),
                    onNewPhoto = {},
                    onBack = {}
                )
            )
        }
    }
}