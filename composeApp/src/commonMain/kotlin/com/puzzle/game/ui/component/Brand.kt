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
import com.puzzle.game.i18n.LocalAppStrings

@Composable
fun BrandMark(
    modifier: Modifier = Modifier,
    size: Dp = 56.dp
) {
    val shape = RoundedCornerShape(size * 0.28f)
    Surface(
        modifier = modifier
            .size(size)
            .shadow(8.dp, shape)
            .border(1.dp, PuzzleColors.Stone.copy(alpha = 0.72f), shape),
        shape = shape,
        color = PuzzleColors.Cloud
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(size * 0.12f)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = this.size.width
                val h = this.size.height
                val left = w * 0.08f
                val top = h * 0.08f
                val card = Size(w * 0.84f, h * 0.84f)
                drawRoundRect(
                    color = PuzzleColors.Mist,
                    topLeft = Offset(left, top),
                    size = card,
                    cornerRadius = CornerRadius(w * 0.12f)
                )
                val tealPiece = Path().apply {
                    moveTo(left, top)
                    lineTo(left + card.width * 0.60f, top)
                    lineTo(left + card.width * 0.48f, top + card.height * 0.48f)
                    lineTo(left, top + card.height * 0.38f)
                    close()
                }
                val coralPiece = Path().apply {
                    moveTo(left + card.width * 0.54f, top)
                    lineTo(left + card.width, top)
                    lineTo(left + card.width, top + card.height * 0.68f)
                    lineTo(left + card.width * 0.48f, top + card.height * 0.50f)
                    close()
                }
                val goldPiece = Path().apply {
                    moveTo(left, top + card.height * 0.36f)
                    lineTo(left + card.width * 0.48f, top + card.height * 0.50f)
                    lineTo(left + card.width, top + card.height * 0.68f)
                    lineTo(left + card.width, top + card.height)
                    lineTo(left, top + card.height)
                    close()
                }
                drawPath(tealPiece, PuzzleColors.Teal)
                drawPath(coralPiece, PuzzleColors.Coral)
                drawPath(goldPiece, PuzzleColors.Gold)
                drawRoundRect(
                    color = PuzzleColors.StoneDark.copy(alpha = 0.74f),
                    topLeft = Offset(left + card.width * 0.40f, top + card.height * 0.52f),
                    size = Size(card.width * 0.20f, card.height * 0.34f),
                    cornerRadius = CornerRadius(w * 0.035f)
                )
                drawCircle(
                    color = PuzzleColors.Coral,
                    radius = w * 0.07f,
                    center = Offset(left + card.width * 0.20f, top + card.height * 0.82f)
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
    val strings = LocalAppStrings.current
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
                strings.appName,
                fontSize = if (compact) 28.sp else 34.sp,
                fontWeight = FontWeight.Bold,
                color = PuzzleColors.StoneDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (showSubtitle) {
                Text(
                    strings.appSubtitle,
                    fontSize = 13.sp,
                    color = PuzzleColors.Muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
