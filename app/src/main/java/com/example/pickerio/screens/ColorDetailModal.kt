package com.example.pickerio.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.constraintlayout.helper.widget.Grid
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

data class ColorDetailModalProps(
    val color: CustomColorInfo,
    val onClose: () -> Unit
)

@Composable
fun ColorDetailModal(props: ColorDetailModalProps) {
    var copiedShade by remember { mutableStateOf<String?>(null) }
    val shades = remember(props.color.hex) { generateShades(props.color.hex) }
    val details = remember(props.color.hex) { getColorDetails(props.color.hex) }

    // Reset copied state after delay
    LaunchedEffect(copiedShade) {
        if (copiedShade != null) {
            kotlinx.coroutines.delay(1500)
            copiedShade = null
        }
    }

    Dialog(
        onDismissRequest = props.onClose,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .shadow(24.dp, RoundedCornerShape(24.dp))
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    shape = RoundedCornerShape(24.dp)
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Color Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(
                        color = hexToColor(props.color.hex),
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    )
            ) {
                // Gradient overlays
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

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.3f)
                                )
                            )
                        )
                )

                // Close button
                IconButton(
                    onClick = props.onClose,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 16.dp, end = 16.dp)
                        .size(40.dp)
                        .background(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Color name and technical name
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 20.dp, bottom = 20.dp, end = 20.dp)
                ) {
                    Text(
                        text = props.color.name,
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                            color = Color.White
                        ),
                        modifier = Modifier.shadow(4.dp)
                    )

                    Text(
                        text = details.technicalName,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.8f)
                        ),
                        modifier = Modifier.shadow(2.dp)
                    )
                }
            }

            // Scrollable content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Color Summary
                ColorSummaryCard(color = props.color, details = details)

                // Color Mixture
                ColorMixtureCard(details = details)

                // Color Shades
                ColorShadesCard(
                    shades = shades,
                    copiedShade = copiedShade,
                    onShadeCopied = { hex -> copiedShade = hex }
                )

                // Copy buttons
                CopyButtons(
                    color = props.color,
                    copiedShade = copiedShade,
                    onCopyHex = { copiedShade = props.color.hex },
                    onCopyRgb = { /* Handle RGB copy */ }
                )
            }
        }
    }
}

@Composable
private fun ColorSummaryCard(color: CustomColorInfo, details: ColorDetails) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )

                Text(
                    text = "Color Summary",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            GridLayout {
                SummaryItem(title = "Family", value = details.family)
                SummaryItem(title = "Temperature", value = details.temperature)
                SummaryItem(title = "HEX Code", value = color.hex.uppercase())
                SummaryItem(title = "RGB Values",
                    value = "${color.rgb.r}, ${color.rgb.g}, ${color.rgb.b}")
            }
        }
    }
}

@Composable
private fun GridLayout(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // First row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // First 2 items
            Box(modifier = Modifier.weight(1f)) {
                // We'll need to handle the content differently
            }
            Box(modifier = Modifier.weight(1f)) {
                // We'll need to handle the content differently
            }
        }

        // Second row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Last 2 items
            Box(modifier = Modifier.weight(1f)) {
                // We'll need to handle the content differently
            }
            Box(modifier = Modifier.weight(1f)) {
                // We'll need to handle the content differently
            }
        }
    }
}

