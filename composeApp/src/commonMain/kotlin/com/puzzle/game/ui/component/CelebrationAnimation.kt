package com.puzzle.game.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class StarParticle(
    val x: Float,
    val y: Float,
    val speed: Float,
    val size: Float,
    val color: Color,
    val rotation: Float,
    val delay: Int
)

@Composable
fun CelebrationOverlay(
    pieceCount: Int,
    onDismiss: () -> Unit,
    onPlayAgain: () -> Unit,
    onBackToMenu: () -> Unit
) {
    val animatedAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(400),
        label = "celebration"
    )

    val stars = remember {
        val rng = Random(42)
        List(25) {
            StarParticle(
                x = rng.nextFloat(),
                y = rng.nextFloat() * 0.6f,
                speed = 0.3f + rng.nextFloat() * 0.7f,
                size = 6f + rng.nextFloat() * 14f,
                color = listOf(
                    Color(0xFFFFD54F), Color(0xFFFF8A80),
                    Color(0xFF81D4FA), Color(0xFFB2FF59),
                    Color(0xFFFF80AB), Color(0xFFFFE57F)
                ).random(rng),
                rotation = rng.nextFloat() * 360f,
                delay = rng.nextInt(300)
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "stars")
    val starAnimProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000),
            repeatMode = RepeatMode.Restart
        ),
        label = "starAnim"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            for (star in stars) {
                val progress = (starAnimProgress + star.delay / 3000f) % 1f
                val sx = star.x * w
                val sy = (star.y + progress * star.speed * 0.6f) * h
                val alpha = if (progress < 0.1f) progress * 10f
                else if (progress > 0.9f) (1f - progress) * 10f
                else 1f

                val cx = sx + star.size
                val cy = sy + star.size
                val angleRad = (star.rotation + progress * 180f) * kotlin.math.PI.toFloat() / 180f

                drawCircle(
                    color = star.color.copy(alpha = alpha * animatedAlpha),
                    radius = star.size,
                    center = Offset(cx, cy)
                )

                for (i in 0 until 4) {
                    val rayAngle = angleRad + i * 90f * kotlin.math.PI.toFloat() / 180f
                    val rx = cx + cos(rayAngle) * star.size * 0.7f
                    val ry = cy + sin(rayAngle) * star.size * 0.7f
                    drawCircle(
                        color = star.color.copy(alpha = alpha * animatedAlpha * 0.6f),
                        radius = star.size * 0.4f,
                        center = Offset(rx, ry)
                    )
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(32.dp)
        ) {
            Box(
                modifier = Modifier.size(78.dp),
                contentAlignment = Alignment.Center
            ) {
                DiamondIcon(modifier = Modifier.size(70.dp), color = MaterialTheme.colorScheme.tertiary)
                PuzzlePieceIcon(modifier = Modifier.size(34.dp), color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "太棒了！",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "你完成了${pieceCount}块拼图！",
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(28.dp))

            StoryButton(
                text = "再来一局",
                onClick = onPlayAgain,
                modifier = Modifier.fillMaxWidth(0.55f).height(50.dp),
                height = 50.dp
            )
            Spacer(modifier = Modifier.height(10.dp))
            StoryButton(
                text = "返回菜单",
                onClick = onBackToMenu,
                modifier = Modifier.fillMaxWidth(0.55f).height(50.dp),
                tone = StoryButtonTone.Secondary,
                height = 50.dp
            )
        }
    }
}
