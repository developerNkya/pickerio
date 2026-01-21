package com.example.pickerio.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun ColorResultsScreen(
    colors: List<CustomColorInfo>,
    onBack: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var selectedColor by remember { mutableStateOf<CustomColorInfo?>(null) }

    // Colors
    val customBackgroundColor = Color(0xFFFEF7F2)
    val darkTextColor = Color(0xFF3A3329)
    val mediumTextColor = Color(0xFF5C5346)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(customBackgroundColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Text(
                    text = "Your Palette",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Serif
                    ),
                    color = darkTextColor
                )
            }

            // Grid of colors
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 140.dp),
                contentPadding = PaddingValues(24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(colors) { colorInfo ->
                    ColorCard(
                        colorInfo = colorInfo,
                        onClick = { selectedColor = colorInfo },
                        darkTextColor = darkTextColor,
                        mediumTextColor = mediumTextColor
                    )
                }
            }
        }

        // Color Detail Modal
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
private fun ColorCard(
    colorInfo: CustomColorInfo,
    onClick: () -> Unit,
    darkTextColor: Color,
    mediumTextColor: Color
) {
    val color = parseColorHex(colorInfo.hex)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.8f)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(color)
            )
            
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = colorInfo.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = darkTextColor,
                    maxLines = 1
                )
                Text(
                    text = colorInfo.hex,
                    style = MaterialTheme.typography.bodySmall,
                    color = mediumTextColor
                )
            }
        }
    }
}

// Removed ColorDetailDialog and ColorValueItem as they are replaced by ColorDetailModal
