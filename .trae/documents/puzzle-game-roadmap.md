# 趣味拼图 —— 项目任务拆分与 Roadmap

---

## 一、工程现状与特点分析

### 1.1 当前工程结构

```
puzzle/
├── composeApp/                              # KMP 主应用模块
│   └── src/
│       ├── commonMain/kotlin/com/puzzle/game/
│       │   ├── ai/AIImageGenerator.kt        # AI 图片生成 (Mock 实现)
│       │   ├── engine/
│       │   │   ├── PuzzleEngine.kt           # 拼图引擎入口
│       │   │   ├── ImageSplitter.kt          # 随机矩形拆分算法
│       │   │   └── model/                    # Block / Position / PuzzlePiece / Rect
│       │   ├── game/
│       │   │   ├── GameState.kt              # 游戏状态 (StateFlow + 三档难度)
│       │   │   └── GameViewModel.kt          # MVVM ViewModel
│       │   ├── ui/
│       │   │   ├── GameScreen.kt             # 游戏界面 (占位色块 + 点击拼图)
│       │   │   ├── MenuScreen.kt             # 菜单界面 (难度选择 + 主题输入)
│       │   │   └── theme/Theme.kt            # Material3 亮色主题
│       │   ├── App.kt                        # 应用入口 Composable
│       │   └── Platform.kt                   # expect 声明
│       ├── androidMain/                      # Android Activity + manifest
│       └── iosMain/                          # iOS ComposeUIViewController
├── iosApp/iosApp/                            # Swift 入口 (iOSApp.swift + Info.plist)
├── gradle/                                   # Gradle Wrapper + libs.versions.toml
└── src/                                      # (保留) 原 Rust 拼图拆分原型
```

### 1.2 已有功能

| 模块 | 功能 | 完成度 | 问题 |
|------|------|--------|------|
| **拼图引擎** | 网格拆分 + 随机种子扩展 + BFS 区域扩展 | 70% | 仅矩形拆分，无曲线/锯齿边缘；纯数学模型，无图片裁剪 |
| **AI 模块** | `AIImageProvider` 接口 + `MockAIImageProvider` | 20% | 仅返回占位数据，无真实 AI 对接 |
| **游戏逻辑** | MVVM + StateFlow + 三档难度 | 60% | `startGame()` 依赖 `imageSource != null` 但无设置入口；无计时/计分/存档 |
| **UI 层** | MenuScreen + GameScreen + CompletedScreen | 40% | 纯色块占位显示；无真实图片渲染；无拖拽交互；无动画 |
| **平台入口** | Android Activity + iOS ViewController | 80% | Android 构建已验证通过，iOS framework 未验证 |
| **构建系统** | Gradle KMP + AGP + Compose Multiplatform 1.10.3 | 90% | Kotlin 2.3.21 编译通过 |

### 1.3 核心技术栈

| 组件 | 版本 | 用途 |
|------|------|------|
| Kotlin | 2.3.21 | 编程语言 |
| Compose Multiplatform | 1.10.3 | 跨平台 UI 框架 |
| Material3 | 1.10.0-alpha05 | UI 组件库 |
| AGP | 8.11.2 | Android 构建 |
| Lifecycle ViewModel | 2.10.0 | MVVM 架构 |
| Ktor | 3.1.3 | HTTP 客户端 (AI API 调用) |
| kotlinx-serialization | 1.8.1 | JSON 序列化 |
| kotlinx-coroutines | 1.10.2 | 协程异步处理 |
| KMP 平台目标 | Android(26+) + iOS(13+) | 目标平台 |

---

## 二、目标用户画像与体验设计原则

### 2.1 目标用户：低幼儿童（3~10岁）

| 年龄层 | 特点 | 设计要点 |
|--------|------|----------|
| 3~5岁 | 识字量极少，精细操作能力弱 | 图标化操作、大按钮(≥64dp)、语音引导、6块以内拼图 |
| 6~8岁 | 认识简单文字，开始有逻辑思维 | 图文混合指引、中等难度(6~20块)、鼓励性反馈 |
| 9~10岁 | 独立操作能力强，追求成就感 | 高难度挑战(20~40块)、计时模式、收藏/成就系统 |

