# Puzzle Game — Product Requirements Document v1

> **Date:** 2026-05-31 | **Version:** 1.0 | **Status:** Cycle 1-2 Complete → Roadmap

---

## Executive Summary

Puzzle Game is a Compose Multiplatform (KMP) jigsaw puzzle app targeting Android (minSdk 26) and iOS. Designed for children and casual players, it features hand-crafted procedural artwork, irregular piece shapes via BFS expansion, dual interaction modes (drag-and-drop + tap-to-place), and an AI image generation pipeline. After two development cycles (12 tasks, 20 files), the core architecture is solid, all 4 Kotlin compilation targets pass, and the codebase is production-clean. This PRD analyzes feature gaps against the competitive landscape and proposes a phased roadmap to evolve from MVP to a market-competitive product.

---

## Current State Assessment

### What's Built (Cycle 1-2)

| Feature | Status | Notes |
|---------|--------|-------|
| 7 themes with procedural artwork | ✅ | PupplePictureGenerator draws cat, balloon, ocean, forest, space, flowers, demo |
| 4 difficulty levels (4/6/12/20p) | ✅ | BFS-based irregular splitting |
| Drag-and-drop + tap-to-place | ✅ | DragDropState with both interaction modes |
| Celebration particle effects | ✅ | Star animation on puzzle completion |
| Dark theme support | ✅ | MaterialTheme with light/dark |
| Settings persistence | ✅ | Sound + reference image toggles via Preferences (SharedPreferences/NSUserDefaults) |
| ERROR state + retry UI | ✅ | Graceful degradation on generation failure |
| iOS ImageBitmap decoding | ✅ | Skia Image.makeFromEncoded for real images on iOS |
| AI image generation pipeline | ✅ | TongyiImageProvider (DashScope API), mock fallback, URL download, localPath loading |
| Native Rust engine integration | ✅ | Dual-path: native (JNI/cinterop) + Kotlin fallback |
| expect/actual for 6 platform concerns | ✅ | Preferences, AssetLoader, SoundManager, ImageDecoder, NativePuzzleEngine, Platform |
| MVVM + StateFlow | ✅ | Clean architecture with sealed-class navigation |

### What Works
- Full 4-platform compilation (Android + iOS arm64/simulator/x64)
- Offline play with procedural artwork
- Zero crash paths (all generation paths guarded by try/catch)

### What's Rough
- SoundManager is fully stubbed (all play*() = Unit)
- AI generation defaults to procedural fallback without API key
- No undo/redo, no hint system, no save/load
- No onboarding/tutorial

---

## Competitive Landscape

| | Our App | RV AppStudios (10M+) | Big Cake (5M+) | Kids Jigsaw (50M+) | Jigsaw Puzzles Epic |
|---|---|---|---|---|---|
| **Rating** | N/A (pre-release) | 4.4★ | 4.8★ | 4.5★ | 4.6★ |
| **Puzzle count** | 7 themes procedural | 10,000+ | 1,000+ | 100+ | 20,000+ |
| **Piece range** | 4-20 | up to 840 | adjustable | 12-96 | 4-400 |
| **Custom puzzles** | ❌ | ✅ photo upload | ❌ | ❌ | ✅ |
| **Multiplayer** | ❌ | ✅ competitive | ❌ | ❌ | ❌ |
| **Daily challenges** | ❌ | ✅ | ✅ daily puzzles | ❌ | ✅ |
| **Save progress** | ❌ | ✅ | ✅ "Puzzle Book" | ❌ | ✅ |
| **Reward system** | ❌ | ✅ coins/tickets | ✅ coins | ✅ stickers | ✅ |
| **Edge sorting** | ❌ | ❌ | ✅ "edges first" | ❌ | ❌ |
| **Piece rotation** | ❌ | ✅ optional | ❌ | ❌ | ✅ |
| **Reference image** | ⚠️ toggle (unused) | ✅ preview | ✅ preview | ✅ | ✅ |
| **Sound effects** | ⚠️ stubbed | ✅ | ✅ | ✅ | ✅ |
| **Offline** | ✅ | ✅ | ✅ | ✅ | ✅ |
| **No ads / free** | ✅ (open source) | ❌ ads + IAP | ❌ ads + IAP | ✅ | ❌ IAP |
| **AI generation** | ✅ pipeline built | ❌ | ❌ | ❌ | ❌ |
| **Platform** | Android + iOS | Android + Win | Android + Win | Android + Win | iOS + Mac |

