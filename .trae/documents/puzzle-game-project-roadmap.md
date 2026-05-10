# 趣味拼图 (KidPuzzle) - 项目任务拆分与 Roadmap

## 版本: v1.0 | 日期: 2026-05-10 | 目标用户: 3-10岁低幼儿

---

## 一、项目总览与核心目标

### 1.1 项目定位

基于 **AI 自动生成拼图素材** 的跨平台（Android / iOS / 鸿蒙）益智拼图游戏，面向 3~10 岁低龄儿童，具备以下核心差异化能力：

- **AI 驱动素材生成**：根据文本描述（prompt）调用 AI 模型自动生成拼图底图
- **智能随机拆分**：将任意图片拆分为不规则形状的拼图碎片
- **零文字操作**：语音提示 + emoji 图标驱动，消除识字量不足带来的理解障碍
- **无时间压力**：不设计时器，不以速度评判，保护儿童持续成就感

### 1.2 核心工程目标（OKR）

| 维度 | 目标 | 衡量标准 |
|------|------|----------|
| **功能完整性** | 完成从菜单→AI选图→拼图→完成反馈的完整闭环 | 10 个核心验证 case 全部通过 |
| **跨平台可用性** | Android APK 可安装运行；iOS Framework 可在模拟器渲染 | `assembleDebug` + `linkDebugFrameworkIosSimulatorArm64` 均 BUILD SUCCESSFUL |
| **低幼友好度** | 3 岁儿童可在无大人指导下独立完成一局简单拼图 | 内部亲子测试 ≥ 80% 成功率 |
| **代码质量** | commonMain 代码覆盖率 ≥ 80%，平台代码最小化 | commonMain 代码占比 > 85% |
| **AI 集成** | 对接至少一个真实 AI 图像生成 API | `AIImageProvider` 有真实实现在生产可用 |

### 1.3 技术架构图

```
┌─────────────────────────────────────────────────────────────┐
│                     App.kt (入口)                            │
├─────────────────────────────────────────────────────────────┤
│  Phase: MENU → GENERATING → PLAYING → COMPLETED            │
├────────────┬────────────────────────┬───────────────────────┤
│   MenuScreen   │     GameScreen       │   CompletedScreen    │
│ (难度选择+     │  (拼图区域+碎片列表)   │   (庆祝动画+分享)    │
│  AI提示词输入) │                      │                      │
├────────────┴────────────────────────┴───────────────────────┤
│              GameViewModel (MVVM - StateFlow)                │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌───────────────┐  ┌───────────────────┐  │
│  │ AIImageGen   │  │ PuzzleEngine  │  │  GameState        │  │
│  │ (Provider     │  │  → ImageSplit │  │  (Phase/Diff/     │  │
│  │  Interface)  │  │     (算法)     │  │   Pieces/Placed)  │  │
│  └──────┬───────┘  └───────────────┘  └───────────────────┘  │
│         │                                                    │
│  ┌──────┴──────────────────────────────────────────────┐    │
│  │            平台差异层 (expect / actual)               │    │
│  │  Android: Ktor-OkHttp  │  iOS: Ktor-Darwin           │    │
│  └──────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
```

### 1.4 代码规范与约定

| 规范项 | 规则 |
|--------|------|
| **语言** | 代码注释使用中文，变量/函数/类名使用英文（Kotlin 惯例） |
| **包结构** | `com.puzzle.game.<layer>` — `ai`, `engine`, `game`, `ui` |
| **文件命名** | 平台文件：`*.android.kt` / `*.ios.kt`；UI 页面：`*Screen.kt` |
| **架构模式** | MVVM：ViewModel + StateFlow，UI 通过 `collectAsState()` 订阅 |
| **异步规范** | 所有 IO 操作通过 `viewModelScope.launch` 调度，禁止阻塞主线程 |
| **空安全** | 全部使用 Kotlin 空安全类型，禁止 `!!` 强制解包 |
| **Compose 规范** | State 提升至 ViewModel，Composable 函数为无副作用纯渲染 |
| **版本锁定** | 所有依赖版本统一在 `gradle/libs.versions.toml` 管理 |