### 2.2 用户体验设计原则 (UX Tenets)

1. **无压力**: 不设时间限制，不惩罚错误操作
2. **大而清晰**: 按钮 ≥ 56dp 触控区域，文字 ≥ 16sp
3. **即时反馈**: 拼对立刻有视觉+声音鼓励；拼错柔和提示
4. **逐步引导**: 首次进入有教程引导，难度递进平滑
5. **色彩温暖**: 使用柔和暖色调，避免刺眼高饱和色
6. **安全内容**: 所有 AI 生成的图片经过安全审查，适合儿童
7. **无需读写**: 核心操作无需文字，依赖图标和颜色

### 2.3 竞品关键功能参考

| 功能 | 竞品参考 | 本项目采纳 |
|------|----------|-----------|
| 多种主题分类 | Kids' Puzzles (40+ 免费图片) | ✅ AI 按主题生成 |
| 难度分级 (3×3 ~ 5×5) | 儿童拼图益智 | ✅ 3档 + 可扩展 |
| 拖拽+旋转拼合 | 儿童拼图益智 | ✅ 拖拽为核心交互 |
| 语音引导 | 多多拼图 | ✅ Phase 2 引入 |
| 星星奖励 | Kids' Puzzles | ✅ 完成奖励 |
| 无时间压力 | 多多拼图 | ✅ 默认无计时 |
| 正向反馈 | 全民拼拼图 | ✅ 动画+音效 |
| AI 个性化生成 | prompt-to-puzzle (Gemini) | ✅ AI 核心卖点 |

---

## 三、整体工程目标

### 3.1 核心目标 (Product Vision)

> 打造一款让 3~10 岁小朋友通过 AI 个性化生成拼图素材、在拖拽拼接中培养观察力与专注力的跨平台益智拼图游戏。

### 3.2 北极星指标

- **DAU 次均会话时长** ≥ 5 分钟
- **拼图完成率** ≥ 80%（拼图开始后成功完成的比率）
- **次日留存** ≥ 30%

### 3.3 架构原则

1. **核心业务逻辑 100% 共享**: 拼图引擎、游戏状态机、AI 接口、数据模型全部在 `commonMain`
2. **UI 层 100% 共享**: 所有 Compose 界面在 `commonMain`，无平台特定 UI 代码
3. **平台特定最小化**: 仅图片加载（Coil/Nuke）、音效播放、文件存储需要 `actual` 实现
4. **MVVM 单向数据流**: ViewModel → StateFlow → Compose UI，无反向依赖
5. **AI 接口解耦**: `AIImageProvider` 接口抽象，Mock → 本地素材库 → 云端 AI 渐进式升级

### 3.4 代码规范

| 维度 | 规范 |
|------|------|
| **包结构** | `com.puzzle.game.[layer].[module]` |
| **文件命名** | 平台特定: `*.android.kt` / `*.ios.kt`; Screen 文件: `*Screen.kt` |
| **架构分层** | `ai/` = AI服务, `engine/` + `engine/model/` = 领域核心, `game/` = 状态管理, `ui/` = 界面 |
| **状态管理** | ViewModel + MutableStateFlow，禁止在 Composable 中直接修改状态 |
| **依赖注入** | 手工 DI（避免引入 Koin/Hilt 的 KMP 兼容问题） |
| **资源组织** | `composeResources/` 存放图标、字体、音效 |
| **API 约定** | 异步操作用 `suspend`，返回 `Result<T>`，错误统一处理 |
| **测试要求** | 引擎层: 单元测试覆盖率 ≥ 80%; UI 层: 关键流程截图测试 |

### 3.5 需要使用的 Skills

| Skill | 用途 |
|-------|------|
| `kotlin-multiplatform` | KMP 工程配置与调试 |
| `compose-multiplatform` | Compose UI 开发 |
| `material3-design` | Material3 组件与主题定制 |
| `ktor-client` | AI API 网络请求 |
| `image-loading-kmp` | 跨平台图片加载 (Coil Multiplatform) |
| `git-workflow` | 版本控制与分支管理 |

---

## 四、完整功能列表 (Feature Backlog)

### Feature List

