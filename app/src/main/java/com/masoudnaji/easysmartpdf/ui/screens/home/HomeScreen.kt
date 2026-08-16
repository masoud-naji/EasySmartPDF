package com.masoudnaji.easysmartpdf.ui.screens.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMerge
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.masoudnaji.easysmartpdf.R
import com.masoudnaji.easysmartpdf.ui.theme.PoonelTheme
import com.masoudnaji.easysmartpdf.ui.theme.Spacing
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.app.Activity
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onCreatePicturesClick: () -> Unit,
    onMergePdfClick: () -> Unit,
    onSplitPdfClick: () -> Unit,
    onImageToPdfClick: () -> Unit,
    onPdfEditClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // On Home screen, background is always dark, so status bar icons should always be light
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = Color.Transparent,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Spacer(Modifier.statusBarsPadding())
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .padding(horizontal = Spacing.md),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_logo_icon),
                                contentDescription = null,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = stringResource(R.string.home_title).uppercase(),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    letterSpacing = 2.sp,
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = Color(0xFFE6E1D9) // High-contrast light neutral
                            )
                        }
                        IconButton(
                            onClick = onSettingsClick,
                            modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = stringResource(R.string.settings_title),
                                tint = Color(0xFFE6E1D9), // High-contrast light neutral
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        },
        modifier = modifier,
        containerColor = Color.Transparent
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            val screenWidth = maxWidth
            
            // Responsive Radius Calculation for 2-3-2 Hive
            // The cluster is ~5.46 * radius wide. We add side padding (32dp total).
            val horizontalPadding = 32.dp
            val availableWidth = screenWidth - horizontalPadding
            val maxRadius = availableWidth / 5.46f
            val radius = maxRadius.coerceIn(45.dp, 64.dp)
            
            val hSpacing = radius * sqrt(3f) 
            val vSpacing = radius * 1.5f

            val centerX = screenWidth / 2
            val clusterCenterY = vSpacing * 2.2f // Visual vertical anchor

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                // Large Metro-style Typography
                Text(
                    text = "PDF TOOLS",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    ),
                    color = Color(0xFFE6E1D9).copy(alpha = 0.5f), // High-contrast light gray
                    modifier = Modifier.padding(start = Spacing.lg, top = Spacing.md, bottom = Spacing.lg)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(vSpacing * 4.5f)
                ) {
                    // Row 1 (2 Cells)
                    HiveToolNode(
                        label = "PDF → IMG",
                        icon = Icons.Default.Image,
                        radius = radius,
                        onClick = onCreatePicturesClick,
                        modifier = Modifier.offset(
                            x = centerX - (hSpacing / 2) - radius,
                            y = clusterCenterY - vSpacing
                        )
                    )
                    HiveToolNode(
                        label = "MERGE",
                        icon = Icons.AutoMirrored.Filled.CallMerge,
                        radius = radius,
                        onClick = onMergePdfClick,
                        modifier = Modifier.offset(
                            x = centerX + (hSpacing / 2) - radius,
                            y = clusterCenterY - vSpacing
                        )
                    )

                    // Row 2 (3 Cells)
                    HiveToolNode(
                        label = "SPLIT",
                        icon = Icons.AutoMirrored.Filled.CallSplit,
                        radius = radius,
                        onClick = onSplitPdfClick,
                        modifier = Modifier.offset(
                            x = centerX - hSpacing - radius,
                            y = clusterCenterY
                        )
                    )
                    HiveToolNode(
                        label = "IMG → PDF",
                        icon = Icons.Default.PictureAsPdf,
                        radius = radius,
                        onClick = onImageToPdfClick,
                        modifier = Modifier.offset(
                            x = centerX - radius,
                            y = clusterCenterY
                        )
                    )
                    HiveToolNode(
                        label = "EDIT",
                        icon = Icons.Default.AutoFixHigh,
                        radius = radius,
                        onClick = onPdfEditClick,
                        modifier = Modifier.offset(
                            x = centerX + hSpacing - radius,
                            y = clusterCenterY
                        )
                    )

                    // Row 3 (2 Cells - Placeholders)
                    HiveToolNode(
                        label = "COMPRESS",
                        icon = Icons.Default.Compress,
                        radius = radius,
                        onClick = {}, // Placeholder
                        enabled = false,
                        modifier = Modifier.offset(
                            x = centerX - (hSpacing / 2) - radius,
                            y = clusterCenterY + vSpacing
                        )
                    )
                    HiveToolNode(
                        label = "PROTECT",
                        icon = Icons.Default.Lock,
                        radius = radius,
                        onClick = {}, // Placeholder
                        enabled = false,
                        modifier = Modifier.offset(
                            x = centerX + (hSpacing / 2) - radius,
                            y = clusterCenterY + vSpacing
                        )
                    )
                }
                
                // Intentional Negative Space
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun HiveToolNode(
    label: String,
    icon: ImageVector,
    radius: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val hexShape = HexagonShape()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scope = rememberCoroutineScope()
    
    val goldColor = Color(0xFFE9A600)
    val darkCharcoal = Color(0xFF1C1B17)
    val isDark = isSystemInDarkTheme()
    
    // Animation states
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = tween(durationMillis = 100),
        label = "scale"
    )
    
    val backgroundColor by animateColorAsState(
        targetValue = if (isPressed) goldColor else {
            if (isDark) darkCharcoal.copy(alpha = 0.9f) else MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = tween(durationMillis = 150),
        label = "bgColor"
    )
    
    val contentColor by animateColorAsState(
        targetValue = if (isPressed) darkCharcoal else {
            if (isDark) goldColor else MaterialTheme.colorScheme.onSurface
        },
        animationSpec = tween(durationMillis = 150),
        label = "contentColor"
    )

    val borderColor = if (isPressed) Color.Transparent else goldColor.copy(alpha = 0.4f)

    Box(
        modifier = modifier
            .size(radius * 2)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(hexShape)
            .background(if (enabled) backgroundColor else backgroundColor.copy(alpha = 0.3f))
            .then(
                if (enabled) Modifier.border(1.2.dp, borderColor, hexShape)
                else Modifier.border(1.dp, borderColor.copy(alpha = 0.1f), hexShape)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = {
                    scope.launch {
                        // Allow time for the user to see the gold press state
                        delay(120) 
                        onClick()
                    }
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(Spacing.xs)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = contentColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                ),
                textAlign = TextAlign.Center,
                color = contentColor,
                lineHeight = 12.sp,
                maxLines = 1
            )
        }
    }
}

class HexagonShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        return Outline.Generic(
            path = Path().apply {
                val radius = size.width / 2f
                val centerX = size.width / 2f
                val centerY = size.height / 2f
                
                for (i in 0..5) {
                    val correctedAngle = (PI / 3) * i - (PI / 2)
                    val x = centerX + radius * cos(correctedAngle).toFloat()
                    val y = centerY + radius * sin(correctedAngle).toFloat()
                    if (i == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    PoonelTheme {
        HomeScreen(
            onCreatePicturesClick = {},
            onMergePdfClick = {},
            onSplitPdfClick = {},
            onImageToPdfClick = {},
            onPdfEditClick = {},
            onSettingsClick = {}
        )
    }
}