### 1.5 需要使用的 Skills

| Skill | 用途 | 适用阶段 |
|-------|------|----------|
| `skill-creator` | 创建项目专属 Skill（如 `puzzle-image-processor`） | 当需要封装图片处理复杂流程时 |

---

## 二、当前工程现状分析

### 2.1 已有能力（✅ 已完成）

| 模块 | 文件 | 状态 | 说明 |
|------|------|------|------|
| Gradle 构建 | `build.gradle.kts`, `settings.gradle.kts`, `libs.versions.toml` | ✅ 已可用 | Android `compileDebugKotlinAndroid` BUILD SUCCESSFUL |
| KMP 多平台配置 | `composeApp/build.gradle.kts` | ✅ 已可用 | Android target + iOS targets (x64/arm64/simulator) |
| 拼图数据模型 | `Block.kt`, `Position.kt`, `Rect.kt`, `PuzzlePiece.kt` | ✅ 已可用 | 完整类型定义 |
| 拼图拆分算法 | `ImageSplitter.kt` | ✅ 已可用 | 基于网格的随机区域生长算法 |
| 拼图引擎入口 | `PuzzleEngine.kt` | ✅ 已可用 | loadImage → splitImage → shufflePieces |
| AI 模块骨架 | `AIImageGenerator.kt` | ⚠️ 仅 Mock | 接口已抽象，`MockAIImageProvider` 返回占位数据 |
| MVVM 状态管理 | `GameViewModel.kt`, `GameState.kt` | ✅ 已可用 | StateFlow 驱动的阶段状态机 |
| 菜单 UI | `MenuScreen.kt` | ⚠️ 基础原型 | 难度选择 + 提示词输入 + 开始按钮 |
| 游戏 UI | `GameScreen.kt` | ⚠️ 基础原型 | 生成中/游戏中/完成三种状态，使用纯色占位替代图片 |
| 主题 | `Theme.kt` | ✅ 已可用 | Material3 lightColorScheme |
| 平台入口 | `MainActivity.kt`, `MainViewController.kt` | ✅ 已可用 | Android Activity + iOS ViewController |

### 2.2 待实现的关键能力（❌ 缺失）

| 能力 | 优先级 | 说明 |
|------|--------|------|
| **真实图片渲染** | P0 | 当前拼图碎片使用纯色块占位，需替换为真实裁剪的图片像素 |
| **AI 图片生成对接** | P0 | Mock 实现需替换为真实 AI API（Gemini Imagen / OpenAI DALL·E） |
| **拖拽交互** | P0 | 当前为点击放置，需实现拖拽拼图碎片到目标区域的交互 |
| **碎片形状多样化** | P1 | 当前为矩形网格块，需支持不规则边缘形状（凹凸咬合） |
| **音效系统** | P1 | 碎片放置音效、完成庆祝音效、背景音乐 |
| **动画系统** | P1 | 碎片吸附动画、完成庆祝动画（粒子/星星） |
| **图片缓存** | P1 | 减少重复 AI 请求，本地缓存已生成的图片 |
| **语音提示** | P2 | TTS 朗读操作指引，降低识字门槛 |
| **多语言** | P2 | 至少中/英文，优先中文 |
| **家长控制** | P2 | 使用时长限制、AI 生成次数限制 |
| **鸿蒙适配** | P3 | KuiklyUI 编译验证 |

### 2.3 与低幼产品标准的差距

对比主流儿童拼图游戏（Kids' Puzzles 4+、儿童拼图益智、多多拼图），当前缺失：

1. ❌ 无真实图片渲染 → 儿童无法看到动物/场景图，缺乏吸引力
2. ❌ 无 AI 图片生成 → 内容固定不可扩展
3. ❌ 无拖拽交互 → 交互趣味性不足
4. ❌ 无音效/动画 → 缺乏正面激励反馈
5. ❌ 难度梯度设计不完整 → 仅三种固定块数，缺少 4-9 块超简模式
6. ❌ 无语音/图标引导 → 低龄儿童可能不理解操作

---