| ID | 功能 | 优先级 | 预估工时 |
|----|------|--------|----------|
| **F1** | 真实图片加载与渲染（平台图片加载器 expect/actual） | P0 | 3天 |
| **F2** | 拼图碎片真实图片裁剪与展示 | P0 | 3天 |
| **F3** | 拖拽交互系统（Drag & Drop 拼图到目标区） | P0 | 5天 |
| **F4** | AI 云端图片生成对接（Ktor → Gemini/Stable Diffusion API） | P0 | 3天 |
| **F5** | 本地主题素材库（预设分类: 动物/交通/太空/自然） | P0 | 2天 |
| **F6** | 拼图完成检测与自动判定 | P0 | 1天 |
| **F7** | 子母网格目标区（预览原图缩略图） | P1 | 2天 |
| **F8** | 完成动画（星星飘落 + 粒子特效） | P1 | 3天 |
| **F9** | 音效系统（拼对/拼错/完成/背景音乐） expect/actual | P1 | 3天 |
| **F10** | 首次引导教程（3步引导: 选图→拖拽→完成） | P1 | 2天 |
| **F11** | 难度自适应（根据年龄/历史表现推荐） | P2 | 2天 |
| **F12** | 计时模式（可选开启，无惩罚） | P2 | 2天 |
| **F13** | 星星收集与成就徽章 | P2 | 3天 |
| **F14** | 图片历史记录（保存最近生成/完成的图片） | P2 | 2天 |
| **F15** | 家长控制面板（年龄设置/时长限制/AI内容安全审查） | P3 | 4天 |
| **F16** | 离线模式（预置素材库完整打包） | P3 | 2天 |
| **F17** | 多语言支持（中/英） | P3 | 1天 |
| **F18** | iOS Xcode 项目完整配置并验证真机运行 | P0 | 2天 |

---

## 五、任务拆分细则 (Task Breakdown)

---

### Phase 0: 基础设施（已完工 ✅）

| 编号 | 任务 | 产出 | 验证标准 | 状态 |
|------|------|------|----------|------|
| P0-1 | KMP + Compose Multiplatform 工程骨架 | `build.gradle.kts`, `settings.gradle.kts`, `libs.versions.toml` | `./gradlew :composeApp:compileDebugKotlinAndroid` 成功 | ✅ Done |
| P0-2 | 拼图引擎核心算法（ImageSplitter + PuzzleEngine） | `engine/` 包下全部模型和算法 | 单元测试: 拆分后碎片数=参数一致，所有 Block 被覆盖 | ✅ Done |
| P0-3 | MVVM 游戏状态机（GameState + GameViewModel） | `game/` 包 | 状态流转 MENU→GENERATING→PLAYING→COMPLETED 正常 | ✅ Done |
| P0-4 | Compose UI 骨架（MenuScreen + GameScreen + Theme） | `ui/` 包 | 界面可展示，颜色占位渲染 OK | ✅ Done |
| P0-5 | 平台入口（Android Activity + iOS ViewController） | `androidMain/`, `iosMain/` | Android Debug 构建成功 | ✅ Done |
| P0-6 | Trae 工程规则 + Git 初始化 | `.trae/rules/`, `.gitignore` | `git log` 有初始 commit | ✅ Done |

---

### Phase 1: 最小可玩原型 (MVP) —— 预计 2~3 周

> **目标**: 可以在 Android 设备上完成一局完整的拼图游戏（选择主题 → AI 生成图片 → 拖拽拼图 → 完成庆祝）

#### Task 1.1: 跨平台图片加载系统
- **依赖**: P0-1
- **描述**: 实现 `expect/actual` 图片加载器，commonMain 定义 `ImageLoader` 接口，androidMain 用 Coil，iosMain 用 Nuke/KMP 适配
- **产出**:
  - `com/puzzle/game/image/ImageLoader.kt` (expect interface)
  - `com/puzzle/game/image/ImageLoader.android.kt` (Coil actual)
  - `com/puzzle/game/image/ImageLoader.ios.kt` (Nuke actual)
  - `gradle/libs.versions.toml` 添加 Coil Multiplatform 依赖
- **验证标准**:
  - [ ] Android 设备上 GameScreen 能显示预设的占位图片
  - [ ] iOS 模拟器上 GameScreen 能显示预设的占位图片
  - [ ] 图片加载失败时有低幼友好的错误占位图（如可爱小动物+文字"图片加载中..."）

