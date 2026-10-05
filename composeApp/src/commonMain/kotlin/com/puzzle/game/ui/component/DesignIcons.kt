package com.puzzle.game.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.puzzle.game.ui.theme.PuzzleColors

@Composable
fun SettingsDesignButton(
    onClick: () -> Unit,
    size: Dp,
    modifier: Modifier = Modifier
) {
    FragmaIconButton(
        onClick = onClick,
        size = size,
        modifier = modifier
    ) {
        SettingsIcon(modifier = Modifier.size(size * 0.52f))
    }
}

@Composable
fun BackIcon(
    modifier: Modifier = Modifier,
    color: Color = PuzzleColors.StoneDark
) {
    Canvas(modifier = modifier) {
        val stroke = size.minDimension * 0.14f
        val start = Offset(size.width * 0.72f, size.height * 0.18f)
        val mid = Offset(size.width * 0.30f, size.height * 0.50f)
        val end = Offset(size.width * 0.72f, size.height * 0.82f)
        drawLine(color, start, mid, strokeWidth = stroke)
        drawLine(color, mid, end, strokeWidth = stroke)
    }
}

@Composable
fun HomeIcon(
    modifier: Modifier = Modifier,
    color: Color = PuzzleColors.StoneDark
) {
    Canvas(modifier = modifier) {
        val stroke = size.minDimension * 0.08f
        val roof = Path().apply {
            moveTo(size.width * 0.16f, size.height * 0.48f)
            lineTo(size.width * 0.50f, size.height * 0.18f)
            lineTo(size.width * 0.84f, size.height * 0.48f)
        }
        val body = Path().apply {
            moveTo(size.width * 0.26f, size.height * 0.44f)
            lineTo(size.width * 0.26f, size.height * 0.84f)
            lineTo(size.width * 0.74f, size.height * 0.84f)
            lineTo(size.width * 0.74f, size.height * 0.44f)
        }
        drawPath(roof, color, style = Stroke(width = stroke))
        drawPath(body, color, style = Stroke(width = stroke))
        drawRoundRect(
            color = color.copy(alpha = 0.22f),
            topLeft = Offset(size.width * 0.43f, size.height * 0.60f),
            size = Size(size.width * 0.14f, size.height * 0.24f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(stroke, stroke)
        )
    }
}

@Composable
fun SettingsIcon(
    modifier: Modifier = Modifier,
    color: Color = PuzzleColors.TealDark
) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val outer = size.minDimension * 0.46f
        val inner = size.minDimension * 0.20f
        repeat(8) { index ->
            val angle = index * kotlin.math.PI.toFloat() / 4f
            val start = Offset(
                center.x + kotlin.math.cos(angle) * outer * 0.70f,
                center.y + kotlin.math.sin(angle) * outer * 0.70f
            )
            val end = Offset(
                center.x + kotlin.math.cos(angle) * outer,
                center.y + kotlin.math.sin(angle) * outer
            )
            drawLine(
                color = color,
                start = start,
                end = end,
                strokeWidth = size.minDimension * 0.16f
            )
        }
        drawCircle(color, radius = outer * 0.72f, center = center)
        drawCircle(PuzzleColors.Cloud, radius = inner, center = center)
    }
}

@Composable
fun BookIcon(
    modifier: Modifier = Modifier,
    color: Color = PuzzleColors.StoneDark
) {
    Canvas(modifier = modifier) {
        val stroke = 1.5.dp.toPx()
        val left = Path().apply {
            moveTo(size.width * 0.10f, size.height * 0.18f)
            quadraticTo(size.width * 0.32f, size.height * 0.10f, size.width * 0.48f, size.height * 0.26f)
            lineTo(size.width * 0.48f, size.height * 0.86f)
            quadraticTo(size.width * 0.30f, size.height * 0.72f, size.width * 0.10f, size.height * 0.80f)
            close()
        }
        val right = Path().apply {
            moveTo(size.width * 0.90f, size.height * 0.18f)
            quadraticTo(size.width * 0.68f, size.height * 0.10f, size.width * 0.52f, size.height * 0.26f)
            lineTo(size.width * 0.52f, size.height * 0.86f)
            quadraticTo(size.width * 0.70f, size.height * 0.72f, size.width * 0.90f, size.height * 0.80f)
            close()
        }
        drawPath(left, color.copy(alpha = 0.24f))
        drawPath(right, color.copy(alpha = 0.24f))
        drawPath(left, color, style = Stroke(width = stroke))
        drawPath(right, color, style = Stroke(width = stroke))
        drawLine(color.copy(alpha = 0.72f), Offset(size.width * 0.50f, size.height * 0.24f), Offset(size.width * 0.50f, size.height * 0.86f), strokeWidth = stroke)
    }
}

@Composable
fun ClockIcon(
    modifier: Modifier = Modifier,
    color: Color = PuzzleColors.StoneDark
) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        drawCircle(
            color = color.copy(alpha = 0.10f),
            radius = size.minDimension * 0.43f,
            center = center
        )
        drawCircle(
            color = color,
            radius = size.minDimension * 0.43f,
            center = center,
            style = Stroke(width = 2.dp.toPx())
        )
        drawLine(color, center, Offset(center.x, center.y - size.height * 0.22f), strokeWidth = 2.dp.toPx())
        drawLine(color, center, Offset(center.x + size.width * 0.18f, center.y), strokeWidth = 2.dp.toPx())
    }
}

