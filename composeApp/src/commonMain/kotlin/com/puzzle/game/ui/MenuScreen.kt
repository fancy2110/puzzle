package com.puzzle.game.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.puzzle.game.data.AssetLoader
import com.puzzle.game.data.PuzzlePictureGenerator
import com.puzzle.game.data.StoryPageData
import com.puzzle.game.decodeToImageBitmap
import com.puzzle.game.game.GameViewModel
import com.puzzle.game.ui.component.BrandMark
import com.puzzle.game.ui.component.CloudButton
import com.puzzle.game.ui.component.CoralButton
import com.puzzle.game.ui.component.PuzzleBackground
import com.puzzle.game.ui.component.PuzzlePieceIcon
import com.puzzle.game.ui.component.SettingsDesignButton
import com.puzzle.game.ui.component.StoneSurface
import com.puzzle.game.ui.adaptive.AdaptiveContent
import com.puzzle.game.ui.adaptive.AdaptiveLayoutMode
import com.puzzle.game.ui.theme.FragmaDimens
import com.puzzle.game.ui.theme.PuzzleColors
import com.puzzle.game.ui.theme.PuzzleDimens
import kotlin.math.roundToInt

@Composable
fun MenuScreen(
    viewModel: GameViewModel,
    onStartGame: () -> Unit,
    onPickTheme: () -> Unit,
    onPickImage: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val pageIndex = state.selectedStoryPageIndex.coerceIn(0, viewModel.storyPages.lastIndex)
    val previews = remember(viewModel.storyPages) {
        viewModel.storyPages.associate { page ->
            page.id to loadStoryPreview(page)
        }
    }
    val pagerState = rememberPagerState(initialPage = pageIndex, pageCount = { viewModel.storyPages.size })

    LaunchedEffect(pageIndex) {
        if (pagerState.currentPage != pageIndex) {
            pagerState.animateScrollToPage(pageIndex)
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            if (page != viewModel.state.value.selectedStoryPageIndex) {
                viewModel.selectStoryPage(page)
            }
        }
    }

    PuzzleBackground {
        AdaptiveContent { spec ->
            val isCompactHeight = spec.mode == AdaptiveLayoutMode.Constrained
            val verticalGap = if (isCompactHeight) 9.dp else 13.dp

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(spec.pagePadding),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = spec.contentMaxWidth),
                    verticalArrangement = Arrangement.spacedBy(verticalGap),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    HomeTopBar(onOpenSettings = onOpenSettings, compact = isCompactHeight)

                    if (spec.mode == AdaptiveLayoutMode.TabletLandscape) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(spec.paneGap),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StoryPagerCard(
                                pages = viewModel.storyPages,
                                previews = previews,
                                pageIndex = pageIndex,
                                pagerState = pagerState,
                                compact = false,
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 460.dp, max = 620.dp)
                            )
                            HomeActionPanel(
                                pieceCount = state.pieceCount,
                                onPieceCountChange = viewModel::selectPieceCount,
                                onStartGame = onStartGame,
                                onPickTheme = onPickTheme,
                                onPickImage = onPickImage,
                                modifier = Modifier.width(340.dp)
                            )
                        }
                    } else {
                        StoryPagerCard(
                            pages = viewModel.storyPages,
                            previews = previews,
                            pageIndex = pageIndex,
                            pagerState = pagerState,
                            compact = isCompactHeight,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = if (isCompactHeight) 310.dp else 390.dp, max = 560.dp)
                                .weight(1f)
                        )

                        HomeActionPanel(
                            pieceCount = state.pieceCount,
                            onPieceCountChange = viewModel::selectPieceCount,
                            onStartGame = onStartGame,
                            onPickTheme = onPickTheme,
                            onPickImage = onPickImage
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeActionPanel(
    pieceCount: Int,
    onPieceCountChange: (Int) -> Unit,
    onStartGame: () -> Unit,
    onPickTheme: () -> Unit,
    onPickImage: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        PieceCountSlider(
            pieceCount = pieceCount,
            onPieceCountChange = onPieceCountChange
        )

        CoralButton(
            text = "开始拼图",
            onClick = onStartGame,
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CloudButton(
                text = "故事库",
                onClick = onPickTheme,
                modifier = Modifier.weight(1f)
            )
            CloudButton(
                text = "换图片",
                onClick = onPickImage,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

private fun loadStoryPreview(page: StoryPageData): ImageBitmap {
    val assetFile = page.theme.assetFile
    if (assetFile != null) {
        val assetBitmap = AssetLoader.readBytes(assetFile)?.let { decodeToImageBitmap(it) }
        if (assetBitmap != null) return assetBitmap
    }
    return PuzzlePictureGenerator.generate(page.theme, 900, 680)
}

@Composable
private fun HomeTopBar(onOpenSettings: () -> Unit, compact: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BrandMark(size = if (compact) FragmaDimens.BrandMarkCompact else FragmaDimens.BrandMark)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = FragmaDimens.StoryCardPadding),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "故事拼图",
                color = PuzzleColors.TealDark,
                fontSize = if (compact) 30.sp else 38.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                "拼完整画面，读懂每一页故事",
                color = PuzzleColors.Muted,
                fontSize = if (compact) 12.sp else 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        SettingsDesignButton(
            onClick = onOpenSettings,
            size = if (compact) FragmaDimens.SettingsButtonCompact else FragmaDimens.SettingsButton
        )
    }
}

@Composable
private fun StoryPagerCard(
    pages: List<StoryPageData>,
    previews: Map<String, ImageBitmap>,
    pageIndex: Int,
    pagerState: androidx.compose.foundation.pager.PagerState,
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            pageSpacing = if (compact) 12.dp else 18.dp,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = if (compact) 28.dp else 38.dp),
            modifier = Modifier
                .fillMaxSize()
        ) { index ->
            val storyPage = pages[index]
            val preview = previews[storyPage.id] ?: previews.values.first()
            val isActive = index == pagerState.currentPage

            StoryPageCard(
                storyPage = storyPage,
                preview = preview,
                isActive = isActive,
                compact = compact,
                modifier = Modifier
                    .fillMaxSize()
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 30.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PagerDots(selectedIndex = pageIndex, total = pages.size)
            Text(
                "${pageIndex + 1} / ${pages.size}",
                color = PuzzleColors.TealDark,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun StoryPageCard(
    storyPage: StoryPageData,
    preview: ImageBitmap,
    isActive: Boolean,
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    StoneSurface(
        modifier = modifier
            .graphicsLayer {
                scaleX = if (isActive) 1f else 0.88f
                scaleY = if (isActive) 1f else 0.92f
                alpha = if (isActive) 1f else 0.72f
            },
        radius = FragmaDimens.StoryCardRadius
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(PuzzleColors.Cloud.copy(alpha = 0.72f))
                .padding(if (compact) FragmaDimens.StoryCardPaddingCompact else FragmaDimens.StoryCardPadding),
            verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(FragmaDimens.StoryImageRadius))
                    .background(Color.White.copy(alpha = 0.62f))
                    .border(1.dp, PuzzleColors.Stone.copy(alpha = 0.58f), RoundedCornerShape(FragmaDimens.StoryImageRadius))
            ) {
                StoryGeometryBackdrop(modifier = Modifier.matchParentSize())
                Image(
                    bitmap = preview,
                    contentDescription = storyPage.title,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(if (compact) 4.dp else 6.dp)
                        .clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Crop
                )
                CornerGlyphs(modifier = Modifier.matchParentSize())
            }

            StoryIntroPanel(storyPage = storyPage, compact = compact)
        }
    }
}

@Composable
private fun StoryIntroPanel(storyPage: StoryPageData, compact: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = if (compact) 86.dp else 104.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(PuzzleColors.Cloud.copy(alpha = 0.88f))
            .padding(horizontal = 16.dp, vertical = if (compact) 10.dp else 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            StoryTitleRule("画面故事")
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                storyPage.story,
                color = PuzzleColors.StoneDark,
                fontSize = if (compact) 14.sp else 16.sp,
                lineHeight = if (compact) 20.sp else 23.sp,
                maxLines = if (compact) 2 else 3,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun StoryTitleRule(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Canvas(modifier = Modifier.weight(1f).height(10.dp)) {
            drawLine(
                color = PuzzleColors.Gold.copy(alpha = 0.54f),
                start = Offset(0f, size.height / 2f),
                end = Offset(size.width, size.height / 2f),
                strokeWidth = 1.dp.toPx()
            )
            drawCircle(PuzzleColors.Gold.copy(alpha = 0.72f), radius = 3.dp.toPx(), center = Offset(size.width, size.height / 2f))
        }
        Text(
            text,
            color = PuzzleColors.StoneDark,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Canvas(modifier = Modifier.weight(1f).height(10.dp)) {
            drawLine(
                color = PuzzleColors.Gold.copy(alpha = 0.54f),
                start = Offset(0f, size.height / 2f),
                end = Offset(size.width, size.height / 2f),
                strokeWidth = 1.dp.toPx()
            )
            drawCircle(PuzzleColors.Gold.copy(alpha = 0.72f), radius = 3.dp.toPx(), center = Offset(0f, size.height / 2f))
        }
    }
}

@Composable
private fun PieceCountSlider(
    pieceCount: Int,
    onPieceCountChange: (Int) -> Unit
) {
    StoneSurface(modifier = Modifier.fillMaxWidth(), radius = FragmaDimens.SliderPanelRadius) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PuzzlePieceIcon(modifier = Modifier.size(22.dp))
                    Text(
                        "碎片数量",
                        fontWeight = FontWeight.Bold,
                        color = PuzzleColors.StoneDark,
                        fontSize = 16.sp
                    )
                }
                Surface(
                    modifier = Modifier.shadow(4.dp, RoundedCornerShape(18.dp)),
                    shape = RoundedCornerShape(18.dp),
                    color = PuzzleColors.Cloud
                ) {
                    Text(
                        "$pieceCount 片",
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                        color = PuzzleColors.TealDark,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Slider(
                value = pieceCount.toFloat(),
                onValueChange = { onPieceCountChange(it.roundToInt()) },
                valueRange = 10f..300f,
                colors = SliderDefaults.colors(
                    thumbColor = PuzzleColors.Coral,
                    activeTrackColor = PuzzleColors.Teal,
                    inactiveTrackColor = PuzzleColors.Stone.copy(alpha = 0.46f)
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("10", color = PuzzleColors.Muted, fontSize = 12.sp)
                Text("80", color = PuzzleColors.Muted, fontSize = 12.sp)
                Text("150", color = PuzzleColors.Muted, fontSize = 12.sp)
                Text("220", color = PuzzleColors.Muted, fontSize = 12.sp)
                Text("300", color = PuzzleColors.Muted, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun CornerGlyphs(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val pad = 16.dp.toPx()
        val len = 26.dp.toPx()
        val color = PuzzleColors.Gold.copy(alpha = 0.62f)
        drawLine(color, Offset(pad, pad), Offset(pad + len, pad), strokeWidth = 1.2.dp.toPx())
        drawLine(color, Offset(pad, pad), Offset(pad, pad + len), strokeWidth = 1.2.dp.toPx())
        drawLine(color, Offset(size.width - pad, pad), Offset(size.width - pad - len, pad), strokeWidth = 1.2.dp.toPx())
        drawLine(color, Offset(size.width - pad, pad), Offset(size.width - pad, pad + len), strokeWidth = 1.2.dp.toPx())
        drawLine(color, Offset(pad, size.height - pad), Offset(pad + len, size.height - pad), strokeWidth = 1.2.dp.toPx())
        drawLine(color, Offset(pad, size.height - pad), Offset(pad, size.height - pad - len), strokeWidth = 1.2.dp.toPx())
        drawLine(color, Offset(size.width - pad, size.height - pad), Offset(size.width - pad - len, size.height - pad), strokeWidth = 1.2.dp.toPx())
        drawLine(color, Offset(size.width - pad, size.height - pad), Offset(size.width - pad, size.height - pad - len), strokeWidth = 1.2.dp.toPx())
    }
}

@Composable
private fun PagerDots(selectedIndex: Int, total: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(total) { index ->
            Box(
                modifier = Modifier
                    .size(width = if (index == selectedIndex) 18.dp else 7.dp, height = 7.dp)
                    .clip(CircleShape)
                    .background(
                        if (index == selectedIndex) PuzzleColors.Coral
                        else PuzzleColors.Cloud.copy(alpha = 0.78f)
                    )
                    .border(
                        1.dp,
                        PuzzleColors.Stone.copy(alpha = 0.54f),
                        CircleShape
                    )
            )
        }
    }
}

@Composable
private fun StoryGeometryBackdrop(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawRoundRect(
            color = Color.White.copy(alpha = 0.26f),
            topLeft = Offset(w * 0.08f, h * 0.05f),
            size = Size(w * 0.84f, h * 0.72f),
            cornerRadius = CornerRadius(28.dp.toPx(), 28.dp.toPx())
        )

        val stair = Path().apply {
            moveTo(w * 0.05f, h * 0.82f)
            lineTo(w * 0.34f, h * 0.67f)
            lineTo(w * 0.56f, h * 0.76f)
            lineTo(w * 0.25f, h * 0.93f)
            close()
        }
        drawPath(stair, PuzzleColors.Gold.copy(alpha = 0.16f))
        drawPath(stair, PuzzleColors.Stone.copy(alpha = 0.24f), style = Stroke(width = 1.2.dp.toPx()))

        drawCircle(
            color = PuzzleColors.Teal.copy(alpha = 0.14f),
            radius = w * 0.10f,
            center = Offset(w * 0.86f, h * 0.83f)
        )
        drawCircle(
            color = PuzzleColors.Coral.copy(alpha = 0.12f),
            radius = w * 0.07f,
            center = Offset(w * 0.17f, h * 0.18f)
        )
    }
}