#### Task 1.2: 拼图碎片真实图片裁剪与渲染
- **依赖**: Task 1.1, P0-2
- **描述**: 将 `PuzzlePiece.pixels` (Rect) 对应区域的图片裁剪为独立 Bitmap/ImageBitmap，在 PieceCard 中渲染
- **产出**:
  - `com/puzzle/game/engine/ImageCutter.kt` (根据 PuzzlePiece 列表裁剪原图)
  - 更新 `GameScreen.kt` PieceCard 从占位色块改为真实图片渲染
  - 更新 `PuzzleEngine.kt` 接收 ImageBitmap 传入裁剪
- **验证标准**:
  - [ ] 碎片列表展示 6/12/20 块时，每块显示正确的图片局部
  - [ ] 所有碎片无缝拼接还原原图（无重叠、无间隙）
  - [ ] 裁剪性能：800×600 图片拆 20 块 < 200ms

#### Task 1.3: 本机预设主题素材库
- **依赖**: P0-1
- **描述**: 打包 5~10 张儿童友好主题图片（动物/交通工具/太空/自然等），作为无需 AI API 的本地素材
- **产出**:
  - `composeApp/src/commonMain/composeResources/drawable/` 下 6 张 PNG 图片
  - `com/puzzle/game/data/AssetImageProvider.kt` 实现 AIImageProvider 接口
  - 更新 `AIImageGenerator` 支持切换本地/AI 两种 Provider
  - MenuScreen 添加"选一个主题"卡片列表
- **验证标准**:
  - [ ] MenuScreen 展示 6 个主题卡片（可爱猫咪、彩虹热气球、海底世界、森林动物、太空冒险、美丽花朵）
  - [ ] 点击卡片 → 进入游戏，拼图碎片为真实图片
  - [ ] 图片尺寸适配屏幕（不拉伸不变形）

#### Task 1.4: 拖拽交互重构
- **依赖**: Task 1.2
- **描述**: 替换当前点击即拼的简单交互，实现完整拖拽系统
- **产出**:
  - `com/puzzle/game/ui/component/DragTarget.kt` (目标区 Composable)
  - `com/puzzle/game/ui/component/DraggablePiece.kt` (可拖拽碎片 Composable)
  - `com/puzzle/game/ui/component/PuzzleBoard.kt` (拼图板状态管理)
  - 更新 `GameViewModel` 添加板面状态 (2D 矩阵 track 每块位置)
  - 更新 `GameState` 添加 `boardState: List<List<PlacedPiece?>>`
- **交互细节**:
  - 长按碎片 → 浮起放大 → 拖拽到目标区 → 松手吸附/弹回
  - 目标区有磁性吸附（碎片靠近目标位置 ±20dp 自动吸附）
  - 碎片放到错误位置时弹回原位（柔和弹簧动画）
  - 碎片放到正确位置时高亮闪烁（绿色脉冲）
- **验证标准**:
  - [ ] 能从碎片列表中拖出一块，放到目标区正确位置并自动吸附
  - [ ] 放到错误位置自动弹回
  - [ ] 全部拼对后自动触发完成判定
  - [ ] 拖拽过程无视觉抖动或性能卡顿（60fps）

#### Task 1.5: AI 云端图片生成对接
- **依赖**: P0-1
- **描述**: 实现 `CloudAIImageProvider`，通过 Ktor 调用 AI 图片生成 API
- **产出**:
  - `com/puzzle/game/ai/CloudAIImageProvider.kt` (Gemini Imagen / Stable Diffusion API)
  - `com/puzzle/game/ai/AIImageApi.kt` (Ktor 网络层封装)
  - `com/puzzle/game/ai/ImageSafetyFilter.kt` (内容安全审查：关键词过滤)
  - 生成中的 Loading UI（可爱的动画小精灵）
- **验证标准**:
  - [ ] 输入"一只可爱的小猫咪" → 返回真实图片 Bitmap
  - [ ] 网络错误时降级到本地预设素材
  - [ ] 输入不当词汇时自动替换为安全主题
  - [ ] 生成图片尺寸最低 512×512