@Composable
private fun SummaryItem(title: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = if (title.contains("HEX") || title.contains("RGB")) {
                        androidx.compose.ui.text.font.FontFamily.Monospace
                    } else {
                        MaterialTheme.typography.bodyMedium.fontFamily
                    },
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColorMixtureCard(details: ColorDetails) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Blender,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )

                Text(
                    text = "Color Mixture",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            Text(
                text = "This color can be achieved by mixing:",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            // Color chips
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                details.mixture.colors.forEachIndexed { index, mixColor ->
                    ColorChip(
                        colorName = mixColor,
                        percentage = details.mixture.percentages[index]
                    )
                }
            }

            // Mixture bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                details.mixture.colors.forEachIndexed { index, mixColor ->
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(details.mixture.percentages[index].toFloat())
                            .background(hexToColor(mixColorMap[mixColor] ?: "#000000"))
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorChip(colorName: String, percentage: Int) {
    Surface(
        modifier = Modifier.wrapContentSize(),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(hexToColor(mixColorMap[colorName] ?: "#000000"))
                    .border(
                        width = 2.dp,
                        color = Color.White,
                        shape = CircleShape
                    )
            )

            Text(
                text = "$colorName ",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            Text(
                text = "$percentage%",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
private fun ColorShadesCard(
    shades: List<ShadeInfo>,
    copiedShade: String?,
    onShadeCopied: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )

                Text(
                    text = "Color Shades",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            Text(
                text = "Tap any shade to copy its HEX code",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            // Shades grid - Manual implementation to avoid nested scroll issues
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // We have 9 items. Fixed(9) implies 9 columns?
                // The original code used GridCells.Fixed(9).
                // Let's render them in a single Row if 9 fits, or just use FlowRow logic manually since we know the count.
                // Assuming we want them in one row or wrapped?
                // Actually 9 items in one row on mobile might be tight.
                // Let's use FlowRow if we can, or just a simple Row with wrapping logic if needed.
                // Given "GridCells.Fixed(9)" it tried to put all 9 in one row? That seems small.
                // Let's assume we want them to wrap comfortably.
                // Or if the design intent was a single row of small squares:
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                   shades.forEach { shade ->
                       // Calculate width based on weight or fixed size
                       Box(modifier = Modifier.weight(1f).aspectRatio(1f).padding(2.dp)) {
                           ShadeItem(
                               shade = shade,
                               isCopied = copiedShade == shade.hex,
                               onClick = { onShadeCopied(shade.hex) }
                           )
                       }
                   }
                }
            }

            // Labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Lighter",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Text(
                    text = "Darker",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}

@Composable
private fun ShadeItem(
    shade: ShadeInfo,
    isCopied: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(hexToColor(shade.hex))
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onClick() })
            }
            .then(
                if (shade.label == "Base") {
                    Modifier.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp)
                    )
                } else {
                    Modifier
                }
            )
    ) {
        if (isCopied) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clip(RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Copied",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun CopyButtons(
    color: CustomColorInfo,
    copiedShade: String?,
    onCopyHex: () -> Unit,
    onCopyRgb: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onCopyHex,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.Transparent
            )
        ) {
            if (copiedShade == color.hex) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text("Copied!")
            } else {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text("Copy HEX")
            }
        }

        OutlinedButton(
            onClick = onCopyRgb,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.Transparent
            )
        ) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text("Copy RGB")
        }
    }
}

// Data classes
data class ShadeInfo(
    val hex: String,
    val label: String,
    val percentage: Int
)

data class ColorDetails(
    val technicalName: String,
    val family: String,
    val temperature: String,
    val mixture: ColorMixture
)

data class ColorMixture(
    val colors: List<String>,
    val percentages: List<Int>
)

// Helper functions
private fun generateShades(hex: String): List<ShadeInfo> {
    val cleanHex = hex.replace("#", "")
    val r = cleanHex.substring(0, 2).toInt(16)
    val g = cleanHex.substring(2, 4).toInt(16)
    val b = cleanHex.substring(4, 6).toInt(16)

    val shades = mutableListOf<ShadeInfo>()

    // Lighter shades
    for (i in 4 downTo 1) {
        val factor = 1 + i * 0.15f
        val newR = min(255, (r + (255 - r) * (factor - 1)).roundToInt())
        val newG = min(255, (g + (255 - g) * (factor - 1)).roundToInt())
        val newB = min(255, (b + (255 - b) * (factor - 1)).roundToInt())

        shades.add(ShadeInfo(
            hex = "#${newR.toString(16).padStart(2, '0')}${newG.toString(16).padStart(2, '0')}${newB.toString(16).padStart(2, '0')}",
            label = "+${i * 15}%",
            percentage = 100 + i * 15
        ))
    }

    // Original
    shades.add(ShadeInfo(
        hex = hex,
        label = "Base",
        percentage = 100
    ))

    // Darker shades
    for (i in 1..4) {
        val factor = 1 - i * 0.15f
        val newR = max(0, (r * factor).roundToInt())
        val newG = max(0, (g * factor).roundToInt())
        val newB = max(0, (b * factor).roundToInt())

        shades.add(ShadeInfo(
            hex = "#${newR.toString(16).padStart(2, '0')}${newG.toString(16).padStart(2, '0')}${newB.toString(16).padStart(2, '0')}",
            label = "-${i * 15}%",
            percentage = 100 - i * 15
        ))
    }

    return shades
}

