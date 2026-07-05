# 🧩 Puzzle Game

A delightful jigsaw puzzle game for children and casual players, built with Kotlin Multiplatform (Compose Multiplatform) targeting Android and iOS.

## Features

- **6 hand-crafted themes**: Cat, Balloon, Ocean, Forest, Space, Flowers
- **4 difficulty levels**: Very Easy (4p) / Easy (6p) / Medium (12p) / Hard (20p)
- **Irregular pieces**: Random-seed BFS expansion algorithm creates uniquely shaped puzzle pieces every game
- **Two interaction modes**: Drag-and-drop or tap-to-place — works great for kids
- **AI image generation** (coming soon): Generate custom puzzle images via Tongyi Wanxiang
- **Celebration effects**: Star particle animation on puzzle completion
- **Dark theme** support

## Architecture

```
composeApp/
├── src/
│   ├── commonMain/   — Shared UI, game engine, AI client
│   ├── androidMain/  — Android entry point
│   └── iosMain/      — iOS entry point
└── build.gradle.kts

experiments/rust-splitter/  — Rust CLI for offline image splitting experiments
```

## Project Structure

| Package | Purpose |
|---------|---------|
| `game/` | GameViewModel, GameState, DragDropState, Navigation |
| `engine/` | PuzzleEngine, ImageSplitter — core puzzle algorithm |
| `data/` | ThemeData, PuzzlePictureGenerator — procedural artwork |
| `ai/` | AIImageGenerator, TongyiImageProvider — AI integration |
| `ui/` | All screens (Splash, Menu, Theme, Game) and components |
| `audio/` | Sound effects via expect/actual |

## Tech Stack

- **Kotlin** 2.3.21
- **Compose Multiplatform** 1.10.3
- **Material 3**
- **Ktor** 3.1.3 (HTTP client for AI APIs)
- **Coil** 3.4.0 (image loading)

## Build

### Android

```bash
./gradlew :composeApp:assembleDebug
```

### iOS

Always open `iosApp/iosApp.xcworkspace` in Xcode. The workspace is the canonical
entry point for both the app and CocoaPods integrations.

Build for an iPad simulator from the command line:

```bash
xcodebuild \
  -workspace iosApp/iosApp.xcworkspace \
  -scheme iosApp \
  -configuration Debug \
  -destination 'platform=iOS Simulator,name=iPad (A16)' \
  build
```

The iOS target supports iPhone and iPad in portrait and landscape orientations.

## Algorithm

The puzzle splitting algorithm (ImageSplitter):

1. Grid the image into 64×64px blocks
2. Randomly select N seed blocks (N = piece count)
3. BFS expand each seed: every round, each seed claims up to 2 unclaimed neighboring blocks (random directions)
4. Assign any remaining orphan blocks to the nearest piece
5. Each piece is a contiguous but irregularly-shaped collection of blocks

## License

MIT
