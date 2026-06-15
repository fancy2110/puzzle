package com.puzzle.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.puzzle.game.ui.component.PuzzleBackground
import com.puzzle.game.ui.theme.PuzzleDimens

@Composable
fun ImageSourceScreen(
    onBack: () -> Unit,
    onPickBuiltIn: () -> Unit,
    onUseCurrent: () -> Unit,
    onGenerateAi: () -> Unit
) {
    PuzzleBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(PuzzleDimens.PagePadding),
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
                subtitle = "当前版本先使用所选主题生成拼图，后续接入提示词和 API Key",
                action = "开始",
                onClick = onGenerateAi
            )

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

@Composable
private fun ImageSourceCard(
    title: String,
    subtitle: String,
    action: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
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
        Button(
            onClick = onClick,
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 14.dp)
        ) {
            Text(action)
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
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onBack, contentPadding = PaddingValues(horizontal = 0.dp)) {
            Text("返回")
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