**Key insight:** Our differentiator is **AI-generated puzzles** and **irregular piece shapes** — no competitor has both. The primary gaps are content volume (7 themes vs 10,000+), engagement mechanics (daily challenges, rewards), and UX polish (edge sorting, piece rotation, save/load).

---

## Feature Gap Analysis

| Feature | Priority | Effort | Competitor Coverage | Our Risk |
|---------|----------|--------|---------------------|----------|
| Sound effects (real) | High | S | All competitors | Silence breaks immersion |
| Save/Load game state | High | M | 3/4 competitors | No session persistence |
| Reference image toggle (wire up) | High | S | All competitors | Already built, just wire |
| Edge-only sorting mode | Med | M | 2/4 competitors | Commonly requested |
| Daily challenge / puzzle-of-day | Med | M | 3/4 competitors | Drives retention |
| Undo/Redo | Med | S | 0/4 (unique!) | Differentiation opportunity |
| Hint system | Med | S | 0/4 (unique!) | Differentiation opportunity |
| Photo/camera import | Med | M | 2/4 competitors | Expands content source |
| More procedural themes | Med | S | N/A | Low effort, high impact |
| Piece rotation mode | Low | M | 2/4 | Increases difficulty ceiling |
| Reward / achievement system | Low | L | 3/4 | Engagement driver |
| Onboarding tutorial | Low | S | 0/4 explicit | First-run experience |
| Timer / high score tracking | Low | S | 2/4 | Competitive motivation |
| Multiplayer (co-op/competitive) | XL | XL | 1/4 | Massive scope |

---

## Proposed Feature Roadmap

### v1.1 — Quick Wins (1-2 weeks)

| # | Feature | Why |
|---|---------|-----|
| 1 | **Wire reference image toggle** | Already wired to Preferences; toggle actually shows/hides reference image in GameScreen |
| 2 | **Wire sound effects** | Implement minimal SoundPool (Android) + AVAudioPlayer (iOS) with 5 sounds |
| 3 | **Undo last move** | Simple stack-based undo: pop last placed piece back to tray |
| 4 | **Hint system** | Highlight one unfilled cell briefly (3 hints per game) |
| 5 | **Onboarding overlay** | 3-step first-launch tutorial explaining drag/tap interaction |
| 6 | **Add 5 more procedural themes** | Low-effort content expansion (PuzzlePictureGenerator new draw methods) |

### v1.2 — Core Enhancements (2-4 weeks)

| # | Feature | Why |
|---|---------|-----|
| 7 | **Save/Load game state** | Serialize GameState to Preferences; auto-save on pause/background |
| 8 | **Daily challenge** | Random theme + random difficulty each day; track streak |
| 9 | **Edge-only sorting mode** | Filter piece tray to show edge pieces first |
| 10 | **Photo/camera import** | Platform-specific image picker → native splitter |
| 11 | **Timer + best time tracking** | Per-difficulty best times stored in Preferences |

### v2.0 — Major Features (1-3 months)

| # | Feature | Why |
|---|---------|-----|
| 12 | **AI generation with API key UX** | Settings screen to enter DashScope key; real AI image generation |
| 13 | **Piece rotation mode** | Toggle in settings; pieces rotatable via gesture |
| 14 | **Achievement system** | Badges for: first completion, speed records, theme master, etc. |
| 15 | **More difficulty levels** | 30p, 48p, 100p — extend BFS algorithm |
| 16 | **Share completed puzzle** | Screenshot + share intent (social/chat) |

---

## Technical Feasibility

| Feature | KMP Feasible? | Platform-specific work | Dependencies |
|---------|---------------|----------------------|--------------|
| Sound effects | ✅ common API, platform actuals | SoundPool (Android), AVAudioPlayer (iOS) | .ogg/.wav asset files |
| Reference image toggle | ✅ pure commonMain change | None | Already built |
| Undo | ✅ pure commonMain | None | Stack<PlacedPiece> in GameViewModel |
| Hint | ✅ pure commonMain | None | Highlight animation |
| Onboarding | ✅ Compose UI in commonMain | None | Preferences.isFirstLaunch() |
| More themes | ✅ PuzzlePictureGenerator | None | None |
| Save/Load | ✅ Preferences (expect/actual) | Serialization to SharedPrefs/NSUserDefaults | kotlinx.serialization |
| Daily challenge | ✅ commonMain | None | Date-based seed for Random |
| Edge sorting | ✅ commonMain | None | Filter piece list |
| Photo import | ⚠️ needs expect/actual | ImagePicker (Android), PHPicker (iOS) | Platform image picker APIs |
| Timer tracking | ✅ commonMain | None | Preferences for best times |
| API key UX | ✅ commonMain | None | Preferences.getApiKey() already built |
| Piece rotation | ✅ Compose graphics | None | Rotation gesture + transform |
| Achievements | ✅ commonMain | None | Preferences for badge state |
| More difficulties | ✅ commonMain | None | Extend GameDifficulty enum + BFS |
| Screenshot share | ⚠️ needs expect/actual | Share intent (Android), UIActivityViewController (iOS) | Platform share APIs |