## 三、完整任务拆分（按阶段）

### 阶段概览

```
Phase 0: 基础设施补全 (3 天)
  └─ Phase 1: 核心玩法闭环 (5 天)
       └─ Phase 2: 低幼体验优化 (4 天)
            └─ Phase 3: AI 集成与内容丰富 (4 天)
                 └─ Phase 4: 质量打磨与跨平台验证 (3 天)
                      └─ Phase 5: 鸿蒙适配探索 (3 天)
```

---

### Phase 0: 基础设施补全（里程碑：图片可渲染到拼图碎片）

**目标**: 让拼图碎片能够显示真实图片像素，而非纯色占位

| 任务ID | 任务描述 | 文件范围 | 验收标准 | 预估 |
|--------|---------|---------|---------|------|
| **P0-1** | 实现 `ImageBitmapLoader` expect/actual 接口，Android 端用 Coil 加载，iOS 端用 Ktor 下载，统一转为 `ImageBitmap` | `commonMain`: `engine/ImageLoader.kt` (expect); `androidMain`: `engine/ImageLoader.android.kt`; `iosMain`: `engine/ImageLoader.ios.kt` | 能从 URL/本地路径加载图片为 `ImageBitmap`，Android 编译通过 | 2h |
| **P0-2** | 在 `PuzzleEngine` 中增加 `loadImageBitmap(bitmap: ImageBitmap)` 方法，将 `ImageBitmap` 保存为引擎内部状态；`ImageSplitter` 增加 `cropPiece(piece, bitmap): ImageBitmap` 方法 | `engine/PuzzleEngine.kt`, `engine/ImageSplitter.kt` | 给定一个 `ImageBitmap` 和拆分后的 `PuzzlePiece`，能裁剪出该碎片对应的图片像素区块 | 3h |
| **P0-3** | 改造 `PieceCard` Composable，替换纯色 Block 为 `Image` 组件显示裁剪后的碎片图片 | `ui/GameScreen.kt` (PieceCard) | 拼图碎片列表中每个卡片显示对应位置的图片裁剪区域 | 2h |
| **P0-4** | 在 `PlayingScreen` 的目标区域中，根据已放置的碎片渲染完整预览图（提示用） | `ui/GameScreen.kt` (PlayingScreen) | 目标区域显示原始图片的淡色半透明预览 | 1.5h |
| **P0-5** | 添加展示用内置图片资源：在 `commonMain/composeResources` 中加入 3-5 张离线卡通图片作为默认素材 | `composeApp/src/commonMain/composeResources/drawable/` | 不依赖网络的情况下，APP 启动后可直接选择内置图片开始游戏 | 1h |
| **P0-6** | 在 `GameViewModel` 中集成 `ImageBitmapLoader`：`startGame()` 先加载图片 → 再拆分成碎片 → 每个碎片关联裁剪区域 | `game/GameViewModel.kt` | 完整流程：选择内置图片 → 点击开始 → 游戏界面显示真实图片的碎片 | 2h |
| **P0-7** | 验证：Android `assembleDebug` + 真机/模拟器运行，确认可看到图片碎片 | | APK 安装后，选择内置图片开始游戏，碎片区域显示图片内容 | 1h |

**Phase 0 依赖**: 无（当前工程可直接开始）

---

### Phase 1: 核心玩法闭环（里程碑：拖拽拼图可玩）

**目标**: 实现完整的拖拽拼图→放置→验证→完成的游戏核心循环