private fun getColorDetails(hex: String): ColorDetails {
    val cleanHex = hex.replace("#", "")
    val r = cleanHex.substring(0, 2).toInt(16)
    val g = cleanHex.substring(2, 4).toInt(16)
    val b = cleanHex.substring(4, 6).toInt(16)

    val max = max(r, max(g, b))
    val min = min(r, min(g, b))
    val lightness = (max + min) / 2f / 255f

    var family = "Neutral"
    var temperature = "Neutral"
    var technicalName = ""

    if (max - min < 20) {
        family = "Neutral"
        temperature = "Neutral"
        technicalName = when {
            lightness > 0.9f -> "Pure White Tint"
            lightness > 0.7f -> "Light Gray"
            lightness > 0.4f -> "Medium Gray"
            lightness > 0.15f -> "Charcoal"
            else -> "Near Black"
        }
    } else if (r >= g && r >= b) {
        if (r > g + 30 && r > b + 30) {
            family = "Red"
            temperature = "Warm"
            technicalName = when {
                g > b + 20 -> if (lightness > 0.6f) "Coral Tint" else "Vermillion"
                b > g + 20 -> if (lightness > 0.6f) "Rose Pink" else "Crimson"
                else -> if (lightness > 0.6f) "Salmon" else "True Red"
            }
        } else if (g > b) {
            family = "Orange"
            temperature = "Warm"
            technicalName = if (lightness > 0.6f) "Peach" else "Burnt Orange"
        } else {
            family = "Pink"
            temperature = "Warm"
            technicalName = if (lightness > 0.6f) "Blush Pink" else "Deep Rose"
        }
    } else if (g >= r && g >= b) {
        family = "Green"
        temperature = "Cool"
        technicalName = when {
            b > r + 20 -> if (lightness > 0.6f) "Mint" else "Teal"
            r > b + 20 -> if (lightness > 0.6f) "Lime" else "Olive"
            else -> if (lightness > 0.6f) "Sage" else "Forest Green"
        }
    } else {
        family = "Blue"
        temperature = "Cool"
        technicalName = when {
            r > g + 20 -> if (lightness > 0.6f) "Lavender" else "Violet"
            g > r + 20 -> if (lightness > 0.6f) "Sky Blue" else "Cerulean"
            else -> if (lightness > 0.6f) "Periwinkle" else "Navy"
        }
    }

    val mixture = calculateMixture(r, g, b)
    return ColorDetails(technicalName, family, temperature, mixture)
}

private fun calculateMixture(r: Int, g: Int, b: Int): ColorMixture {
    val colors = mutableListOf<String>()
    val percentages = mutableListOf<Int>()

    val total = (r + g + b).toFloat()

    if (r > 50) {
        colors.add("Red")
        percentages.add(((r / total) * 100).roundToInt())
    }

    if (g > 50) {
        if (r > 100 && g > 100 && b < 150) {
            colors.add("Yellow")
            percentages.add(((g / total) * 100).roundToInt())
        } else {
            colors.add("Green")
            percentages.add(((g / total) * 100).roundToInt())
        }
    }

    if (b > 50) {
        colors.add("Blue")
        percentages.add(((b / total) * 100).roundToInt())
    }

    val lightness = (r + g + b) / 3f
    if (lightness > 200) {
        colors.add("White")
        percentages.add((((lightness - 200) / 55) * 30).roundToInt())
    } else if (lightness < 80) {
        colors.add("Black")
        percentages.add((((80 - lightness) / 80) * 40).roundToInt())
    }

    val sum = percentages.sum()
    if (sum > 0) {
        for (i in percentages.indices) {
            percentages[i] = ((percentages[i].toFloat() / sum) * 100).roundToInt()
        }
    }

    return ColorMixture(colors, percentages)
}

private val mixColorMap = mapOf(
    "Red" to "#E53935",
    "Blue" to "#1E88E5",
    "Yellow" to "#FDD835",
    "Green" to "#43A047",
    "White" to "#FAFAFA",
    "Black" to "#212121"
)

private fun hexToColor(hex: String): Color {
    val cleanHex = hex.replace("#", "")
    val colorLong = when (cleanHex.length) {
        6 -> 0xFF000000L or cleanHex.toLong(16)
        8 -> cleanHex.toLong(16)
        else -> 0xFF000000L
    }
    return Color(colorLong)
}

// Preview
@Preview(showBackground = true, widthDp = 360, heightDp = 600)
@Composable
fun ColorDetailModalPreview() {
    MaterialTheme {
        ColorDetailModal(
            props = ColorDetailModalProps(
                color = CustomColorInfo(
                    name = "Crimson Sunset",
                    hex = "#E63946",
                    rgb = RGB(230, 57, 70),
                    x = 0,
                    y = 0
                ),
                onClose = {}
            )
        )
    }
}