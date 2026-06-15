package com.puzzle.game.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.puzzle.game.ui.component.BrandMark
import com.puzzle.game.ui.component.PuzzleBackground
import com.puzzle.game.ui.theme.PuzzleColors
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(600),
        label = "splash_fade"
    )
    val markScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.92f,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = Spring.StiffnessLow),
        label = "splash_mark_scale"
    )

    LaunchedEffect(Unit) {
        visible = true
        delay(1300)
        onFinished()
    }

    PuzzleBackground {
        Box(
            modifier = Modifier.fillMaxSize().padding(28.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.alpha(alpha)
            ) {
                BrandMark(
                    size = 104.dp,
                    modifier = Modifier.scale(markScale)
                )
                Spacer(modifier = Modifier.height(22.dp))
                Text(
                    text = "故事拼图",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = PuzzleColors.StoneDark
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "拼完整画面，读懂每一页故事",
                    fontSize = 14.sp,
                    color = PuzzleColors.Muted
                )
                Spacer(modifier = Modifier.height(28.dp))
                Surface(
                    color = PuzzleColors.Cloud.copy(alpha = 0.72f),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = "正在打开今日画面",
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                        color = PuzzleColors.TealDark,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