---

## User Journey & UX Improvements

### Current Flow
```
Open → Splash(1.5s) → Menu → [Pick Theme] → [Pick Difficulty] → [Pick Source] → Play → Complete → Menu
```

### Friction Points
1. **No saving** — leaving mid-game loses all progress
2. **Silent experience** — no audio feedback on piece placement
3. **First-time confusion** — no explanation of drag vs tap interaction
4. **Limited content** — 7 themes with no daily variety
5. **Reference image toggle exists but does nothing** — user confusion
6. **No undo** — misplacing a piece requires restart
7. **No hint** — stuck users have no recourse

### Proposed v1.1 Flow
```
Open → Onboarding (first launch) → Splash → Menu
  → [Daily Challenge] OR [Free Play: Theme→Difficulty→Source→Play]
  → Playing (with undo/hint buttons, sound feedback, reference toggle working)
  → Complete (time display, share button) → Menu
  → Save state on pause/background → Resume on relaunch
```

---

## Success Metrics

| Metric | Target | How to Measure |
|--------|--------|---------------|
| Crash-free rate | >99.5% | Firebase Crashlytics / Apple Crash Reports |
| First-day retention | >40% | Firebase Analytics |
| Session length | >5 min avg | In-app timer |
| Puzzle completion rate | >60% | GameViewModel state tracking |
| Reference toggle usage | >80% enabled | Preferences analytics |
| Daily challenge engagement | >30% DAU | Daily challenge start count |

---

## Open Questions

1. **Monetization strategy:** Currently open-source/free. Should we add IAP for premium themes or API-key-based AI generation?
2. **Content pipeline:** Should procedural themes be extended indefinitely, or should we add a photo-upload path?
3. **iOS App Store compliance:** Does the demo1.png asset have proper licensing?
4. **Rust native engine scope:** Should we invest in native splitting for 100+ pieces, or can Kotlin handle it?
5. **Multiplayer:** Is competitive/cooperative multiplayer worth the 3-6 month investment?
6. **Localization:** Target languages? Currently Chinese-only UI strings.

---

## Appendix: Research Notes

### Competitor Deep Dives

**1. RV AppStudios Jigsaw Puzzles (10M+ DL, 4.4★)**
- Strength: Competitive multiplayer mode, 10K+ puzzles, custom puzzle maker
- Weakness: Aggressive ads, pieces "move when touched" (UX complaint)
- Monetization: Ads + IAP to remove ads

**2. Big Cake Jigsaw Puzzles (5M+ DL, 4.8★)**
- Strength: "Puzzle Book" save system, daily puzzles, "edges first" mode
- Weakness: Excessive ads (pre-puzzle, post-puzzle, main page), privacy concerns (shares location + financial data)
- Monetization: Ads + coin IAP

**3. RV AppStudios Kids Jigsaw (50M+ DL, 4.5★)**
- Strength: Completely free, no ads, teacher-approved, 4 game modes
- Weakness: Limited to very young children (ages 2-5), no jigsaw complexity
- Monetization: None (brand play for other RV apps)

**4. Jigsaw Puzzles Epic (iOS, 4.6★)**
- Strength: 20K+ puzzles, 10+ year history, categories, piece rotation
- Weakness: Free tier limited, IAP for full access
- Monetization: Freemium + IAP

### Common User Complaints Across Competitors
- "Ads are too frequent and intrusive" (most common complaint)
- "Pieces are hard to grab on small screens"
- "No way to see the reference image clearly"
- "Can't sort pieces by edges"
- "Progress lost when switching devices"

### Our Competitive Advantages
1. **AI-generated puzzles** — no competitor offers this
2. **Irregular piece shapes** — unique, more challenging than grid-based cuts
3. **No ads, open source** — trust advantage
4. **Cross-platform (Android + iOS)** — most competitors are Android-only or single-platform
5. **Kotlin Multiplatform** — shared codebase reduces maintenance cost vs separate native apps
