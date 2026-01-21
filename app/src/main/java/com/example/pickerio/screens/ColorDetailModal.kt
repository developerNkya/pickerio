package com.example.pickerio.screens

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.pickerio.api.NetworkModule
import com.example.pickerio.api.ColorApiResponse
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.constraintlayout.helper.widget.Grid
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
// Add this import at the top
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally


data class ColorDetailModalProps(
    val color: CustomColorInfo,
    val onClose: () -> Unit
)

@Composable
fun ColorDetailModal(props: ColorDetailModalProps) {
    // State for the color currently being displayed/inspected in the modal.
    // Initialized with the color passed in props.
    var displayedColor by remember { mutableStateOf(props.color) }
    
    val clipboardManager = LocalClipboardManager.current
    
    // API Data state
    var apiResponse by remember { mutableStateOf<ColorApiResponse?>(null) }
    var isApiLoading by remember { mutableStateOf(false) }

    var copiedShade by remember { mutableStateOf<String?>(null) }
    
    // Re-calculate these whenever displayedColor changes
    val shades = remember(displayedColor.hex) { generateShades(displayedColor.hex) }
    val details = remember(displayedColor.hex) { getColorDetails(displayedColor.hex) }

    // Fetch API data when displayedColor changes
    LaunchedEffect(displayedColor.hex) {
        isApiLoading = true
        apiResponse = null
        try {
            // Remove '#' for API call
            val hexClean = displayedColor.hex.replace("#", "")
            val response = withContext(Dispatchers.IO) {
                NetworkModule.api.getColor(hexClean)
            }
            apiResponse = response
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isApiLoading = false
        }
    }

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
                        color = hexToColor(displayedColor.hex),
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
                    if (apiResponse != null) {
                        Text(
                            text = apiResponse!!.name.value,
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                                color = Color.White
                            ),
                            modifier = Modifier.shadow(4.dp)
                        )
                    } else {
                         // Loading state for name
                         Row(verticalAlignment = Alignment.CenterVertically) {
                             CircularProgressIndicator(
                                 modifier = Modifier.size(24.dp),
                                 color = Color.White,
                                 strokeWidth = 2.dp
                             )
                             Spacer(modifier = Modifier.width(8.dp))
                             Text(
                                text = "Identifying...",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                             )
                         }
                    }

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
                ColorSummaryCard(color = displayedColor, details = details, apiResponse = apiResponse)

                // Color Mixture
                ColorMixtureCard(details = details)

                // Color Shades
                ColorShadesCard(
                    shades = shades,
                    copiedShade = copiedShade,
                    onShadeCopied = { hex -> 
                        // Update displayed color when a shade is clicked
                        // We create a new CustomColorInfo for the shade
                        val r = Integer.valueOf(hex.substring(1, 3), 16)
                        val g = Integer.valueOf(hex.substring(3, 5), 16)
                        val b = Integer.valueOf(hex.substring(5, 7), 16)
                        
                        // Get technical name for immediate feedback so we don't show "Shade"
                        val techName = getColorDetails(hex).technicalName
                        
                        displayedColor = CustomColorInfo(
                            hex = hex,
                            rgb = RGB(r, g, b),
                            name = techName, 
                            x = 0, y = 0
                        )
                        // Reset API response so loading state shows for new shade
                        apiResponse = null
                        // Also set copied shade just for feedback
                        copiedShade = hex 
                    }
                )

                // Copy buttons
                CopyButtons(
                    color = displayedColor,
                    copiedShade = copiedShade,
                    onCopyHex = { 
                        clipboardManager.setText(AnnotatedString(displayedColor.hex))
                        copiedShade = displayedColor.hex 
                    },
                    onCopyRgb = { 
                         val rgbString = "rgb(${displayedColor.rgb.r}, ${displayedColor.rgb.g}, ${displayedColor.rgb.b})"
                         clipboardManager.setText(AnnotatedString(rgbString))
                         // We reuse copiedShade state for visual feedback, though ideally we'd have a separate one
                         // For now, let's just trigger the feedback on the hex as a proxy or just rely on system toast if any?
                         // Better: let's update CopyButtons to show 'Copied!' for RGB too if we want.
                         // For simplicity, let's just copy.
                    }
                )
            }
        }
    }
}