#### Task 1.6: 完成检测与自动判定
- **依赖**: Task 1.4
- **描述**: 每次拖放后自动检测拼图是否完成，触发完成流程
- **产出**:
  - `com/puzzle/game/engine/PuzzleValidator.kt` (对比 BoardState 与原始 Block 布局)
  - 更新 `GameViewModel.placePiece()` 增加自动完成检测
- **验证标准**:
  - [ ] 最后一块拼上后自动转为 COMPLETED 状态
  - [ ] 完成判定延迟 < 100ms
  - [ ] 误判率 = 0（不会部分拼完就跳完成）

#### Task 1.7: 完成动画与鼓励反馈
- **依赖**: Task 1.5
- **描述**: 完成拼图后的庆祝动画和正面反馈
- **产出**:
  - `com/puzzle/game/ui/component/CelebrationAnimation.kt` (星星飘落 + 缩放 + 粒子)
  - 完成后显示"太棒了！✨"文案 + "某某小朋友真厉害" 个性化鼓励
  - 动画时长约 3 秒，可跳过
  - 更新 `CompletedScreen` 整合动画
- **验证标准**:
  - [ ] 完成后出现星星飘落动画 ≥ 10 个星星
  - [ ] 中央拼图缩放动画（0.8→1.0 弹性效果）
  - [ ] 动画流畅无卡顿（60fps）

#### Task 1.8: iOS 平台验证
- **依赖**: Task 1.1 ~ 1.7
- **描述**: 在 iOS 模拟器上验证完整游戏流程
- **产出**:
  - 修复 iOS 平台编译问题（如有）
  - 验证 iOS 图片加载正确
  - 验证 iOS 拖拽交互正常
- **验证标准**:
  - [ ] `./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64` 成功
  - [ ] iOS 模拟器上完成一局完整游戏（选图→拖拽→完成）

---

### Phase 2: 完整体验增强 —— 预计 2~3 周

> **目标**: 游戏体验打磨到可对外发布 Beta 版的质量水平

#### Task 2.1: 音效系统
- **依赖**: Phase 1 完成
- **产出**:
  - `com/puzzle/game/audio/AudioPlayer.kt` (expect/actual)
  - 音效资源: 拼对声音("叮咚")、拼错声音(柔和"嗯~")、完成音乐、背景音乐
  - 音量控制（家长面板中可调）
- **验证标准**: 拼对/拼错/完成各有不同音效

#### Task 2.2: 新手引导教程
- **依赖**: Phase 1 完成
- **产出**:
  - `com/puzzle/game/ui/tutorial/TutorialScreen.kt`
  - 3 步骤引导：① 选一个喜欢的图案 → ② 拖动碎片到空格 → ③ 全部拼好就完成啦！
  - 每步有手指动画指示
  - 仅首次启动展示，后续可跳过
- **验证标准**: 首次启动展示引导，完成后不再显示

#### Task 2.3: 参考缩略图显示
- **依赖**: Task 1.4
- **产出**:
  - 游戏界面增加小型参考图区域（右上角可展开）
  - 低幼模式：目标区显示半透明原图（如 30% 透明度）辅助定位
- **验证标准**: 参考图与原图一致；低幼模式目标区有半透明指引

#### Task 2.4: 自定义难度
- **依赖**: Phase 1 完成
- **产出**:
  - 增加 VERY_EASY(4块) 和 EXPERT(30块) 难度
  - 碎片形状可配置：矩形 / 曲线 / 锯齿
  - 实现不规则碎片边缘拆分算法 `JigsawSplitter`
- **验证标准**: 选择矩形/曲线/锯齿模式，碎片边缘形状不同

#### Task 2.5: 计时器与统计
- **依赖**: Phase 1 完成
- **产出**:
  - 游戏界面底部计时器（可选显示）
  - 完成页展示用时
  - 本地历史记录（最近 20 局）
- **验证标准**: 计时器从开始到完成准确计时；历史记录可查看

---

### Phase 3: 扩展与优化 —— 预计 2~3 周

#### Task 3.1: 星星收集与成就徽章
- **产出**: 每完成一局获得 1~3 颗星星（按难度）；解锁成就徽章（"拼图新手"→"拼图达人"→"拼图大师"）

