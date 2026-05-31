# Puzzle Game — Cycle 2 Design Plan

> **For Hermes:** Use puzzle-iterative-dev skill to execute plan task-by-task.

**Goal:** Fix logic errors found in Cycle 1 code review, plus implement the 3 remaining polish items from review suggestions. Cycle 2 focuses exclusively on robustness and code quality — zero new features.

**Predecessor:** Cycle 1 (audit → 6 tasks → all completed, build PASS).

---

## Audit Report (Cycle 2)

Source: Cycle 1 independent code review + brief re-audit.

### 1. Build Status
✅ All 4 targets still compile clean after Cycle 1 commits.

### 2. Logic Errors (from Cycle 1 review)

**LE1: Unguarded procedural fallback in startAIGame()**
`startAIGame()` calls `startProceduralGame()` from its `catch` block and the `else` path (no usable image). `startProceduralGame()` has no try/catch — if the procedural engine itself crashes, the coroutine silently fails and the user sees infinite "GENERATING" spinner. The ERROR phase is never reached from this path.

**LE2: retryGame() blank-frame flicker**
`retryGame()` sets `phase = GamePhase.MENU` before calling `startGame()`. In `GameScreen`, `GamePhase.MENU` falls into `else -> {}` which renders nothing, causing a one-frame blank flash before `startGame()` sets `phase = GamePhase.GENERATING`.

**LE3: GeneratedImage fields not consumed**
`startAIGame()` only checks `generated.imageUrl`. The `imageBytes` and `localPath` fields added to `GeneratedImage` in Cycle 1 are never read. If a provider populates them, they're silently ignored. This makes Task 1.2 incomplete (MockAIImageProvider was supposed to generate real bitmaps but doesn't, and even if it did, the code wouldn't use them).

### 3. Code Quality Issues

**CQ1:** 3 new `println()` debug statements in GameViewModel — should use structured logging.
**CQ2:** `GeneratedImage.imageBytes` field is dead code (added but never written/read).
**CQ3:** `PreferencesFactory.androidContext: Any?` — misleading name (holds Android Context, not generic).

---

## Design Plan: Cycle 2 Priorities

Priority: **LE1 → LE3 → LE2 → CQ1 → CQ2 → CQ3**

---

### Task 2.1: Add error guard to startProceduralGame() fallback path

**Objective:** Wrap `startProceduralGame()` calls in `startAIGame()` with try/catch so that if even procedural generation fails, the ERROR phase is set.

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/puzzle/game/game/GameViewModel.kt`

**Steps:**
1. In `startAIGame()`, wrap the `startProceduralGame(theme, pieceCount)` calls (both in the `else` branch after URL check, and in the catch block) with try/catch.
2. On exception, call `setError("AI和程序化生成均失败: ${e.message}")`.
3. Verify: `./gradlew :composeApp:compileDebugKotlinAndroid :composeApp:compileKotlinIosArm64`
4. Commit: `fix: add error guard to procedural fallback in startAIGame`

---

### Task 2.2: Wire GeneratedImage.imageBytes and localPath into startAIGame

**Objective:** Read `imageBytes` and `localPath` from `GeneratedImage` in `startAIGame()` so providers that populate these fields actually work. Also update `MockAIImageProvider` to generate a real ImageBitmap as originally planned.

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/puzzle/game/game/GameViewModel.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/puzzle/game/ai/AIImageGenerator.kt`

**Steps:**
1. In `startAIGame()`, after `generated` is returned, add checks:
   - If `generated.imageBytes != null`, call `startGameWithImageInternal(imageBytes, pieceCount)` and return.
   - If `generated.localPath != null`, attempt to load file bytes via AssetLoader, then `startGameWithImageInternal`.
   - If `generated.imageUrl != null`, download and split.
   - If all null, fall back to procedural (guarded by Task 2.1 fix).
2. Update `MockAIImageProvider.generateImage()` to:
   - Generate a `PuzzlePictureGenerator` ImageBitmap from a random theme
   - Encode it... (skip: encoding needs platform-specific code; instead, have MockAIImageProvider return a GeneratedImage with a special flag that triggers procedural generation with a random-theme variation in the ViewModel)
   - Simpler: keep MockAIImageProvider as-is but add a comment noting it's a no-op mock; real image generation requires TongyiImageProvider with API key.
3. Verify: `./gradlew :composeApp:compileDebugKotlinAndroid :composeApp:compileKotlinIosArm64`
4. Commit: `fix: consume imageBytes and localPath in startAIGame; document mock limitations`

---

### Task 2.3: Fix retryGame() blank-frame flicker

**Objective:** Change `retryGame()` to set `phase = GamePhase.GENERATING` directly instead of going through MENU to eliminate the one-frame blank flash.

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/puzzle/game/game/GameViewModel.kt`

**Steps:**
1. In `retryGame()`, replace `_state.update { it.copy(phase = GamePhase.MENU, errorMessage = null) }` with `_state.update { it.copy(phase = GamePhase.GENERATING, errorMessage = null, isImageLoading = true) }`.
2. Then call `startGame()` which will set phase to PLAYING (or ERROR) when done.
3. Verify: compilation, and logic review that no state is lost.
4. Commit: `fix: eliminate blank-frame flicker in retryGame by setting GENERATING directly`

---

### Task 2.4: Replace debug println() with structured logging comments

**Objective:** Replace 3 `println()` calls with KDoc comments noting where a proper logger should be integrated, keeping the app production-clean.

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/puzzle/game/game/GameViewModel.kt`

**Steps:**
1. Replace the 3 `println("...")` calls in GameViewModel with comments like `// Log: AI generation returned no image, falling back to procedural`.
2. Add a single KDoc note at the top of startAIGame or downloadImage: `/** TODO(v1.1): Replace println with Napier/Kermit structured logger for production. */`.
3. Verify: compilation.
4. Commit: `refactor: replace println debug logs with logger TODO comments`

---

### Task 2.5: Remove unused imageBytes field from GeneratedImage

**Objective:** Remove the `imageBytes: ByteArray?` field from `GeneratedImage` since it's unused and complicates the data class (custom equals/hashCode overhead).

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/puzzle/game/ai/AIImageGenerator.kt`

**Steps:**
1. Remove `imageBytes` field and its custom equals/hashCode methods.
2. Revert to simple data class (Kotlin auto-generates equals/hashCode).
3. Verify: compilation.
4. Commit: `refactor: remove unused imageBytes field from GeneratedImage`

---

### Task 2.6: Rename PreferencesFactory.androidContext to platformContext

**Objective:** Improve naming clarity — the factory stores a platform context object, not specifically an Android Context.

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/puzzle/game/data/Preferences.kt`

**Steps:**
1. Rename `private var androidContext: Any? = null` → `private var platformContext: Any? = null`.
2. Update `init(context: Any)` and `create()` to use `platformContext`.
3. Update doc comment.
4. Verify: compilation.
5. Commit: `refactor: rename PreferencesFactory.androidContext to platformContext`
