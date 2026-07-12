package com.masoudnaji.easysmartpdf.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Vertical Technical Network Background for Poonel.
 * Optimized for an "uneven," organic technical look:
 * - Reduced density to avoid clutter.
 * - Randomized line lengths and distribution for an intentional, non-repeating feel.
 * - Clusters with "frontier" lines that extend unevenly into the center.
 */
@Composable
fun PoonelBackground(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF1C1B17),
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(color = backgroundColor)

            val width = size.width
            val height = size.height
            val honeyColor = Color(0xFFE9A600)
            
            val unitSize = 40.dp.toPx()
            val hDist = unitSize * 0.866f 
            val vDist = unitSize * 1.5f

            // 1. Top cluster - Uneven and localized
            drawUnevenTechnicalStructure(
                origin = Offset(width * 0.85f, height * -0.05f),
                rows = 15,
                cols = 6,
                unitSize = unitSize,
                hDist = hDist,
                vDist = vDist,
                color = honeyColor,
                isDownward = true,
                seed = 42
            )

            // 2. Bottom cluster - Uneven and localized
            drawUnevenTechnicalStructure(
                origin = Offset(width * 0.15f, height * 1.05f),
                rows = 12,
                cols = 5,
                unitSize = unitSize * 0.85f,
                hDist = hDist * 0.85f,
                vDist = vDist * 0.85f,
                color = honeyColor,
                isDownward = false,
                seed = 123
            )
        }
        
        content()
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawUnevenTechnicalStructure(
    origin: Offset,
    rows: Int,
    cols: Int,
    unitSize: Float,
    hDist: Float,
    vDist: Float,
    color: Color,
    isDownward: Boolean,
    seed: Int
) {
    val random = Random(seed)
    
    for (c in 0 until cols) {
        for (r in 0 until rows) {
            // Randomly skip some elements to make it "uneven"
            if (random.nextFloat() > 0.6f) continue

            val xDir = if (origin.x > size.width / 2) -1 else 1
            val xOffset = if (r % 2 == 1) hDist else 0f
            val px = origin.x + (c * hDist * 2 * xDir) + xOffset
            val py = if (isDownward) origin.y + (r * vDist * 0.5f) else origin.y - (r * vDist * 0.5f)

            // Depth fade
            val progress = r.toFloat() / rows
            val baseAlpha = (0.12f * (1f - progress)).coerceIn(0f, 0.12f)
            
            if (baseAlpha > 0.01f) {
                val center = Offset(px, py)
                
                // Draw the geometric element with some randomness in style
                drawIsometricElement(
                    center = center,
                    size = unitSize,
                    color = color.copy(alpha = baseAlpha),
                    nodeAlpha = (baseAlpha * 3f).coerceAtMost(0.4f),
                    styleIndex = random.nextInt(4)
                )
                
                // UNEVEN CIRCUIT LINES
                // Only some nodes shoot out lines, and their lengths are randomized
                val isAtFrontier = r > rows * 0.5f
                val lineProbability = if (isAtFrontier) 0.25f else 0.1f
                
                if (random.nextFloat() < lineProbability) {
                    // Randomized line length
                    val minLen = unitSize * 0.5f
                    val maxLen = if (isAtFrontier) unitSize * 3.5f else unitSize * 1.5f
                    val lineLength = minLen + random.nextFloat() * (maxLen - minLen)
                    
                    val endY = if (isDownward) py + lineLength else py - lineLength
                    
                    drawLine(
                        color = color.copy(alpha = baseAlpha * 0.6f),
                        start = center,
                        end = Offset(px, endY),
                        strokeWidth = 1.dp.toPx()
                    )
                    
                    // Termination node
                    drawCircle(
                        color = color.copy(alpha = baseAlpha * 2.5f),
                        radius = 2.dp.toPx(),
                        center = Offset(px, endY)
                    )
                    
                    // Rare horizontal branch for the "red area" look from the image
                    if (isAtFrontier && random.nextFloat() < 0.3f) {
                        val branchLen = hDist * (0.8f + random.nextFloat())
                        val branchX = px + (branchLen * xDir)
                        val branchY = py + (if (isDownward) lineLength * 0.7f else -lineLength * 0.7f)
                        
                        drawLine(
                            color = color.copy(alpha = baseAlpha * 0.4f),
                            start = Offset(px, branchY),
                            end = Offset(branchX, branchY),
                            strokeWidth = 0.8.dp.toPx()
                        )
                        drawCircle(
                            color = color.copy(alpha = baseAlpha * 2f),
                            radius = 1.5.dp.toPx(),
                            center = Offset(branchX, branchY)
                        )
                    }
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawIsometricElement(
    center: Offset,
    size: Float,
    color: Color,
    nodeAlpha: Float,
    styleIndex: Int
) {
    val points = (0..5).map { i ->
        val angle = (PI / 2) + (i * PI / 3)
        Offset(
            center.x + size * cos(angle).toFloat(),
            center.y + size * sin(angle).toFloat()
        )
    }

    when (styleIndex) {
        0 -> { // Cube (Y-lines)
            val path = Path().apply {
                points.forEachIndexed { i, pt -> if (i == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y) }
                close()
            }
            drawPath(path = path, color = color, style = Stroke(width = 1.dp.toPx()))
            drawLine(color = color, start = center, end = points[0], strokeWidth = 1.dp.toPx())
            drawLine(color = color, start = center, end = points[2], strokeWidth = 1.dp.toPx())
            drawLine(color = color, start = center, end = points[4], strokeWidth = 1.dp.toPx())
        }
        1 -> { // Complex Technical Path
            drawLine(color = color, start = points[0], end = points[1], strokeWidth = 1.dp.toPx())
            drawLine(color = color, start = points[1], end = points[2], strokeWidth = 1.dp.toPx())
            drawLine(color = color, start = points[2], end = center, strokeWidth = 1.dp.toPx())
            drawLine(color = color, start = center, end = points[4], strokeWidth = 1.dp.toPx())
        }
        2 -> { // Double chevron / tech arrows
            drawLine(color = color, start = points[1], end = points[2], strokeWidth = 1.dp.toPx())
            drawLine(color = color, start = points[2], end = points[3], strokeWidth = 1.dp.toPx())
            val innerSize = size * 0.6f
            val innerPoints = (0..5).map { i ->
                val angle = (PI / 2) + (i * PI / 3)
                Offset(center.x + innerSize * cos(angle).toFloat(), center.y + innerSize * sin(angle).toFloat())
            }
            drawLine(color = color.copy(alpha = color.alpha * 0.6f), start = innerPoints[1], end = innerPoints[2], strokeWidth = 0.8.dp.toPx())
        }
        else -> { // Interconnected segment
            drawLine(color = color, start = points[0], end = points[5], strokeWidth = 1.dp.toPx())
            drawLine(color = color, start = points[5], end = points[4], strokeWidth = 1.dp.toPx())
        }
    }

    // Nodes (Dots) - only on some vertices for a cleaner look
    points.forEachIndexed { i, pt ->
        if ((styleIndex + i) % 3 == 0) {
            drawCircle(
                color = color.copy(alpha = nodeAlpha),
                radius = 2.dp.toPx(),
                center = pt
            )
        }
    }
}

@Preview
@Composable
fun PoonelBackgroundPreview() {
    PoonelBackground {}
}