| 任务ID | 任务描述 | 文件范围 | 验收标准 | 预估 | 依赖 |
|--------|---------|---------|---------|------|------|
| **P1-1** | 重构拼图区域为目标网格布局：根据 `PuzzlePiece.blocks` 计算碎片在完整图中的行列位置，在目标区域渲染网格，空位显示虚线框 | `ui/GameScreen.kt`, 新增 `ui/component/PuzzleBoard.kt` | 4×3 网格中 12 个槽位，已放置的显示图片碎片，未放置的显示虚线框 | 3h | P0-3 |
| **P1-2** | 实现碎片拖拽交互：使用 `Modifier.pointerInput` + `detectDragGestures`，碎片跟随手指移动，松手时检测是否在目标槽位上方 | `ui/GameScreen.kt`, `ui/component/DraggablePiece.kt` | 手指拖动碎片卡片在屏幕上移动，松手后如果对齐到目标槽位则吸附 | 4h | P1-1 |
| **P1-3** | 实现吸附判定逻辑：计算碎片中心坐标与目标槽位中心坐标的欧氏距离，小于阈值时触发吸附动画 | `game/GameViewModel.kt` 新增 `tryPlacePiece(pieceId, dropX, dropY)` | 碎片拖动到正确槽位 ±20dp 范围内松手，自动吸附到正确位置 | 2h | P1-2 |
| **P1-4** | 实现碎片回弹动画：如果未对齐任何槽位，碎片以弹性动画回到碎片列表区域 | `ui/component/DraggablePiece.kt` (Animatable) | 松手后若不吸附，碎片平滑回弹到原位置 | 1.5h | P1-2 |
| **P1-5** | 添加碎片旋转功能：双击/双指旋转碎片 90°（低龄用户可关闭此选项） | `game/GameState.kt` 新增 `allowRotation`, `PuzzlePiece` 新增 `rotation` | 双击碎片，碎片逆时针旋转 90°，`PuzzlePiece.rotation` 同步更新 | 2h | P1-1 |
| **P1-6** | 重新设计难度梯度：`GameDifficulty` 扩展为 5 级 | `game/GameState.kt` | EASY(4块/2×2) / SIMPLE(9块/3×3) / MEDIUM(16块/4×4) / HARD(25块/5×5) / EXPERT(36块/6×6) | 0.5h | - |
| **P1-7** | 实现碎片列表自动滚动：当前拖动碎片靠近屏幕边缘时，碎片列表自动滚动以显示更多碎片 | `ui/GameScreen.kt` (LazyVerticalGrid + scrollState) | 碎片超过 10 个时，可滚动浏览全部碎片 | 1h | - |
| **P1-8** | 验证：完整游戏流程测试 | | 从菜单选择难度→开始→拖拽碎片到目标区域→完成全部→显示完成页面 | 1.5h | P1-1~P1-7 |

**Phase 1 依赖**: Phase 0 全部完成

---

### Phase 2: 低幼体验优化（里程碑：3岁儿童可独立操作）

**目标**: 让界面和交互完全适配 3-10 岁低幼儿的操作水平和认知能力

| 任务ID | 任务描述 | 文件范围 | 验收标准 | 预估 | 依赖 |
|--------|---------|---------|---------|------|------|
| **P2-1** | 低幼主题重设计：增大按钮尺寸（最小 64dp 触摸目标）、提高色彩对比度、使用圆角大卡片、增大字体（最小 16sp） | `ui/MenuScreen.kt`, `ui/GameScreen.kt`, `ui/theme/Theme.kt` | 所有交互元素 ≥ 64dp，字体 ≥ 16sp，主色使用高饱和暖色调 | 3h | P1-8 |
| **P2-2** | 图标驱动导航：用 emoji/大图标替代文字按钮（← 返回 → 🏠，重置 → 🔄，开始 → ▶️） | `ui/MenuScreen.kt`, `ui/GameScreen.kt` | 菜单和游戏中所有操作按钮均有 emoji 图标，无纯文字按钮 | 2h | P1-8 |
| **P2-3** | 完成庆祝动画：`CompletedScreen` 增加 Lottie/帧动画（星星雨、气球上升）+ 鼓掌音效 | `ui/CompletedScreen.kt` (独立拆分)，新增 `ui/animation/CelebrationAnimation.kt` | 完成拼图后播放星星动画，持续 3 秒，可点击跳过 | 3h | P1-8 |
| **P2-4** | 碎片放置反馈：每次成功放置碎片时播放短促的"叮咚"音效 + 碎片微缩放动画 | `ui/component/PlacementFeedback.kt` | 碎片吸附到目标位置时缩放到 105% → 回到 100%，伴随音效 | 1.5h | P1-3 |
| **P2-5** | 语音提示系统：使用 TTS（平台 API）朗读当前步骤（"把小猫咪的头放到左上角"），在开始时播报一次 | `commonMain`: `engine/TtsController.kt` (expect); `androidMain`/`iosMain`: actual 实现 | 进入游戏后自动播报一次语音指引；菜单页无语音 | 2h | - |
| **P2-6** | 错误操作降低挫败感：碎片放置到错误位置时，碎片微微抖动后回弹，不显示错误提示 | `ui/component/DraggablePiece.kt` | 错误放置无红色 X 标记，仅抖动回弹，不打断游戏流 | 1h | P1-4 |
| **P2-7** | 添加"看一看"提示功能：点击💡按钮，短暂显示未放置碎片的正确位置（淡色闪烁），持续 3 秒后消失 | `game/GameViewModel.kt` 新增 `showHint()`, `ui/GameScreen.kt` 新增 HintOverlay | 点击灯泡按钮后，目标区域闪烁显示 1 个随机未放置碎片的位置 | 2h | P1-1 |
| **P2-8** | 验证：低幼用户可用性测试 checklist | | 3岁儿童在 3 分钟内独立完成 4 块拼图；5 岁儿童完成 9 块 | 2h | P2-1~P2-7 |

