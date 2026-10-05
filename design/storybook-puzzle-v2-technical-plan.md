# 故事拼图 V2 技术落地方案

关联图稿：

- `design/mockups/home-story-pager-concept.png`
- `design/mockups/game-book-canvas-concept.png`

## 1. 目标

将当前 App 从“选图拼图工具”升级为“故事阅读式拼图体验”：

- 首页支持图片组横向 pager，用户像翻故事书一样选择画面。
- 图片说明改为“画面故事”，服务于儿童理解故事，而不是描述图片文件。
- 难度从固定枚举改为线性碎片数量，范围 `10..300`。
- 拼图页保留矩形拼图区域，但视觉上呈现为图书页/画布，而不是普通矩形容器。

## 2. 当前工程影响面

主要改动文件：

- `composeApp/src/commonMain/kotlin/com/puzzle/game/game/GameState.kt`
- `composeApp/src/commonMain/kotlin/com/puzzle/game/game/GameViewModel.kt`
- `composeApp/src/commonMain/kotlin/com/puzzle/game/data/ThemeData.kt`
- `composeApp/src/commonMain/kotlin/com/puzzle/game/ui/MenuScreen.kt`
- `composeApp/src/commonMain/kotlin/com/puzzle/game/ui/GameScreen.kt`
- `composeApp/src/commonMain/kotlin/com/puzzle/game/ui/component/Brand.kt`
- `composeApp/src/commonMain/kotlin/com/puzzle/game/ui/component/DesignPrimitives.kt`

参考资源：

- `design/resources/storybook-v2-tokens.json`
- `design/resources/storybook-v2-content.json`
- `design/resources/brand-mark.svg`
- `design/resources/book-canvas-frame.svg`

## 3. 数据模型

### 3.1 替换固定难度枚举

当前：

```kotlin
enum class GameDifficulty(val pieceCount: Int, val label: String)
```

建议改为：

```kotlin
data class PuzzleDifficulty(
    val pieceCount: Int = 120,
    val min: Int = 10,
    val max: Int = 300
) {
    val label: String get() = "${pieceCount}片"
}
```

`GameState` 中：

```kotlin
val pieceCount: Int = 120
```

保留兼容方法：

```kotlin
fun selectPieceCount(value: Int) {
    _state.update { it.copy(pieceCount = value.coerceIn(10, 300)) }
}
```

`startGame()` 使用 `currentState.pieceCount`，不再从 `difficulty.pieceCount` 取值。

### 3.2 故事图片组

建议新增：

```kotlin
data class StoryImageGroup(
    val id: String,
    val title: String,
    val summary: String,
    val pages: List<StoryImagePage>
)

data class StoryImagePage(
    val id: String,
    val title: String,
    val story: String,
    val assetFile: String?,
    val themeId: String
)
```

首页 pager 选择的是 `StoryImagePage`，不是单个 `ThemeData`。`ThemeData.description` 可以继续作为 fallback，但新的故事介绍应来自 `StoryImagePage.story`。

`GameState` 建议新增：

```kotlin
val storyGroups: List<StoryImageGroup> = StoryPresets.groups
val selectedGroupIndex: Int = 0
val selectedPageIndex: Int = 0
val selectedStoryPage: StoryImagePage? = StoryPresets.groups.first().pages.first()
```

## 4. 首页 UI 落地

### 4.1 页面结构

```text
PuzzleBackground
└── Column(PagePadding)
    ├── HomeTopBar(BrandLockup + Settings)
    ├── StoryPager
    │   ├── side preview card left
    │   ├── active StoryPageCard
    │   └── side preview card right
    ├── PagerIndicator(1 / N)
    ├── PieceCountSlider(10..300)
    ├── CoralButton("开始拼图")
    └── Secondary action row("换主题", "换图片")
```

### 4.2 StoryPager

推荐先用 `LazyRow` 落地，后续再替换为 foundation pager：

- `LazyRow` 横向展示 story pages。
- 当前页居中，左右卡片露出 36-48dp。
- `userScrollEnabled = true`，点击侧卡可切换。
- 当前页改变时同步 `selectedGroupIndex / selectedPageIndex`。

卡片规格：

- 外层 `StoneSurface(radius = 22.dp)`
- 图片区域：矩形，`aspectRatio(0.84f)` 或在小屏使用 `heightIn`
- 故事区标题：`画面故事`
- 故事文案最多 2 行，超出省略。

### 4.3 PieceCountSlider

范围：

- min: `10`
- max: `300`
- default: `120`
- step: 建议 `1`，展示时可以按拖动实时更新。

Compose 落地：

