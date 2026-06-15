package com.puzzle.game.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@Composable
fun CoralButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(PuzzleDimens.PrimaryButtonHeight),
        shape = RoundedCornerShape(PuzzleDimens.ControlRadius),
        colors = ButtonDefaults.buttonColors(
            containerColor = PuzzleColors.Coral,
            contentColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
        contentPadding = PaddingValues(horizontal = 18.dp)
    ) {
        Text(text, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun CloudButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(PuzzleDimens.SecondaryButtonHeight),
        shape = RoundedCornerShape(PuzzleDimens.ControlRadius),
        border = BorderStroke(1.dp, PuzzleColors.Stone.copy(alpha = 0.72f)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = PuzzleColors.Cloud,
            contentColor = PuzzleColors.StoneDark
        ),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
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