**Phase 2 依赖**: Phase 1 全部完成

---

### Phase 3: AI 集成与内容丰富（里程碑：AI 生成拼图可用）

**目标**: 对接真实的 AI 图片生成 API，实现用户自定义主题素材

| 任务ID | 任务描述 | 文件范围 | 验收标准 | 预估 | 依赖 |
|--------|---------|---------|---------|------|------|
| **P3-1** | 实现 `GeminiImageProvider`：对接 Google Gemini Imagen API，接收 prompt + 风格参数，返回生成图片的 URL | `ai/GeminiImageProvider.kt` (commonMain) | 输入"可爱的小狗在草地上玩耍"，返回 Gemini 生成的图片 URL | 3h | - |
| **P3-2** | 实现 `OpenAIImageProvider`：对接 OpenAI DALL·E API，作为备选方案 | `ai/OpenAIImageProvider.kt` (commonMain) | 与 Gemini 接口行为一致，返回图片 URL | 2h | - |
| **P3-3** | `AIImageProvider` 增加故障转移（fallback）机制：Gemini 超时→自动切换 OpenAI；两者均失败→使用内置图片 | `ai/AIImageGenerator.kt` 重构为 `AIImageGenerator(providers: List<AIImageProvider>)` | 网络异常时不崩溃，自动降级到内置图片 | 2h | P3-1 |
| **P3-4** | 实现 AI 图片本地缓存：基于 `okio` 文件系统 + LRU 策略，避免重复 API 请求 | `ai/ImageCache.kt` (commonMain) | 相同 prompt 再次请求时直接返回本地缓存的图片，不调用 API | 2h | - |
| **P3-5** | 增强菜单 UI：增加主题选择区域（六宫格图标：🐱动物/🚀太空/🌊海洋/🌺花朵/🦕恐龙/🎨随机），替换纯文本输入 | `ui/MenuScreen.kt`, 新增 `ui/component/ThemePicker.kt` | 儿童点击动物图标 → 自动填充预设 prompt → 点击开始 → AI 生成对应图片 | 2.5h | P3-1 |
| **P3-6** | 实现图片生成进度 UI：`GeneratingScreen` 增加进度条 + 预估等待时间 + 可爱动画（小动物在跑） | `ui/GeneratingScreen.kt` (独立拆分) | 生成过程中显示进度条，预估剩余时间，配有小动物跑步动画 | 1.5h | P3-1 |
| **P3-7** | 图片预览与重新生成：AI 生成后先在预览页显示完整图片，用户确认后再进入拼图；不满意可点"换一张" | `ui/AiPreviewScreen.kt` (新增) | AI 生成后展示完整图片 + "开始拼图" 和 "换一张" 两个按钮 | 2h | P3-1 |
| **P3-8** | 验证：AI 端到端测试 | | 输入动物主题→等待生成→预览图片→确认→进入拼图→完成完整流程 | 1.5h | P3-1~P3-7 |