@Composable
fun ImageIcon(
    modifier: Modifier = Modifier,
    color: Color = PuzzleColors.TealDark
) {
    Canvas(modifier = modifier) {
        val stroke = size.minDimension * 0.07f
        drawRoundRect(
            color = color.copy(alpha = 0.10f),
            topLeft = Offset(size.width * 0.12f, size.height * 0.18f),
            size = Size(size.width * 0.76f, size.height * 0.64f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(stroke * 1.4f, stroke * 1.4f)
        )
        drawRoundRect(
            color = color,
            topLeft = Offset(size.width * 0.12f, size.height * 0.18f),
            size = Size(size.width * 0.76f, size.height * 0.64f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(stroke * 1.4f, stroke * 1.4f),
            style = Stroke(width = stroke)
        )
        drawCircle(color.copy(alpha = 0.70f), radius = size.minDimension * 0.07f, center = Offset(size.width * 0.68f, size.height * 0.34f))
        val hill = Path().apply {
            moveTo(size.width * 0.18f, size.height * 0.76f)
            lineTo(size.width * 0.40f, size.height * 0.52f)
            lineTo(size.width * 0.54f, size.height * 0.66f)
            lineTo(size.width * 0.66f, size.height * 0.54f)
            lineTo(size.width * 0.84f, size.height * 0.76f)
            close()
        }
        drawPath(hill, color.copy(alpha = 0.55f))
    }
}

@Composable
fun ThemeIcon(
    modifier: Modifier = Modifier,
    color: Color = PuzzleColors.Gold
) {
    Canvas(modifier = modifier) {
        val stroke = size.minDimension * 0.07f
        val diamond = Path().apply {
            moveTo(size.width * 0.50f, size.height * 0.08f)
            lineTo(size.width * 0.84f, size.height * 0.42f)
            lineTo(size.width * 0.50f, size.height * 0.92f)
            lineTo(size.width * 0.16f, size.height * 0.42f)
            close()
        }
        drawPath(diamond, color.copy(alpha = 0.24f))
        drawPath(diamond, PuzzleColors.StoneDark, style = Stroke(width = stroke))
        drawCircle(PuzzleColors.Coral, radius = size.minDimension * 0.09f, center = Offset(size.width * 0.50f, size.height * 0.42f))
    }
}

@Composable
fun PuzzlePieceIcon(
    modifier: Modifier = Modifier,
    color: Color = PuzzleColors.TealDark
) {
    Canvas(modifier = modifier) {
        val path = Path().apply {
            moveTo(size.width * 0.12f, size.height * 0.16f)
            lineTo(size.width * 0.42f, size.height * 0.16f)
            cubicTo(size.width * 0.40f, 0f, size.width * 0.66f, 0f, size.width * 0.64f, size.height * 0.16f)
            lineTo(size.width * 0.88f, size.height * 0.16f)
            lineTo(size.width * 0.88f, size.height * 0.44f)
            cubicTo(size.width, size.height * 0.42f, size.width, size.height * 0.68f, size.width * 0.88f, size.height * 0.66f)
            lineTo(size.width * 0.88f, size.height * 0.88f)
            lineTo(size.width * 0.60f, size.height * 0.88f)
            cubicTo(size.width * 0.62f, size.height, size.width * 0.36f, size.height, size.width * 0.38f, size.height * 0.88f)
            lineTo(size.width * 0.12f, size.height * 0.88f)
            lineTo(size.width * 0.12f, size.height * 0.62f)
            cubicTo(0f, size.height * 0.64f, 0f, size.height * 0.38f, size.width * 0.12f, size.height * 0.40f)
            close()
        }
        drawPath(path, color)
    }
}

@Composable
fun PauseIcon(
    modifier: Modifier = Modifier,
    color: Color = PuzzleColors.StoneDark
) {
    Canvas(modifier = modifier) {
        val barWidth = size.width * 0.22f
        drawRoundRect(
            color = color,
            topLeft = Offset(size.width * 0.20f, size.height * 0.14f),
            size = androidx.compose.ui.geometry.Size(barWidth, size.height * 0.72f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth * 0.25f)
        )
        drawRoundRect(
            color = color,
            topLeft = Offset(size.width * 0.58f, size.height * 0.14f),
            size = androidx.compose.ui.geometry.Size(barWidth, size.height * 0.72f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth * 0.25f)
        )
    }
}

@Composable
fun CheckIcon(
    modifier: Modifier = Modifier,
    color: Color = PuzzleColors.TealDark
) {
    Canvas(modifier = modifier) {
        val stroke = size.minDimension * 0.14f
        drawLine(color, Offset(size.width * 0.16f, size.height * 0.54f), Offset(size.width * 0.42f, size.height * 0.78f), strokeWidth = stroke)
        drawLine(color, Offset(size.width * 0.42f, size.height * 0.78f), Offset(size.width * 0.86f, size.height * 0.24f), strokeWidth = stroke)
    }
}

@Composable
fun DiamondIcon(
    modifier: Modifier = Modifier,
    color: Color = PuzzleColors.Gold
) {
    Canvas(modifier = modifier) {
        val path = Path().apply {
            moveTo(size.width / 2f, 0f)
            lineTo(size.width, size.height / 2f)
            lineTo(size.width / 2f, size.height)
            lineTo(0f, size.height / 2f)
            close()
        }
        drawPath(path, color)
        drawPath(path, PuzzleColors.Cloud.copy(alpha = 0.65f), style = Stroke(width = 1.dp.toPx()))
    }
}
