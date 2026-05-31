# Puzzle Game — Cycle 1 Design Plan

> **For Hermes:** Use puzzle-iterative-dev skill to execute plan task-by-task.

**Goal:** Address high-priority architecture gaps identified in Cycle 1 audit, focusing on making settings persistent, wiring AI image generation, adding error handling, and cleaning up stubbed features.

**Architecture:** KMP + Compose Multiplatform, MVVM with StateFlow, sealed-class navigation. Changes focus on commonMain (shared logic) with minimal platform-specific updates.

**Tech Stack:** Kotlin 2.3.21, Compose Multiplatform 1.10.3, Ktor 3.1.3, Material 3

---

## Audit Report (Cycle 1)

### 1. Build Status
✅ **PASS** — All 4 Kotlin targets compile with zero errors.
```text
compileDebugKotlinAndroid       — UP-TO-DATE
compileKotlinIosArm64           — UP-TO-DATE
compileKotlinIosSimulatorArm64  — UP-TO-DATE
compileKotlinIosX64             — UP-TO-DATE
BUILD SUCCESSFUL in 19s
```

### 2. TODO/Dead Code
✅ **Clean** — No TODO, FIXME, HACK, or XXX comments found.

### 3. expect/actual Pairing
✅ **All 6 expects have both Android and iOS actuals:**
| expect | Android actual | iOS actual |
|--------|---------------|-----------|
| `Preferences` | ✅ SharedPreferences | ✅ NSUserDefaults |
| `decodeToImageBitmap` | ✅ BitmapFactory | ⚠️ returns null (stub) |
| `AssetLoader` | ✅ context.assets | ✅ NSBundle |
| `NativePuzzleEngine` | ✅ JNI + graceful degradation | ✅ cinterop + stub |
| `SoundManager` | ⚠️ all stubs = Unit | ⚠️ all stubs = Unit |
| `getPlatform()` | ✅ AndroidPlatform | ✅ IOSPlatform |

### 4. Architecture Gaps (Critical → Polish)

**G1 (Medium): Settings not persisted** — `soundEnabled` and `referenceEnabled` in `SettingsScreen.kt` use local `remember { mutableStateOf(true) }`. They never read from or write to `Preferences`. Toggles are cosmetic — flipping them has zero effect on the app.

**G2 (Medium): SoundManager fully stubbed** — All 5 `play*()` functions on both Android and iOS return `Unit` with no implementation. This is understandable for initial development but makes the "音效" toggle in Settings meaningless.

**G3 (High): AI image generation not wired** — `TongyiImageProvider` has a complete DashScope API implementation (task submission + polling). `AIImageGenerator` has a mock provider. But neither is connected to `GameViewModel`. The `onGenerateAi` path in `App.kt` just calls `gameViewModel.startGame()` which uses the procedural Kotlin engine — identical to the "当前主题" path.

**G4 (High): iOS ImageBitmap decoding stub returns null** — `decodeToImageBitmap` on iOS is `= null`. This blocks any future feature that loads real images on iOS (AI download, photo library, camera).

**G5 (Medium): No error state in GameScreen** — `GameScreen` has GENERATING / PLAYING / COMPLETED phases. If `startGame()` fails silently (e.g., native engine unavailable + procedural fallback crashes), the user sees an infinite loading spinner. There is no `GamePhase.ERROR` state.

**G6 (Low): Pause state not persisted** — Timer resets and pieces reshuffle if user backgrounds the app.

**G7 (Low): Unused/unclear code** — `ThemeData.prompt` field was originally for AI prompts but is now repurposed as procedural generation context. `MockAIImageProvider.generateImage()` doesn't actually generate images — it returns `imageUrl = null, localPath = null`.

### 5. UI/UX Gaps (Beyond Current Scope)
- No undo/redo for piece placement
- No hint system
- No onboarding/tutorial for first-time users
- No score/high score tracking
- design/prototype.html exists at `design/prototype.html` — not yet compared against current implementation

---

## Design Plan: Cycle 1 Priorities

Priority order: **G1 (Settings persistence) → G3 (AI wiring) → G4 (iOS decode) → G5 (Error handling) → G2 (Sound stub cleanup) → G7 (Code cleanup)**

---

### Task 1.1: Wire Settings persistence to Preferences