#### Task 3.2: 家长控制面板
- **产出**: 密码保护的设置页；可设置年龄/每日时长/AI 内容安全级别/音效音量

#### Task 3.3: 多语言 i18n
- **产出**: `composeResources/values/strings.xml` (中/英)；所有文案走资源文件

#### Task 3.4: 离线模式
- **产出**: 预置 20+ 张本地素材 + 本地 AI 模型（TFLite）生成

#### Task 3.5: 鸿蒙适配 PoC
- **产出**: KuiklyUI 试点工程；核心引擎 + 游戏逻辑验证编译通过

---

## 六、任务依赖关系图

```
P0 基础设施 (✅Done)
  │
  ├──► T1.1 图片加载 ──┬──► T1.2 图片裁剪 ──┬──► T1.4 拖拽交互 ──► T1.6 完成检测 ──► T1.7 完成动画
  │                    │                     │
  ├──► T1.3 预设素材 ──┘                     ├──► T2.3 参考缩略图
  │                                          │
  ├──► T1.5 AI生成对接 ──────────────────────┘
  │
  └──► T1.8 iOS验证 ──► Phase 1 完成 ──► T2.1 音效 ──► T2.2 引导 ──► T2.4 自定义难度 ──► T2.5 计时统计
                                                                                          │
                                                                                          ▼
                                                                                    Phase 2 完成
                                                                                          │
                                                                                          ▼
                                                                     T3.1 成就 ──► T3.2 家长面板 ──► T3.3 i18n ──► T3.4 离线 ──► T3.5 鸿蒙PoC
```

---

## 七、核心功能验证 Case

### 7.1 拼图引擎验证

| Case | 输入 | 期望输出 | 验证方式 |
|------|------|----------|----------|
| CE-1 基本拆分 | image=800×600, pieceCount=6 | 6 个碎片，全部 Block 被覆盖 | 单元测试 assert |
| CE-2 所有区域覆盖 | image=800×600, pieceCount=12 | 所有 (columns×rows) 个 Block 的 isTaken=true | 单元测试计数 |
| CE-3 无重叠覆盖 | image=800×600, pieceCount=20 | 任意两个碎片 items 无交集 | 单元测试 Set 校验 |
| CE-4 碎片面积 | image=800×600, pieceCount=6 | 各碎片面积差异 < 50% | 单元测试方差检查 |
| CE-5 边界情况 | image=100×100, pieceCount=10 | pieceCount 自动 clamp 到 Block 总数 | 单元测试无 crash |
| CE-6 不规则拆分 | image=800×600, pieceCount=12, mode=JIGSAW | 碎片边缘非直线 | 单元测试边缘形状判定 |

### 7.2 游戏流程验证

| Case | 操作序列 | 期望结果 | 验证方式 |
|------|----------|----------|----------|
| GF-1 正常流程 | 选难度→开始→拖6块→完成 | MENU→GENERATING→PLAYING→COMPLETED | 手动测试 |
| GF-2 返回菜单 | PLAYING → 点"返回" | 回到 MENU，状态重置 | 手动测试 |
| GF-3 重置游戏 | PLAYING → 点"重置" | 碎片回到初始位置，棋盘清空 | 手动测试 |
| GF-4 拖放正确 | 拖碎片A到正确目标位置 | 碎片吸附到目标区，高亮闪烁 | 手动测试 |
| GF-5 拖放错误 | 拖碎片A到错误目标位置 | 碎片弹回原位，弹簧动画 | 手动测试 |
| GF-6 AI 生成失败 | AI API 返回 500 | 自动降级到本地预设素材 | 手动测试 / Mock |
| GF-7 全部完成后 | 最后一块拼上 | 自动跳转 COMPLETED，星星动画播放 | 手动测试 |

### 7.3 UI/UX 验证

