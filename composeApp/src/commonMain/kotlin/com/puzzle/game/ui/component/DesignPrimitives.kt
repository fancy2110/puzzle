package com.puzzle.game.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.puzzle.game.ui.theme.FragmaDimens
import com.puzzle.game.ui.theme.PuzzleColors
import com.puzzle.game.ui.theme.PuzzleDimens

@Composable
fun PuzzleBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFFFFCF6),
                        PuzzleColors.Mist,
                        Color(0xFFEFF5F4)
                    )
                )
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            content()
        }
    }
}

@Composable
fun StoneSurface(
    modifier: Modifier = Modifier,
    radius: androidx.compose.ui.unit.Dp = PuzzleDimens.CardRadius,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .shadow(12.dp, RoundedCornerShape(radius), ambientColor = Color.Black.copy(alpha = 0.08f), spotColor = Color.Black.copy(alpha = 0.10f))
            .border(1.dp, PuzzleColors.Stone.copy(alpha = 0.62f), RoundedCornerShape(radius)),
        shape = RoundedCornerShape(radius),
        color = PuzzleColors.Cloud
    ) {
        content()
    }
}

enum class StoryButtonTone {
    Primary,
    Secondary,
    Danger
}

@Composable
fun StoryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tone: StoryButtonTone = StoryButtonTone.Primary,
    height: Dp = if (tone == StoryButtonTone.Primary) FragmaDimens.PrimaryActionHeight else FragmaDimens.SecondaryActionHeight
) {
    val shape = RoundedCornerShape(PuzzleDimens.ControlRadius)
    when (tone) {
        StoryButtonTone.Primary, StoryButtonTone.Danger -> {
            val container = if (tone == StoryButtonTone.Danger) PuzzleColors.CoralDark else PuzzleColors.Coral
            Button(
                onClick = onClick,
                modifier = modifier.height(height),
                shape = shape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = container,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 7.dp, pressedElevation = 3.dp),
                contentPadding = PaddingValues(horizontal = 18.dp)
            ) {
                Text(
                    text,
                    fontSize = if (height >= FragmaDimens.PrimaryActionHeight) 20.sp else 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }

        StoryButtonTone.Secondary -> {
            OutlinedButton(
                onClick = onClick,
                modifier = modifier.height(height),
                shape = shape,
                border = BorderStroke(1.dp, PuzzleColors.Stone.copy(alpha = 0.72f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = PuzzleColors.Cloud,
                    contentColor = PuzzleColors.StoneDark
                ),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Text(
                    text,
                    fontSize = if (height >= FragmaDimens.SecondaryActionHeight) 16.sp else 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun CoralButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    StoryButton(text = text, onClick = onClick, modifier = modifier, tone = StoryButtonTone.Primary)
}

@Composable
fun CloudButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    StoryButton(text = text, onClick = onClick, modifier = modifier, tone = StoryButtonTone.Secondary)
}

@Composable
fun FragmaIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = FragmaDimens.TopControlHeight,
    icon: @Composable () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .size(size)
            .shadow(8.dp, RoundedCornerShape(size * 0.30f))
            .border(1.dp, PuzzleColors.Stone.copy(alpha = 0.74f), RoundedCornerShape(size * 0.30f)),
        shape = RoundedCornerShape(size * 0.30f),
        color = PuzzleColors.Cloud.copy(alpha = 0.96f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            IconButtonOrnament(modifier = Modifier.fillMaxSize())
            icon()
        }
    }
}

@Composable
fun Plaque(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .shadow(8.dp, RoundedCornerShape(PuzzleDimens.ControlRadius))
            .border(1.dp, PuzzleColors.Stone.copy(alpha = 0.68f), RoundedCornerShape(PuzzleDimens.ControlRadius)),
        shape = RoundedCornerShape(PuzzleDimens.ControlRadius),
        color = PuzzleColors.Cloud.copy(alpha = 0.94f)
    ) {
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            content()
        }
    }
}

@Composable
fun StoryDialogSurface(
    icon: @Composable () -> Unit,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actions: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .shadow(18.dp, RoundedCornerShape(30.dp), ambientColor = Color.Black.copy(alpha = 0.12f), spotColor = Color.Black.copy(alpha = 0.18f))
            .border(1.dp, PuzzleColors.Stone.copy(alpha = 0.62f), RoundedCornerShape(30.dp)),
        shape = RoundedCornerShape(30.dp),
        color = PuzzleColors.Cloud.copy(alpha = 0.98f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier.size(74.dp),
                contentAlignment = Alignment.Center
            ) {
                DialogIconFrame(modifier = Modifier.matchParentSize())
                icon()
            }
            Text(
                text = title,
                fontSize = 24.sp,
                lineHeight = 30.sp,
                fontWeight = FontWeight.Bold,
                color = PuzzleColors.StoneDark,
                textAlign = TextAlign.Center
            )
            Text(
                text = message,
                fontSize = 15.sp,
                lineHeight = 21.sp,
                fontWeight = FontWeight.SemiBold,
                color = PuzzleColors.Muted,
                textAlign = TextAlign.Center
            )
            Box(modifier = Modifier.padding(top = 10.dp)) {
                actions()
            }
        }
    }
}

@Composable
private fun IconButtonOrnament(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.padding(9.dp)) {
        val corner = 8.dp.toPx()
        val stroke = 1.dp.toPx()
        drawLine(PuzzleColors.Gold.copy(alpha = 0.28f), Offset(corner, 0f), Offset(0f, corner), strokeWidth = stroke)
        drawLine(PuzzleColors.Gold.copy(alpha = 0.28f), Offset(size.width - corner, size.height), Offset(size.width, size.height - corner), strokeWidth = stroke)
    }
}

@Composable
private fun DialogIconFrame(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val path = Path().apply {
            moveTo(size.width / 2f, 0f)
            cubicTo(size.width * 0.78f, size.height * 0.04f, size.width * 0.96f, size.height * 0.22f, size.width, size.height / 2f)
            cubicTo(size.width * 0.96f, size.height * 0.78f, size.width * 0.78f, size.height * 0.96f, size.width / 2f, size.height)
            cubicTo(size.width * 0.22f, size.height * 0.96f, size.width * 0.04f, size.height * 0.78f, 0f, size.height / 2f)
            cubicTo(size.width * 0.04f, size.height * 0.22f, size.width * 0.22f, size.height * 0.04f, size.width / 2f, 0f)
            close()
        }
        drawPath(path, PuzzleColors.Coral.copy(alpha = 0.18f))
        drawPath(path, PuzzleColors.Gold.copy(alpha = 0.58f), style = Stroke(width = 1.5.dp.toPx()))
    }
}