**Objective:** Replace local `remember` state in `SettingsScreen` with read/write through `Preferences` expect/actual, so sound and reference toggles survive app restarts.

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/puzzle/game/ui/SettingsScreen.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/puzzle/game/data/Preferences.kt` (add sound/reference getters/setters)
- Modify: `composeApp/src/androidMain/kotlin/com/puzzle/game/data/Preferences.android.kt`
- Modify: `composeApp/src/iosMain/kotlin/com/puzzle/game/data/Preferences.ios.kt`

**Steps:**
1. Add `fun isSoundEnabled(): Boolean` and `fun setSoundEnabled(enabled: Boolean)` to `expect class Preferences` and both actuals.
2. Add `fun isReferenceEnabled(): Boolean` and `fun setReferenceEnabled(enabled: Boolean)` to expect and actuals.
3. In `SettingsScreen`, accept a `Preferences` parameter, replace `remember { mutableStateOf(true) }` with `preferences.isSoundEnabled()` / `preferences.isReferenceEnabled()`.
4. Update `App.kt` to create a `Preferences` instance and pass it to `SettingsScreen`.
5. Verify: `./gradlew :composeApp:compileDebugKotlinAndroid :composeApp:compileKotlinIosArm64`
6. Commit: `feat: wire settings persistence through Preferences expect/actual`

---

### Task 1.2: Integrate AI image generation into GameViewModel

**Objective:** Wire `TongyiImageProvider` / `AIImageGenerator` into `GameViewModel.startGame()` so the AI path actually generates and uses an AI image instead of falling back to procedural art.

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/puzzle/game/game/GameViewModel.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/puzzle/game/ai/AIImageGenerator.kt` (make MockAIImageProvider actually generate an ImageBitmap procedurally so it's useful without API key)
- Possibly modify: `composeApp/src/commonMain/kotlin/com/puzzle/game/ui/ImageSourceScreen.kt`

**Steps:**
1. Add `aiImageGenerator` field to `GameViewModel` (default: MockAIImageProvider).
2. Add `startAIGame()` method that:
   a. Sets phase to GENERATING
   b. Calls `aiImageGenerator.generate(theme.prompt)` in viewModelScope
   c. If `imageUrl != null`, downloads image bytes via Ktor HTTP client
   d. Routes to `startGameWithImage(imageBytes)` for native splitting
   e. On failure, falls back to procedural Kotlin path
3. Update `App.kt` to call `gameViewModel.startAIGame()` instead of `gameViewModel.startGame()` for the `onGenerateAi` callback.
4. Verify: `./gradlew :composeApp:compileDebugKotlinAndroid :composeApp:compileKotlinIosArm64`
5. Commit: `feat: wire AI image generation into GameViewModel`

---

### Task 1.3: Add error state handling in GameScreen

**Objective:** Add a `GamePhase.ERROR` state so that if game generation fails, the user sees an error screen with a "retry" button instead of an infinite loading spinner.

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/puzzle/game/game/GameState.kt` (add ERROR to GamePhase enum)
- Modify: `composeApp/src/commonMain/kotlin/com/puzzle/game/game/GameViewModel.kt` (catch exceptions in startGame/startAIGame, set ERROR phase)
- Modify: `composeApp/src/commonMain/kotlin/com/puzzle/game/ui/GameScreen.kt` (add ErrorScreen composable)

**Steps:**
1. Add `ERROR` to `GamePhase` enum.
2. Wrap `startGame()` and `startGameWithImageInternal()` try/catch blocks; on exception, set `phase = GamePhase.ERROR`.
3. Add an `ErrorScreen` composable showing a friendly message + "重试" button → calls viewModel.startGame().
4. Handle `GamePhase.ERROR` in `GameScreen`'s `when` block.
5. Verify: `./gradlew :composeApp:compileDebugKotlinAndroid :composeApp:compileKotlinIosArm64`
6. Commit: `feat: add ERROR game phase with retry UI`

---

### Task 1.4: Implement iOS ImageBitmap decoding

**Objective:** Replace the iOS `decodeToImageBitmap` stub `= null` with actual NSData→UIImage→ImageBitmap conversion so real images can be displayed on iOS.

**Files:**
- Modify: `composeApp/src/iosMain/kotlin/com/puzzle/game/ImageDecoder.ios.kt`

**Steps:**
1. Implement `decodeToImageBitmap` using `platform.UIKit`:
   - Create `NSData` from `ByteArray`
   - Create `UIImage` from `NSData`
   - Convert to `ImageBitmap` using Compose's `UIImage.toComposeImageBitmap()`
2. Verify: `./gradlew :composeApp:compileKotlinIosArm64`
3. Commit: `fix: implement iOS ImageBitmap decoding from NSData`

---

### Task 1.5: Clean up unused SoundManager stubs

**Objective:** Remove the no-op `SoundManager` stubs on both platforms and either add a comment explaining they're placeholders, or provide minimal SoundPool/AVAudioPlayer implementations.

**Decision:** Keep stubs but rename to make intent clear — add doc comments indicating "Audio not yet implemented, see v1.1 roadmap."

**Files:**
- Modify: `composeApp/src/androidMain/kotlin/com/puzzle/game/audio/SoundManager.android.kt`
- Modify: `composeApp/src/iosMain/kotlin/com/puzzle/game/audio/SoundManager.ios.kt`

**Steps:**
1. Add KDoc comments to all `play*()` methods: `/** TODO(v1.1): Implement with [SoundPool] on Android, [AVAudioPlayer] on iOS. */`
2. Verify: `./gradlew :composeApp:compileDebugKotlinAndroid :composeApp:compileKotlinIosArm64`
3. Commit: `docs: annotate SoundManager stubs with v1.1 roadmap notes`

---

### Task 1.6: Code cleanup — rename prompt field and fix MockAIImageProvider

**Objective:** Rename `ThemeData.prompt` to `ThemeData.description` (since it's used for procedural generation context, not AI prompts) and make `MockAIImageProvider` generate a real procedural ImageBitmap instead of returning nulls.

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/puzzle/game/data/ThemeData.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/puzzle/game/data/PuzzlePictureGenerator.kt` (any reference to `theme.prompt`)
- Modify: `composeApp/src/commonMain/kotlin/com/puzzle/game/ui/MenuScreen.kt` (reference to `theme.prompt`)
- Modify: `composeApp/src/commonMain/kotlin/com/puzzle/game/ui/ThemeScreen.kt` (if references prompt)
- Modify: `composeApp/src/commonMain/kotlin/com/puzzle/game/ai/AIImageGenerator.kt`

**Steps:**
1. Rename `prompt` → `description` in `ThemeData`, update all references.
2. Update `MockAIImageProvider.generateImage()` to call `PuzzlePictureGenerator.generate()` and return the bitmap bytes as a GeneratedImage with `localPath` populated.
3. Verify: `./gradlew :composeApp:compileDebugKotlinAndroid :composeApp:compileKotlinIosArm64`
4. Commit: `refactor: rename ThemeData.prompt→description, make mock AI generate real bitmaps`