**Phase 3 依赖**: Phase 2 全部完成；需要有效的 API Key

---

### Phase 4: 质量打磨与跨平台验证（里程碑：Android + iOS 均可稳定运行）

**目标**: 质量收尾，确保两个平台均可构建和运行

| 任务ID | 任务描述 | 文件范围 | 验收标准 | 预估 | 依赖 |
|--------|---------|---------|---------|------|------|
| **P4-1** | Android 端到端测试：`assembleDebug` → 安装 APK → 全流程验证（菜单→选图→拼图→完成） | | 一次完整游戏流程无 crash，无 ANR，碎片交互不卡顿 | 2h | P3-8 |
| **P4-2** | iOS framework 构建验证：`linkDebugFrameworkIosSimulatorArm64` | | BUILD SUCCESSFUL，生成 `ComposeApp.framework` | 1h | - |
| **P4-3** | iOS Simulator 运行验证：Xcode 打开 `iosApp` → 运行 → 验证 UI 布局和交互 | `iosApp/` | iOS Simulator 中可看到完整的菜单、拼图、完成页面，交互正常 | 2h | P4-2 |
| **P4-4** | 屏幕适配专项：测试 4.7"~6.9" 手机 + 7.9"~12.9" iPad 布局 | `ui/MenuScreen.kt`, `ui/GameScreen.kt` | 平板横屏时拼图区域和碎片列表左右并排；手机竖屏时上下排列 | 3h | P4-1 |
| **P4-5** | 性能优化：`ImageSplitter` 拆分大数据图片时使用 `Dispatchers.Default` 避免 UI 卡顿；LazyVerticalGrid 碎片列表增加 key 优化重组 | `engine/PuzzleEngine.kt`, `game/GameViewModel.kt`, `ui/GameScreen.kt` | 36 块拼图生成时间 < 500ms，碎片列表滑动 60fps | 2h | P4-1 |
| **P4-6** | 内存优化：使用 `ImageBitmap` 按需裁剪替代全尺寸副本，释放未使用碎片图片 | `engine/ImageSplitter.kt`, `game/GameViewModel.kt` | 加载 2048×2048 图片 + 36 块碎片，峰值内存 ≤ 100MB | 2h | P4-1 |
| **P4-7** | 错误处理与边界 case：网络断开、图片加载失败、AI API 超时、图片尺寸异常（极小/极大） | 全局 | 每种异常情况有对应的用户友好提示（非技术性弹窗），不 crash | 2h | P4-1 |
| **P4-8** | 编写核心业务逻辑单元测试 | `commonTest/` 目录 | `ImageSplitter.split()` 测试：给定图片尺寸和碎片数量，验证碎片数量、覆盖范围、无重叠 | 3h | P1-8 |
| **P4-9** | 执行完整验证 checklist（见第五章） | | 10 个核心验证 case 全部通过 | 1.5h | P4-1~P4-8 |

**Phase 4 依赖**: Phase 3 全部完成

---

### Phase 5: 鸿蒙适配探索（里程碑：确认鸿蒙路径可行性）

**目标**: 验证核心逻辑能否在鸿蒙环境运行

| 任务ID | 任务描述 | 文件范围 | 验收标准 | 预估 | 依赖 |
|--------|---------|---------|---------|------|------|
| **P5-1** | KuiklyUI 环境搭建：拉取 KuiklyUI 模板工程，配置鸿蒙工具链（DevEco Studio） | 独立工程目录 | DevEco Studio 可创建 KuiklyUI 项目并编译 | 3h | - |
| **P5-2** | 迁移 `engine/` 纯 Kotlin 代码到 KuiklyUI 工程，验证编译 | 独立工程 | `engine/`, `engine/model/` 下所有文件在 KuiklyUI 工程中编译通过 | 2h | P5-1 |
| **P5-3** | 迁移 `game/GameState.kt` + `game/GameViewModel.kt`，评估 ViewModel 依赖替换 | 独立工程 | 核心状态管理逻辑可在鸿蒙环境中运行 | 2h | P5-2 |
| **P5-4** | 输出鸿蒙适配评估报告：共享度、阻碍点、工作量估算 | `.trae/documents/harmonyos-feasibility.md` | 明确鸿蒙可行路径和不可行部分 | 1h | P5-3 |