```kotlin
Slider(
    value = state.pieceCount.toFloat(),
    onValueChange = { viewModel.selectPieceCount(it.roundToInt()) },
    valueRange = 10f..300f
)
```

产品建议：

- 10-80：低龄或快速体验。
- 80-180：推荐故事难度。
- 180-300：挑战模式。

性能注意：

- 不要在拖动 slider 时立刻生成碎片。
- 只更新 `pieceCount` 状态。
- 点击“开始拼图”后再执行图片切割。

## 5. 拼图页 UI 落地

### 5.1 页面结构

```text
PuzzleBackground
└── BoxWithConstraints
    ├── GameTopBar(back, progress, timer)
    ├── BookCanvasBoard
    │   ├── decorative book/canvas frame
    │   └── PuzzleImageSurface(rectangular)
    │       ├── faded reference image
    │       ├── placed pieces
    │       └── irregular target outlines
    ├── PieceTrayDock
    ├── InteractionHint
    └── FloatingDraggedPiece
```

### 5.2 BookCanvasBoard

目标：保留矩形拼图计算区域，不改变现有拖拽命中算法。

实现原则：

- `boardImageWindowPos` 和 `boardImageSize` 仍绑定在真实矩形图片区域。
- 图书页、画布边框、书签、角标全部是装饰层，不参与命中计算。
- 装饰层可以使用 Canvas 绘制，避免引入额外图片依赖。

建议组件：

```kotlin
@Composable
fun BookCanvasBoard(
    modifier: Modifier,
    imageAspect: Float,
    content: @Composable BoxScope.() -> Unit
)
```

绘制层级：

1. 石台阴影和底座。
2. 书本外框。
3. 页面背景。
4. 真实拼图矩形内容。
5. 金属角标/书签。

### 5.3 PieceTrayDock

目标：候选碎片像放在书签托盘上，卡槽尺寸稳定。

落地规则：

- 卡槽固定宽度，例如 `112.dp`。
- 选中态用内部发光/描边，不改变 `Modifier.width/height/border width`。
- 已放置碎片保持原槽位尺寸，显示轻量完成态。

## 6. 交互逻辑

### 首页

- 左右滑动 StoryPager：切换当前故事画面。
- 点击侧卡：滚动并选中该页。
- 拖动碎片数量 slider：仅更新 `pieceCount`。
- 开始拼图：读取当前 `selectedStoryPage + pieceCount`，进入生成阶段。

### 拼图页

- 拖拽碎片：沿用当前 50% overlap 命中逻辑。
- 点击碎片：选中后点击目标区域放置。
- 选中态：只绘制内发光，不引发布局重测。
- 返回：进入暂停确认弹层。

## 7. 状态与性能

关键约束：

- 10-300 片会显著提升碎片数量，UI 不应预生成所有高分辨率候选图。
- 候选栏只渲染可见碎片。
- `pieceBitmaps` 对 300 片应按需加载或使用裁剪渲染 fallback。
- 拼图区域不要绘制百万格网格；只绘制必要的目标轮廓。

推荐阈值：

- `pieceCount <= 80`：可生成候选缩略图缓存。
- `pieceCount > 80`：候选栏优先使用 `PieceImageContent` 实时裁剪，或按 LazyRow 可见项生成缩略图。
- `pieceCount > 180`：关闭全部细网格，仅保留目标高亮和已放置碎片。

## 8. 实施阶段

### Phase 1：数据与首页

- 新增 `StoryImageGroup / StoryImagePage`。
- 将 `GameDifficulty` 迁移为 `pieceCount`。
- 首页替换为 StoryPager + 线性 slider。
- 保证 Android/iOS 编译通过。

### Phase 2：拼图页视觉

- 新增 `BookCanvasBoard`。
- 替换当前普通棋盘边框。
- 保持 `boardImageWindowPos / boardImageSize` 绑定真实图片区域。
- 候选栏改为 `PieceTrayDock`。

### Phase 3：体验和性能

- 300 片生成性能回归测试。
- 候选栏 LazyRow 可见项渲染验证。
- 系统返回、暂停弹层、安全区复测。

## 9. 验收标准

- 首页可以在 10-300 范围选择碎片数。
- 首页可以横向翻页查看故事画面组。
- 首页展示的是“画面故事”，不是文件说明。
- 点击开始后，拼图使用当前 pager 页和当前碎片数量。
- 拼图页真实可交互区域仍是矩形。
- 图书/画布装饰不影响拖拽命中。
- 选中碎片不导致候选栏跳动。
- Android 与 iOS Kotlin 编译通过。
