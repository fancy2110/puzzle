package com.puzzle.game.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.puzzle.game.ui.theme.PuzzleColors

@Composable
fun BrandMark(
    modifier: Modifier = Modifier,
    size: Dp = 56.dp
) {
    val shape = RoundedCornerShape(size * 0.28f)

    Surface(
        modifier = modifier
            .size(size)
            .shadow(8.dp, shape, ambientColor = Color.Black.copy(alpha = 0.08f), spotColor = Color.Black.copy(alpha = 0.14f))
            .border(1.dp, PuzzleColors.Stone.copy(alpha = 0.72f), shape),
        shape = shape,
        color = PuzzleColors.Cloud
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(size * 0.12f)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = this.size.width
                val h = this.size.height
                val cardTop = h * 0.18f
                val cardLeft = w * 0.14f
                val cardSize = Size(w * 0.72f, h * 0.66f)

                drawRoundRect(
                    color = Color.White.copy(alpha = 0.82f),
                    topLeft = Offset(cardLeft, cardTop),
                    size = cardSize,
                    cornerRadius = CornerRadius(w * 0.11f, w * 0.11f)
                )

                val skyPiece = Path().apply {
                    moveTo(cardLeft, cardTop)
                    lineTo(cardLeft + cardSize.width * 0.56f, cardTop)
                    lineTo(cardLeft + cardSize.width * 0.48f, cardTop + cardSize.height * 0.46f)
                    lineTo(cardLeft + cardSize.width * 0.12f, cardTop + cardSize.height * 0.38f)
                    close()
                }
                drawPath(skyPiece, PuzzleColors.Teal.copy(alpha = 0.88f))

                val storyPiece = Path().apply {
                    moveTo(cardLeft + cardSize.width * 0.42f, cardTop + cardSize.height * 0.03f)
                    lineTo(cardLeft + cardSize.width, cardTop)
                    lineTo(cardLeft + cardSize.width, cardTop + cardSize.height * 0.72f)
                    lineTo(cardLeft + cardSize.width * 0.52f, cardTop + cardSize.height * 0.54f)
                    close()
                }
                drawPath(storyPiece, PuzzleColors.Coral.copy(alpha = 0.92f))

                val groundPiece = Path().apply {
                    moveTo(cardLeft, cardTop + cardSize.height * 0.42f)
                    lineTo(cardLeft + cardSize.width * 0.54f, cardTop + cardSize.height * 0.52f)
                    lineTo(cardLeft + cardSize.width, cardTop + cardSize.height * 0.74f)
                    lineTo(cardLeft + cardSize.width, cardTop + cardSize.height)
                    lineTo(cardLeft, cardTop + cardSize.height)
                    close()
                }
                drawPath(groundPiece, PuzzleColors.Gold.copy(alpha = 0.88f))

                drawRoundRect(
                    color = PuzzleColors.StoneDark.copy(alpha = 0.72f),
                    topLeft = Offset(cardLeft + cardSize.width * 0.40f, cardTop + cardSize.height * 0.52f),
                    size = Size(cardSize.width * 0.20f, cardSize.height * 0.32f),
                    cornerRadius = CornerRadius(w * 0.035f, w * 0.035f)
                )
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.86f),
                    topLeft = Offset(cardLeft + cardSize.width * 0.20f, cardTop + cardSize.height * 0.48f),
                    size = Size(cardSize.width * 0.14f, cardSize.height * 0.16f),
                    cornerRadius = CornerRadius(w * 0.025f, w * 0.025f)
                )
                drawCircle(
                    color = PuzzleColors.Coral,
                    radius = w * 0.07f,
                    center = Offset(cardLeft + cardSize.width * 0.22f, cardTop + cardSize.height * 0.82f)
                )
            }
        }
    }
}

@Composable
fun BrandLockup(
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    centered: Boolean = false,
    showSubtitle: Boolean = true
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BrandMark(size = if (compact) 46.dp else 56.dp)
        Column(
            horizontalAlignment = if (centered) Alignment.CenterHorizontally else Alignment.Start
        ) {
            Text(
                "故事拼图",
                fontSize = if (compact) 28.sp else 34.sp,
                fontWeight = FontWeight.Bold,
                color = PuzzleColors.StoneDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (showSubtitle) {
                Text(
                    "拼完整画面，读懂每一页故事",
                    fontSize = 13.sp,
                    color = PuzzleColors.Muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