**Phase 5 依赖**: Phase 4 全部完成

---

## 四、任务依赖关系图

```
P0-1 (ImageLoader接口) ──→ P0-2 ──→ P0-3 ──→ P0-4
                                │           │
                                └──→ P0-6 ──┴──→ P0-7 (Phase 0 完成)
                                                     │
                    ┌────────────────────────────────┤
                    ▼                                ▼
                  P1-1 (目标网格)              P1-5 (旋转)
                    │                                │
                    ├──→ P1-2 (拖拽) ──→ P1-3 (吸附) ──→ P1-4 (回弹)
                    │         │            │
                    │         └──→ P2-4 ──┘──→ P2-6
                    │
                    ├──→ P1-7 (滚动)
                    │
                    └────────────────────────────────→ P1-8 (Phase 1 完成)
                                                           │
              ┌────────────────────────────────────────────┤
              ▼                                            ▼
        P2-1 (主题重设计)                          P2-5 (语音提示)
              │
              ├──→ P2-2 (图标导航)
              │
              ├──→ P2-3 (庆祝动画)
              │
              └──→ P2-7 (提示功能) ──→ P2-8 (Phase 2 完成)
                                            │
              ┌─────────────────────────────┤
              ▼                             ▼
        P3-1 (Gemini API)            P3-2 (OpenAI API)
              │                             │
              └──→ P3-3 (故障转移) ←────────┘
                      │
                      ├──→ P3-4 (缓存)
                      │
                      ├──→ P3-5 (主题选择UI)
                      │
                      ├──→ P3-6 (进度UI)
                      │
                      └──→ P3-7 (预览页) ──→ P3-8 (Phase 3 完成)
                                                  │
              ┌───────────────────────────────────┤
              ▼                                   ▼
        P4-1 (Android 验证)              P4-2 (iOS 构建)
              │                                   │
              ├──→ P4-4 (屏幕适配)        └──→ P4-3 (iOS 运行)
              │
              ├──→ P4-5 (性能优化)
              │
              ├──→ P4-6 (内存优化)
              │
              ├──→ P4-7 (错误处理)
              │
              └──→ P4-8 (单元测试) ──→ P4-9 (Phase 4 完成)
                                            │
                                            └──→ P5-1 → P5-2 → P5-3 → P5-4
```

---

## 五、完整核心功能验证 Case

### 5.1 功能正确性验证

| Case ID | 验证场景 | 前置条件 | 操作步骤 | 预期结果 | 覆盖阶段 |
|---------|---------|---------|---------|---------|---------|
| **VC-1** | 内置图片加载与拆分 | 工程构建成功 | 1. 启动 APP → 2. 选择内置图片 → 3. 选择简单难度(4块) → 4. 点击开始 | 拼图界面显示 4 个含图片内容的碎片卡片；目标区域显示 2×2 网格 | P0 |
| **VC-2** | 碎片拖拽吸附 | VC-1 完成 | 1. 拖动碎片 A 到目标区域左上角 → 2. 松手 | 碎片 A 自动吸附到左上角槽位，伴随缩放动画；左上角槽位显示图片内容 | P1 |
| **VC-3** | 碎片回弹 | VC-1 完成 | 1. 拖动碎片 A 到非目标区域 → 2. 松手 | 碎片 A 以弹性动画回到碎片列表原位置 | P1 |
| **VC-4** | 完成流程 | VC-2 相同 | 1. 依次正确放置全部 4 个碎片 | 最后一个碎片放置后 → 跳转完成页面 → 显示 🎉 动画 + 鼓励文字 + "再来一局" 按钮 | P1 |
| **VC-5** | 难度切换 | 在菜单页 | 1. 依次选择 EASY → SIMPLE → MEDIUM → HARD → EXPERT | 每次选择高亮显示对应选项，"开始游戏" 按钮显示的块数随难度变化 | P1 |
| **VC-6** | 提示功能 | 游戏中 | 1. 点击 💡 按钮 | 目标区域有 1 个空位闪烁 3 秒，提示该位置的碎片在碎片列表中也闪烁 | P2 |
| **VC-7** | AI 图片生成 | 配置有效 API Key | 1. 菜单 → 选择 "🐱动物" 主题 → 2. 点击开始 | 生成进度条显示→完成后显示预览图→确认后进入拼图界面，碎片显示 AI 生成的图片 | P3 |
| **VC-8** | AI 故障降级 | 断开网络 | 1. 菜单 → 选择 AI 主题 → 2. 点击开始 | 自动降级：AI 超时后直接使用内置图片，不 crash | P3 |
| **VC-9** | 跨设备竖屏 | 4.7" 手机 + iPad 竖屏 | 1. 在两个设备上分别执行 VC-1 | 两个设备 UI 均正常显示，无元素重叠或溢出 | P4 |
| **VC-10** | 疯狂点击测试 | 游戏中 | 1. 快速连续点击多个碎片 → 2. 快速连续点击开始/返回 | 不 crash，不出现重复碎片或状态异常 | P4 |

