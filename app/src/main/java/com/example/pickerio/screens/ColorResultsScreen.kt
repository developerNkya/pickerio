package com.example.pickerio.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

data class ColorInfo(
    val name: String,
    val hex: String,
    val rgb: RGB,
    val percentage: Float
)

//data class RGB(
//    val r: Int,
//    val g: Int,
//    val b: Int
//)

data class ColorResultsProps(
    val colors: List<ColorInfo>,
    val onNewPhoto: () -> Unit,
    val onBack: () -> Unit,
    val onColorSelected: (ColorInfo) -> Unit = {}
)

@Composable
fun ColorResults(props: ColorResultsProps) {
    val systemUiController = rememberSystemUiController()
    var copiedIndex by remember { mutableIntStateOf(-1) }

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
                onColorSelected = props.onColorSelected
            )

            // Color list
            ColorList(
                colors = props.colors,
                copiedIndex = copiedIndex,
                onCopy = { index, text ->
                    copiedIndex = index
                    // TODO: Implement clipboard copy
                },
                onColorSelected = props.onColorSelected
            )

            // Bottom button
            BottomButton(
                onClick = props.onNewPhoto
            )
        }
    }
}

@Composable
private fun ColorBlobsBackground(colors: List<ColorInfo>) {
    val infiniteTransition = rememberInfiniteTransition()

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
                        x = xPercent * LocalConfiguration.current.screenWidthDp.dp +
                                (16 * cos(blobOffset)).dp,
                        y = yPercent * LocalConfiguration.current.screenHeightDp.dp +
                                (12 * sin(blobOffset)).dp
                    )
                    .size(256.dp)
                    .clip(CircleShape)
                    .background(
                        color = Color(hexToColor(color.hex)).copy(alpha = 0.15f)
                    )
                    .blur(48.dp)
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
                tint = MaterialTheme.colorScheme.onSecondaryContainer
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
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Serif
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
private fun PaletteStrip(colors: List<ColorInfo>, onColorSelected: (ColorInfo) -> Unit) {
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
                        .background(Color(hexToColor(color.hex)))
                        .clickable { onColorSelected(color) }
                )
            }
        }
    }
}


@Composable
private fun ColorList(
    colors: List<ColorInfo>,
    copiedIndex: Int,
    onCopy: (Int, String) -> Unit,
    onColorSelected: (ColorInfo) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)  // Only horizontal padding
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 16.dp),  // Vertical padding here
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(colors.size) { index ->
                ColorListItem(
                    color = colors[index],
                    index = index,
                    copiedIndex = copiedIndex,
                    onCopy = onCopy,
                    onClick = { onColorSelected(colors[index]) }
                )
            }
        }
    }
}

@Composable
private fun ColorListItem(
    color: ColorInfo,
    index: Int,
    copiedIndex: Int,
    onCopy: (Int, String) -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Color preview
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .fillMaxHeight()
                    .background(Color(hexToColor(color.hex)))
            ) {
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
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                        .size(32.dp),
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.2f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Serif
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
                    iconColor = Color(hexToColor(color.hex)),
                    copyIndex = index * 2,
                    copiedIndex = copiedIndex,
                    onCopy = onCopy
                )

                // RGB copy button
                CopyButton(
                    text = "rgb(${color.rgb.r}, ${color.rgb.g}, ${color.rgb.b})",
                    iconColor = Color(hexToColor("#FF4444")), // Red indicator
                    copyIndex = index * 2 + 1,
                    copiedIndex = copiedIndex,
                    onCopy = onCopy,
                    isRgb = true
                )

                // View details hint
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(0f), // Hidden by default, can be shown on hover
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
            .hoverable(interactionSource = interactionSource),
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

// Helper function to convert hex string to Android Color int
private fun hexToColor(hex: String): Long {
    val cleanHex = hex.replace("#", "")
    return when (cleanHex.length) {
        6 -> 0xFF000000L or cleanHex.toLong(16)
        8 -> cleanHex.toLong(16)
        else -> 0xFF000000L
    }
}

// Technical name function (ported from React)
private fun getTechnicalName(hex: String): String {
    val cleanHex = hex.replace("#", "")
    val r = cleanHex.substring(0, 2).toInt(16)
    val g = cleanHex.substring(2, 4).toInt(16)
    val b = cleanHex.substring(4, 6).toInt(16)
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

// Preview
@Preview(showBackground = true, showSystemUi = true, widthDp = 360)
@Composable
fun ColorResultsPreviewSmall() {
    val sampleColors = listOf(
        ColorInfo("Crimson Sunset", "#E63946", RGB(230, 57, 70), 0.25f),
        ColorInfo("Golden Hour", "#F4A261", RGB(244, 162, 97), 0.20f),
        ColorInfo("Ocean Depth", "#2A9D8F", RGB(42, 157, 143), 0.18f),
        ColorInfo("Midnight Blue", "#264653", RGB(38, 70, 83), 0.22f),
        ColorInfo("Sunflower", "#E9C46A", RGB(233, 196, 106), 0.15f)
    )

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            ColorResults(
                props = ColorResultsProps(
                    colors = sampleColors,
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
    val sampleColors = listOf(
        ColorInfo("Crimson Sunset", "#E63946", RGB(230, 57, 70), 0.25f),
        ColorInfo("Golden Hour", "#F4A261", RGB(244, 162, 97), 0.20f)
    )

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            ColorResults(
                props = ColorResultsProps(
                    colors = sampleColors,
                    onNewPhoto = {},
                    onBack = {}
                )
            )
        }
    }
}