| Case | 检查点 | 期望结果 | 验证方式 |
|------|--------|----------|----------|
| UX-1 按钮最小尺寸 | MenuScreen 所有按钮 | ≥ 56dp 高度 | 手动测量 / 截图对比 |
| UX-2 字体最小尺寸 | 所有 Text composable | ≥ 16sp (标题除外) | Lint 规则检查 |
| UX-3 对比度 | 文字 vs 背景色 | WCAG AA (≥4.5:1) | 自动对比度检查 |
| UX-4 主题一致性 | 所有 Screen | Material3 Light 调色板统一 | 手动 review |
| UX-5 加载状态 | AI 生成中 | 显示 Loading 动画 + 进度文案 | 手动测试 |
| UX-6 空状态 | 无网络/无素材 | 显示友好占位提示 | 手动测试 |
| UX-7 横竖屏适配 | 旋转设备 | UI 布局不崩溃，元素可见 | 手动测试 |

### 7.4 平台验证

| Case | 平台 | 验证点 |
|------|------|--------|
| PL-1 Android 真机 | Pixel / 小米 | 安装 APK → 完整一局 → 无 crash |
| PL-2 iPhone 模拟器 | iPhone 15 Simulator | 启动 → 完整一局 → 无 crash |
| PL-3 iPad 适配 | iPad 10 Simulator | UI 布局合理 → 碎片大小适配 |
| PL-4 性能 | 低端 Android (4GB RAM) | 帧率 ≥ 30fps → 内存 < 200MB |

---

## 八、Roadmap 时间线

```
Week 1-3   Phase 1: MVP
           ├── W1: T1.1 图片加载 + T1.3 预设素材
           ├── W2: T1.2 图片裁剪 + T1.4 拖拽交互
           └── W3: T1.5 AI生成 + T1.6 完成检测 + T1.7 完成动画 + T1.8 iOS验证
           
Week 4-6   Phase 2: 体验增强
           ├── W4: T2.1 音效 + T2.2 引导教程
           ├── W5: T2.3 参考缩略图 + T2.4 不规则碎片
           └── W6: T2.5 计时统计 + 内部 Beta 测试

Week 7-9   Phase 3: 扩展与优化
           ├── W7: T3.1 成就 + T3.2 家长面板
           ├── W8: T3.3 i18n + T3.4 离线模式
           └── W9: T3.5 鸿蒙 PoC + 性能优化 + App Store 提交
```

---

## 九、质量标准与验收

### 9.1 代码质量

- [ ] 拼图引擎单元测试覆盖率 ≥ 80%
- [ ] `commonMain` 零平台依赖（无 `android.*` / `platform.UIKit.*` import）
- [ ] 所有 `suspend` 函数有超时处理（5s 默认）
- [ ] Lint 检查零 warning（禁止 `!!` 强制解包、禁止 `magic number` 除外）

### 9.2 界面质量

- [ ] 所有页面在 4"~12.9" 屏幕适配正常
- [ ] 色彩对比度 WCAG AA 合规
- [ ] 触摸热区 ≥ 48dp，主要按钮 ≥ 56dp
- [ ] 大标题字体 ≥ 28sp，正文 ≥ 16sp
- [ ] 动画时长 250~500ms，不超过 1000ms

### 9.3 内容安全

- [ ] AI 生成 Prompt 关键词过滤
- [ ] AI 生成图片通过 Google SafeSearch / 内容审核 API
- [ ] 本地预设素材全部为儿童友好内容

### 9.4 交付物

| 交付物 | 说明 |
|--------|------|
| Android APK | Debug + Release |
| iOS IPA | TestFlight 分发 |
| 技术文档 | `.trae/rules/project_rules.md` 更新 |
| 测试报告 | 验证 Case 执行结果 |
| AI 对接文档 | API Key 配置说明 |

---

## 十、风险与缓解

| 风险 | 影响 | 概率 | 缓解措施 |
|------|------|------|----------|
| Compose Multiplatform iOS 性能不达标 | 高 | 中 | 降低动画复杂度；预留 iOS 原生 UI 降级方案 |
| AI API 费用高/不稳定 | 中 | 中 | 本地预设 20+ 素材兜底；缓存机制减少重复调用 |
| 不规则碎片拆分算法复杂 | 中 | 低 | Phase 1 使用矩形拆分；Phase 2 逐步引入曲线 |
| 鸿蒙 KuiklyUI 兼容性不足 | 低 | 中 | 核心逻辑先行验证；UI 层备选 ArkUI 原生重写 |
| 低幼用户测试难获取 | 中 | 高 | 内部员工子女先行测试；简化交互降低门槛 |