### 5.2 低幼用户专项验证

| Case ID | 验证场景 | 儿童年龄 | 操作要求 | 通过标准 |
|---------|---------|---------|---------|---------|
| **KC-1** | 4块拼图独立完成 | 3-4 岁 | 无大人指导，完成 4 块拼图 | 3 分钟内完成 |
| **KC-2** | 9块拼图独立完成 | 5-6 岁 | 无大人指导，完成 9 块拼图 | 5 分钟内完成 |
| **KC-3** | 16块拼图独立完成 | 7-8 岁 | 无大人指导，完成 16 块拼图 | 8 分钟内完成 |
| **KC-4** | 主题选择 | 4-5 岁 | 从六宫格图标中选择喜欢的主题 | 能独立点击图标并说出选择了什么 |
| **KC-5** | 完成页反应 | 3-5 岁 | 完成拼图后观察反应 | 儿童表现出愉悦（笑/鼓掌/欢呼） |

---

## 六、质量要求与低幼设计准则

### 6.1 视觉设计准则

| 准则 | 规范 | 当前差距 |
|------|------|---------|
| **触摸目标** | 所有可交互元素最小 64dp × 64dp | 返回/重置按钮过小 |
| **字体大小** | 最小 16sp，标题 28sp+ | 基本符合 |
| **色彩** | 高饱和度暖色调为主，对比度 ≥ 4.5:1 | 当前 Material3 偏成熟 |
| **圆角** | 卡片/按钮统一 12dp+ 圆角 | 碎片区 8dp，可增大 |
| **图标** | 所有操作按钮必须有 emoji/大图标 | 当前有文字按钮 |

### 6.2 交互设计准则

| 准则 | 规范 | 当前差距 |
|------|------|---------|
| **无惩罚** | 错误操作不显示 X/红叉/警告 | 待验证 |
| **即时反馈** | 每次操作 100ms 内有视觉/听觉反馈 | 待实现音效 |
| **单一焦点** | 每屏只有一个主要操作，避免选项过多 | 菜单页 3 个难度按钮，可接受 |
| **容错** | 碎片接近目标 20% 范围内自动吸附 | 待实现 |
| **无时间压力** | 不设计时器，不显示用时 | ✅ 当前无计时器 |

### 6.3 内容安全准则

| 准则 | 规范 |
|------|------|
| **AI 内容过滤** | 所有 AI 生成的图片需经过安全内容审核（无暴力/恐怖/不当内容） |
| **隐私** | 不收集儿童个人信息，不上传设备 ID 或位置 |
| **家长门** | 设置页面（API Key 配置等）需通过简单算术验证（如 "2+3=?"）进入 |
| **内购保护** | AI 图片生成次数每日上限 20 次（可家长调整），防止误触大量消耗 |

---

##