@Composable
private fun ColorSummaryCard(color: CustomColorInfo, details: ColorDetails, apiResponse: ColorApiResponse?) {
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
            
            // API Description 
            if (apiResponse != null) {
                 Text(
                    text = "Description: ${apiResponse.name.value} is a ${details.temperature.lowercase()} ${details.family} visual.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.padding(top = 8.dp)
                )
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
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    val canScrollLeft by remember(scrollState.value) {
        derivedStateOf { scrollState.value > 0 }
    }
    val canScrollRight by remember(scrollState.value, scrollState.maxValue) {
        derivedStateOf { scrollState.value < scrollState.maxValue }
    }

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
                text = "Tap any shade to view its details",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // Left arrow with AnimatedVisibility
                androidx.compose.animation.AnimatedVisibility(
                    visible = canScrollLeft,
                    enter = fadeIn() + slideInHorizontally { -20 },
                    exit = fadeOut() + slideOutHorizontally { -20 },
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .zIndex(1f)
                ) {
                    FloatingArrowButton(
                        direction = ArrowDirection.LEFT,
                        onClick = {
                            coroutineScope.launch {
                                val scrollAmount = 250
                                val target = scrollState.value - scrollAmount
                                scrollState.animateScrollTo(
                                    max(0, target),
                                    animationSpec = tween(300)
                                )
                            }
                        }
                    )
                }

                // Right arrow with AnimatedVisibility
                androidx.compose.animation.AnimatedVisibility(
                    visible = canScrollRight,
                    enter = fadeIn() + slideInHorizontally { 20 },
                    exit = fadeOut() + slideOutHorizontally { 20 },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .zIndex(1f)
                ) {
                    FloatingArrowButton(
                        direction = ArrowDirection.RIGHT,
                        onClick = {
                            coroutineScope.launch {
                                val scrollAmount = 250
                                val target = scrollState.value + scrollAmount
                                scrollState.animateScrollTo(
                                    min(scrollState.maxValue, target),
                                    animationSpec = tween(300)
                                )
                            }
                        }
                    )
                }

                // Horizontal scrollable shades
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Spacer(modifier = Modifier.width(4.dp))

                    shades.forEach { shade ->
                        SmartShadeItem(
                            shade = shade,
                            isCopied = copiedShade == shade.hex,
                            isBase = shade.label == "Base",
                            onClick = { onShadeCopied(shade.hex) }
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))
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
private fun SmartShadeItem(
    shade: ShadeInfo,
    isCopied: Boolean,
    isBase: Boolean,
    onClick: () -> Unit
) {
    val selectionAnimation by animateFloatAsState(
        targetValue = if (isCopied) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "selectionGlow"
    )

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "scaleAnimation"
    )

    val density = LocalDensity.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(60.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(hexToColor(shade.hex))
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) { onClick() }
                .drawWithContent {
                    drawContent()

                    if (isBase) {
                        val borderWidth = with(density) { 2.dp.toPx() }
                        val cornerRadius = with(density) { 12.dp.toPx() }

                        drawRoundRect(
                            color = Color.White,
                            style = Stroke(width = borderWidth),
                            cornerRadius = CornerRadius(cornerRadius)
                        )

                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.3f),
                            style = Stroke(width = borderWidth * 2),
                            cornerRadius = CornerRadius(cornerRadius)
                        )
                    }

                    if (selectionAnimation > 0) {
                        val glowColor = Color.White.copy(alpha = 0.5f * selectionAnimation)
                        val glowWidth = with(density) { 4.dp.toPx() } * selectionAnimation
                        val cornerRadius = with(density) { 12.dp.toPx() }

                        drawRoundRect(
                            color = glowColor,
                            style = Stroke(width = glowWidth),
                            cornerRadius = CornerRadius(cornerRadius)
                        )

                        drawRoundRect(
                            color = Color.Black.copy(alpha = 0.2f * selectionAnimation),
                            style = Stroke(width = with(density) { 2.dp.toPx() }),
                            cornerRadius = CornerRadius(cornerRadius)
                        )
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (isCopied) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                        .clip(RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Copied",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (shade.label != "Base") {
                Text(
                    text = shade.label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (shade.percentage > 70) Color.Black else Color.White
                    ),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 4.dp)
                        .background(
                            color = if (shade.percentage > 70)
                                Color.White.copy(alpha = 0.7f)
                            else
                                Color.Black.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }

        Text(
            text = if (shade.label == "Base") "Base" else shade.percentage.toString(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (shade.label == "Base") FontWeight.Bold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

enum class ArrowDirection {
    LEFT, RIGHT
}

@Composable
private fun FloatingArrowButton(
    direction: ArrowDirection,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()

    // Pulse animation for the arrow
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAnimation"
    )

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )
                )
            )
            .clickable(onClick = onClick)
            .shadow(
                elevation = 8.dp,
                shape = CircleShape,
                clip = true
            )
            .drawBehind {
                // Inner shine
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.3f * pulseAlpha),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.3f, size.height * 0.3f)
                    ),
                    radius = size.minDimension / 2,
                    center = center,
                    blendMode = BlendMode.Overlay
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (direction == ArrowDirection.LEFT)
                Icons.Default.ArrowBack
            else
                Icons.Default.ArrowForward,
            contentDescription = if (direction == ArrowDirection.LEFT)
                "Scroll left"
            else
                "Scroll right",
            tint = Color.White,
            modifier = Modifier.size(16.dp)
        )
    }
}

// Also update your Preview function to test the new scroll behavior:
@Preview(showBackground = true, widthDp = 360, heightDp = 600)
@Composable
fun ColorDetailModalPreview() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Test with many shades to see the scroll
            val testShades = generateShades("#E63946")
            var copiedShade by remember { mutableStateOf<String?>(null) }

            ColorShadesCard(
                shades = testShades + testShades, // Double the shades for testing scroll
                copiedShade = copiedShade,
                onShadeCopied = { hex -> copiedShade = hex }
            )
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

