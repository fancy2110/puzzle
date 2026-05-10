# Puzzle Game - 工程规则

## 项目概览
基于 AI 自动生成拼图素材的跨平台拼图游戏，目标人群为低幼儿。

## 技术栈
- **语言**: Kotlin 2.1.0
- **UI框架**: Compose Multiplatform 1.7.3 (Material3)
- **构建系统**: Gradle 8.11.1 + Kotlin Multiplatform (KMP)
- **网络层**: Ktor 3.0.2
- **序列化**: kotlinx-serialization 1.7.3
- **架构模式**: MVVM (ViewModel + StateFlow)
- **目标平台**: Android (API 26+), iOS (13+), 鸿蒙 (通过 KuiklyUI 适配)

## 构建命令
```bash
# 检查环境
./gradlew --version

# 构建 Android
./gradlew :composeApp:assembleDebug

# 构建 iOS framework
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64

# 清理构建
./gradlew clean

# 代码检查 (Kotlin 编译检查)
./gradlew :composeApp:compileKotlinAndroid
./gradlew :composeApp:compileKotlinIosSimulatorArm64
```

## 项目结构
```
puzzle/
├── composeApp/                     # 主应用模块 (KMP)
│   ├── src/
│   │   ├── commonMain/kotlin/      # 跨平台共享代码
│   │   │   └── com/puzzle/game/
│   │   │       ├── ai/             # AI图像生成
│   │   │       ├── engine/         # 拼图引擎核心
│   │   │       │   └── model/      # 数据模型
│   │   │       ├── game/           # 游戏逻辑 (ViewModel/State)
│   │   │       └── ui/             # Compose UI层
│   │   │           └── theme/      # Material3主题
│   │   ├── androidMain/            # Android平台代码
│   │   └── iosMain/                # iOS平台代码
│   └── build.gradle.kts
├── iosApp/                         # iOS入口 (Swift)
├── build.gradle.kts                # 根构建脚本
├── settings.gradle.kts             # 项目设置
├── gradle.properties               # Gradle属性
├── gradle/
│   ├── libs.versions.toml          # 版本目录
│   └── wrapper/                    # Gradle Wrapper
└── src/                            # (保留) 原Rust原型代码
```

## 模块职责
| 模块 | 路径 | 职责 |
|------|------|------|
| `composeApp` | `composeApp/` | 唯一的KMP模块，包含shared(commonMain) + Android + iOS源码集 |

## 关键设计决策
1. **单模块架构**: 使用单一 `composeApp` 模块，通过 Kotlin source sets 区分平台
2. **MVVM架构**: ViewModel 在 commonMain 中共享，UI 在 commonMain 中用 Compose 共享
3. **AI模块解耦**: 通过接口抽象 AI 图片生成，默认使用 Mock 实现，后续可对接真实 AI API
4. **鸿蒙适配**: 规划通过腾讯 KuiklyUI 框架实现 Compose Kotlin → 鸿蒙 ArkTS 编译
5. **低幼友好设计**: 大色块UI、简单交互、emoji图标、三档难度可调

## 鸿蒙适配策略
- **当前阶段**: Android + iOS 为主要目标，鸿蒙作为后续扩展
- **技术路线**: 核心业务逻辑（PuzzleEngine/ImageSplitter/GameState）纯Kotlin无平台依赖，可直接通过 KuiklyUI 编译到鸿蒙
- **UI层**: 通过 KuiklyUI 的 Compose DSL 兼容层运行，或使用 KuiklyUI 原生 API 重写 UI 层

## 文件命名约定
- 平台特定文件使用 `.android.kt` / `.ios.kt` 后缀
- 共享业务逻辑无后缀
- Compose UI 组件文件以 `Screen.kt` 结尾
- 数据模型文件以模型名命名
