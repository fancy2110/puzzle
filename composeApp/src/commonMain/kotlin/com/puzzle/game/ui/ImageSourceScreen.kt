package com.puzzle.game.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.puzzle.game.ui.component.BackIcon
import com.puzzle.game.ui.component.FragmaIconButton
import com.puzzle.game.ui.component.ImageIcon
import com.puzzle.game.ui.component.PuzzleBackground
import com.puzzle.game.ui.component.StoneSurface
import com.puzzle.game.ui.component.StoryButton
import com.puzzle.game.ui.component.StoryButtonTone
import com.puzzle.game.ui.adaptive.AdaptiveContent
import com.puzzle.game.ui.theme.PuzzleColors
import com.puzzle.game.ui.theme.PuzzleDimens

@Composable
fun ImageSourceScreen(
    onBack: () -> Unit,
    onPickBuiltIn: () -> Unit,
    onUseCurrent: () -> Unit,
    onGenerateAi: (String) -> Unit
) {
    var prompt by remember {
        mutableStateOf("月光花园里，一个小朋友和小蜗牛一起点亮温柔的小灯塔，儿童绘本插画，无文字")
    }

    PuzzleBackground {
        AdaptiveContent { spec ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(spec.pagePadding),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 640.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
            SimpleTopBar(title = "图片来源", onBack = onBack)

            ImageSourceCard(
                title = "内置图片",
                subtitle = "使用已内置的主题图片，Android 和 iOS 离线可用",
                action = "选择",
                onClick = onPickBuiltIn
            )
            ImageSourceCard(
                title = "当前主题",
                subtitle = "沿用首页已选主题，直接返回开始游戏",
                action = "使用",
                onClick = onUseCurrent
            )
            ImageSourceCard(
                title = "AI 生成",
                subtitle = "输入一句画面描述，生成后会进入拼图切割流程",
                action = "生成",
                onClick = { onGenerateAi(prompt) }
            )

            StoneSurface(modifier = Modifier.fillMaxWidth(), radius = 18.dp) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "生成提示词",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    OutlinedTextField(
                        value = prompt,
                        onValueChange = { prompt = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        shape = RoundedCornerShape(16.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = PuzzleColors.Cloud.copy(alpha = 0.78f),
                            unfocusedContainerColor = PuzzleColors.Cloud.copy(alpha = 0.62f),
                            focusedIndicatorColor = PuzzleColors.TealDark,
                            unfocusedIndicatorColor = PuzzleColors.Stone,
                            cursorColor = PuzzleColors.TealDark
                        )
                    )
                    StoryButton(
                        text = "用提示词生成拼图",
                        onClick = { onGenerateAi(prompt) },
                        modifier = Modifier.fillMaxWidth(),
                        tone = StoryButtonTone.Primary
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "相册导入和完整 AI 提示词流程已预留入口，优先保证当前内置图和程序图在双端稳定运行。",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
                }
            }
        }
    }
}

@Composable
private fun ImageSourceCard(
    title: String,
    subtitle: String,
    action: String,
    onClick: () -> Unit
) {
    StoneSurface(modifier = Modifier.fillMaxWidth(), radius = 18.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ImageIcon(modifier = Modifier.size(30.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
            StoryButton(
                text = action,
                onClick = onClick,
                tone = StoryButtonTone.Secondary,
                height = 42.dp,
                modifier = Modifier.width(74.dp)
            )
        }
    }
}

@Composable
internal fun SimpleTopBar(
    title: String,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FragmaIconButton(
            onClick = onBack,
            size = 50.dp
        ) {
            BackIcon(modifier = Modifier.size(23.dp))
        }
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(48.dp))
    }
}
